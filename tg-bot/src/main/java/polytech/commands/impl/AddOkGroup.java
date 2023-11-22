package polytech.commands.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polytech.commands.Command;
import polytech.commands.context.Context;
import polytech.data.domain.ChannelGroup;
import polytech.data.domain.CurrentChannel;
import polytech.data.repositories.ChannelGroupsRepository;
import polytech.util.IState;
import polytech.util.SocialMedia;
import polytech.util.State;

@Component
public class AddOkGroup extends Command {
    private static final String ADD_OK_GROUP_MSG = """
            Чтобы добавить новую группу, введите в чат ссылку на нее.
            Примеры ссылок:
            https://ok.ru/ok
            https://ok.ru/group57212027273260""";
    public static final String SAME_SOCIAL_MEDIA_MSG = """
            Социальная сеть %s уже была синхронизирована с текущим Телеграмм-каналом.
            Пожалуйста, выберите другую социальную сеть и попробуйте снова.""";

    @Autowired
    private ChannelGroupsRepository channelGroupsRepository;

    @Override
    public IState state() {
        return State.AddOkGroup;
    }

    @Override
    public void doExecute(AbsSender absSender, User user, Chat chat, Context context) {
        CurrentChannel currentChannel = context.currentChannel();
        if (currentChannel != null) {
            for (ChannelGroup smg : channelGroupsRepository.getGroupsForChannel(currentChannel.getChannelId())) {
                if (smg.getSocialMedia() == SocialMedia.OK) {
                    sendAnswerWithOnlyBackButton(
                            absSender,
                            chat.getId(),
                            String.format(SAME_SOCIAL_MEDIA_MSG, SocialMedia.OK.getName()),
                            loggingInfo(user.getUserName()));
                    return;
                }
            }
        }
        sendAnswerWithOnlyBackButton(
                absSender,
                chat.getId(),
                ADD_OK_GROUP_MSG,
                loggingInfo(user.getUserName()));
    }
}
