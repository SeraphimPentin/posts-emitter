package polytech.callbacks.justmessages.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import polytech.commands.NonCommand;
import polytech.commands.context.Context;
import polytech.data.domain.Account;
import polytech.data.domain.ChannelGroup;
import polytech.data.repositories.ChannelGroupsRepository;
import polytech.datacheck.OkDataCheck;
import polytech.util.IState;
import polytech.util.SocialMedia;
import polytech.util.State;

import java.util.Objects;

import static polytech.commands.Command.GROUP_NAME_NOT_FOUND;
import static polytech.commands.impl.AddOkGroup.SAME_SOCIAL_MEDIA_MSG;

@Component
public class AddOkGroupHandler extends NonCommandHandler {
    private static final String GROUP_NOT_FOUND = "Не удалось получить id группы. Попробуйте еще раз.";
    private static final String WRONG_ACCOUNT = """
            Неверный аккаунт %s.
            Пожалуйста, вернитесь в главное меню (%s) и следуйте дальнейшим инструкциям.""";

    @Autowired
    private OkDataCheck okDataCheck;
    @Autowired
    private ChannelGroupsRepository channelGroupsRepository;

    @Override
    public IState state() {
        return State.AddOkGroup;
    }

    @Override
    protected NonCommand.AnswerPair nonCommandExecute(long chatId, String text, Context context) {
        {
            Account currentAccount = context.currentAccount();
            if (currentAccount == null) {
                return new NonCommand.AnswerPair(String.format(WRONG_ACCOUNT, SocialMedia.OK.getName(),
                        State.MainMenu.getIdentifier()),
                        true);
            }

            for (ChannelGroup smg : channelGroupsRepository
                    .getGroupsForChannel(context.currentChannel().getChannelId())) {
                if (smg.getSocialMedia() == SocialMedia.OK) {
                    return new NonCommand.AnswerPair(String.format(SAME_SOCIAL_MEDIA_MSG, SocialMedia.OK.getName()), true);
                }
            }

            String accessToken = currentAccount.getAccessToken();

            Long groupId = okDataCheck.getOKGroupId(text, accessToken);

            if (groupId == null) {
                return new NonCommand.AnswerPair(GROUP_NOT_FOUND, true);
            }

            NonCommand.AnswerPair answer = okDataCheck.checkOKGroupAdminRights(accessToken, groupId);

            String groupName = okDataCheck.getOKGroupName(groupId, currentAccount.getAccessToken());

            if (Objects.equals(groupName, null)) {
                return new NonCommand.AnswerPair(GROUP_NAME_NOT_FOUND, true);
            }

            if (!answer.getError()) {
                context.setCurrentGroup(new ChannelGroup(
                        currentAccount.getAccessToken(),
                        groupName,
                        currentAccount.getAccountId(),
                        chatId,
                        groupId,
                        SocialMedia.OK.getName()
                ));
            }

            return answer;
        }
    }
}
