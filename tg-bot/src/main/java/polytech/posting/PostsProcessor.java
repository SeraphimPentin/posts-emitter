package polytech.posting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polytech.bot.FileIsTooBigException;
import polytech.bot.MessageSender;
import polytech.bot.TgContentManager;
import polytech.data.domain.Account;
import polytech.data.domain.ChannelGroup;
import polytech.data.domain.UserChannels;
import polytech.data.repositories.AccountsRepository;
import polytech.data.repositories.ChannelGroupsRepository;
import polytech.data.repositories.UserChannelsRepository;
import polytech.OkAuthorizator;
import polytech.exceptions.OkApiException;
import polytech.posting.ok.OkPostProcessor;
import polytech.posting.vk.VkPostProcessor;
import polytech.ratelim.RateLimiter;
import polytech.ratelim.Throttler;
import polytech.util.Emojis;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class PostsProcessor implements IPostsProcessor {
    private static final String SINGLE_ITEM_POSTS = "";
    private static final String ERROR_POST_MSG = "Упс, что-то пошло не так " + Emojis.SAD_FACE + " \n"
            + "Не удалось опубликовать пост в ok.ru/group/";
    private static final String CHANNEL_INFO_ERROR = "Ошибка получения информации по каналу.";
    private static final String TOO_MANY_API_REQUESTS_MSG = "Превышено количество публикаций в единицу времени";
    private static final String AUTHOR_RIGHTS_MSG = "Пересланный из другого канала пост не может быть опубликован в "
            + "соответствии с Законом об авторском праве.";
    private static final Logger LOGGER = LoggerFactory.getLogger(PostsProcessor.class);
    private static final String TG_LOAD_POST_ERROR_MGS = "Ошибка при загрузке поста из телеграмма";
    private static final String TG_FILE_IS_TOO_BIG_MGS = "%s: Размер файла не может превышать %d Мб"
            .formatted(TG_LOAD_POST_ERROR_MGS, TgContentManager.FILE_SIZE_LIMIT_MB);
    private static final String UNEXPECTED_ERROR_MSG = "Произошла непредвиденная ошибка при обработке поста ";
    private static final String NO_USER_FOUND = "No chat found for channel %s.";

    @Autowired
    private RateLimiter postingRateLimiter;

    @Autowired
    private Throttler repliesThrottler;

    @Autowired
    private OkPostProcessor okPostProcessor;

    @Autowired
    private OkAuthorizator okAuthorizator;

    @Autowired
    private VkPostProcessor vkPostProcessor;

    @Autowired
    private UserChannelsRepository userChannelsRepository;

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private ChannelGroupsRepository channelGroupsRepository;

    @Lazy
    @Autowired
    private TgContentManager tgContentManager;

    @Autowired
    private MessageSender tgNotificator;

    @Override
    public void processPostsInChannel(long channelId, List<Message> posts) {
        Map<String, List<Message>> postsItems = posts.stream().collect(
                Collectors.groupingBy(
                        post -> post.getMediaGroupId() == null ? SINGLE_ITEM_POSTS : post.getMediaGroupId(),
                        Collectors.toList()
                ));
        postsItems.getOrDefault(SINGLE_ITEM_POSTS, Collections.emptyList())
                .forEach(post -> processPostItems(channelId, Collections.singletonList(post)));
        postsItems.remove(SINGLE_ITEM_POSTS);
        postsItems.values().forEach(items -> processPostItems(channelId, items));
    }

    private void processPostItems(long channelId, List<Message> postItems) {
        if (postItems.isEmpty()) {
            return;
        }
        Message postItem = postItems.get(0);
        Long ownerChatId = userChannelsRepository.getUserChatId(channelId);
        if (ownerChatId == null) {
            LOGGER.warn(String.format(NO_USER_FOUND, channelId));
            return;
        }
        if (!postingRateLimiter.allowRequest(ownerChatId)) {
            repliesThrottler.throttle(ownerChatId, () ->
                    tgNotificator.sendNotification(ownerChatId, TOO_MANY_API_REQUESTS_MSG)
            );
            return;
        }
        Chat forwardFromChat = postItem.getForwardFromChat();
        if (forwardFromChat != null && forwardFromChat.getId() != channelId) {
            tgNotificator.sendNotification(ownerChatId, AUTHOR_RIGHTS_MSG);
            return;
        }
        if (!userChannelsRepository.isSetAutoposting(ownerChatId, channelId)) {
            return;
        }
        IPostProcessor.Post post;
        try {
            post = downloadPost(postItems);
        } catch (FileIsTooBigException e) {
            tgNotificator.sendNotification(ownerChatId, TG_FILE_IS_TOO_BIG_MGS);
            return;
        } catch (TelegramApiException | URISyntaxException | IOException e) {
            LOGGER.error("Error when downloading post from " + channelId, e);
            tgNotificator.sendNotification(ownerChatId, TG_LOAD_POST_ERROR_MGS);
            return;
        }

        try {
            UserChannels tgChannel = userChannelsRepository.getUserChannel(channelId, ownerChatId);
            if (tgChannel == null || !tgChannel.isAutoposting()) {
                return;
            }
            List<String> messagesToChannelOwner = new ArrayList<>();
            for (ChannelGroup group : channelGroupsRepository.getGroupsForChannel(tgChannel.getChannelId())) {
                String accessToken = group.getAccessToken();
                long accountId = group.getAccountId();

                if (accessToken == null) {
                    tgNotificator.sendNotification(ownerChatId, CHANNEL_INFO_ERROR);
                    continue;
                }

                String message;
                switch (group.getSocialMedia()) {
                    case OK -> message = okPostProcessor.processPostInChannel(
                            post,
                            ownerChatId, group.getGroupId(), accountId,
                            accessToken,
                            new TokenExpirationHandler(group, accountId)
                    );
                    case VK -> message = vkPostProcessor.processPostInChannel(post, ownerChatId,
                            group.getGroupId(), accountId, accessToken);
                    default -> {
                        LOGGER.error(String.format("Social media not found: %s",
                                group.getSocialMedia()));
                        message = ERROR_POST_MSG + group.getGroupId();
                    }
                }
                messagesToChannelOwner.add(message);
            }
            String aggregatedMessages = aggregateMessages(messagesToChannelOwner);
            sendNotificationIfEnabled(ownerChatId, channelId, aggregatedMessages);
        } catch (RuntimeException e) {
            LOGGER.error("Error when handling post in " + channelId, e);
            tgNotificator.sendNotification(ownerChatId, UNEXPECTED_ERROR_MSG + e);
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent") //checked with postItem.hasPhoto()
    private IPostProcessor.Post downloadPost(List<Message> postItems) throws TelegramApiException, URISyntaxException,
            IOException {
        List<File> videos = new ArrayList<>();
        List<File> photos = new ArrayList<>();
        List<File> animations = new ArrayList<>();
        List<File> documents = new ArrayList<>();
        List<MessageEntity> textLinks = new ArrayList<>();
        String text = null;
        Poll poll = null;

        for (Message postItem : postItems) {
            if (postItem.hasPhoto()) {
                PhotoSize photoSize = postItem.getPhoto().stream()
                        .max(Comparator.comparingInt(PhotoSize::getFileSize)).get();
                File file = tgContentManager.download(photoSize);
                photos.add(file);
            }
            if (postItem.hasVideo()) {
                File file = tgContentManager.download(postItem.getVideo());
                videos.add(file);
            }
            if (postItem.hasAnimation()) {
                Video animation = TgContentManager.toVideo(postItem.getAnimation());
                File file = tgContentManager.download(animation);
                animations.add(file);
            }
            if (postItem.hasDocument()) {
                File file = tgContentManager.download(postItem.getDocument());
                documents.add(file);
            }
            if (postItem.hasPoll()) {
                poll = postItem.getPoll();
            }
            if (postItem.getCaption() != null && !postItem.getCaption().isEmpty()) {
                text = postItem.getCaption();
                List<MessageEntity> captionEntities = postItem.getCaptionEntities();
                if (captionEntities != null && !captionEntities.isEmpty()) {
                    for (MessageEntity entity : captionEntities) {
                        if (entity.getType().equals(EntityType.TEXTLINK)) {
                            textLinks.add(entity);
                        }
                    }
                }
            }
            if (postItem.hasText() && !postItem.getText().isEmpty()) {
                text = postItem.getText();
                if (postItem.hasEntities()) {
                    List<MessageEntity> entities = postItem.getEntities();
                    for (MessageEntity entity : entities) {
                        if (entity.getType().equals(EntityType.TEXTLINK)) {
                            textLinks.add(entity);
                        }
                    }
                }
            }
        }

        return new IPostProcessor.Post(videos, photos, animations, documents, textLinks, text, poll);
    }

    private static String aggregateMessages(List<String> messagesToChannelOwner) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String message : messagesToChannelOwner) {
            stringBuilder.append(message);
            stringBuilder.append("\n\n");
        }
        return stringBuilder.toString();
    }

    private void sendNotificationIfEnabled(long userChatId, long channelId, String message) {
        if (userChannelsRepository.isSetNotification(userChatId, channelId)) {
            tgNotificator.sendNotification(userChatId, message);
        }
    }

    public class TokenExpirationHandler {
        private final ChannelGroup group;
        private final long accountId;

        private TokenExpirationHandler(ChannelGroup group, long accountId) {
            this.group = group;
            this.accountId = accountId;
        }

        /**
         *
         * @return refreshed access token
         */
        public String handleTokenExpiration() throws URISyntaxException, IOException, OkApiException {
            Account account = accountsRepository.getUserAccount(
                    group.getChatId(), accountId, group.getSocialMedia().getName());
            OkAuthorizator.TokenPair tokenPair = okAuthorizator.refreshToken(account.getRefreshToken());
            group.setAccessToken(tokenPair.accessToken());
            account.setAccessToken(tokenPair.accessToken());
            account.setRefreshToken(tokenPair.refreshToken());
            accountsRepository.insertNewAccount(account);
            channelGroupsRepository.insertChannelGroup(group);
            return tokenPair.accessToken();
        }
    }
}
