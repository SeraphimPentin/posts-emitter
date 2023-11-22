package polytech.callbacks.typed.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polytech.callbacks.typed.CallbackType;
import polytech.callbacks.typed.objects.GroupCallback;
import polytech.callbacks.typed.parsers.GroupCallbackParser;
import polytech.commands.context.Context;
import polytech.data.domain.ChannelGroup;
import polytech.data.domain.CurrentChannel;
import polytech.data.repositories.ChannelGroupsRepository;
import polytech.util.State;

import java.util.Objects;

@Component
public class GroupCallbackHandler extends ATypedCallbackHandler<GroupCallback> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupCallbackHandler.class);
    @Autowired
    private ChannelGroupsRepository channelGroupsRepository;

    public GroupCallbackHandler(GroupCallbackParser callbackParser) {
        this.callbackParser = callbackParser;
    }

    @Override
    public CallbackType callbackType() {
        return CallbackType.GROUP_CHOSEN;
    }

    @Override
    public void handleCallback(long userChatId, Message message, GroupCallback callback, Context context) throws TelegramApiException {
        if (!callback.isClickForDeletion) {
            ChannelGroup currentGroup = getCurrentGroup(userChatId, callback.groupId, context);
            if (currentGroup != null) {
                context.setCurrentGroup(currentGroup);

                deleteLastMessage(message);
                processNextCommand(State.GroupDescription, message, null);
            } else {
                LOGGER.error(String.format("Cannot find such a social media group id: %s", callback.groupId));
            }
        } else {
            CurrentChannel currentChannel = context.currentChannel();
            String socialMediaName = callback.socialMedia;
            channelGroupsRepository.deleteChannelGroup(currentChannel.getChannelId(), socialMediaName);
            context.setCurrentGroup(null);
            deleteLastMessage(message);
            processNextCommand(State.TgSyncGroups, message, null);
        }
    }

    private ChannelGroup getCurrentGroup(Long chatId, Long groupId, Context context) {
        ChannelGroup currentSocialMedia = null;
        for (ChannelGroup smg : channelGroupsRepository
                .getGroupsForChannel(context.currentChannel().getChannelId())) {
            if (Objects.equals(smg.getGroupId(), groupId)) {
                currentSocialMedia = new ChannelGroup(
                        smg.getAccessToken(),
                        smg.getGroupName(),
                        smg.getAccountId(),
                        smg.getChatId(),
                        smg.getGroupId(),
                        smg.getSocialMedia().getName()
                );
                break;
            }
        }
        return currentSocialMedia;
    }
}
