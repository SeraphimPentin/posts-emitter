package polytech.callbacks.justmessages.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import polytech.commands.NonCommand;
import polytech.commands.context.Context;
import polytech.data.domain.Account;
import polytech.data.domain.ChannelGroup;
import polytech.data.repositories.ChannelGroupsRepository;
import polytech.datacheck.VkDataCheck;
import polytech.util.IState;
import polytech.util.SocialMedia;
import polytech.util.State;
import polytech.vk.api.VkAuthorizator;

import java.util.Objects;

import static polytech.commands.Command.GROUP_NAME_NOT_FOUND;
import static polytech.commands.impl.AddOkGroup.SAME_SOCIAL_MEDIA_MSG;

@Component
public class AddVkGroupHandler extends NonCommandHandler {
    private static final String GROUP_NOT_FOUND = "Не удалось получить id группы. Попробуйте еще раз.";
    private static final String WRONG_ACCOUNT = """
            Неверный аккаунт %s.
            Пожалуйста, вернитесь в главное меню (%s) и следуйте дальнейшим инструкциям.""";
    private static final String USER_NOT_GROUP_ADMIN = """
            Пользователь не является администратором, модератором или редактором канала.
            Попробуйте еще раз.""";
    public static final String VK_GROUP_ADDED = """
            Группа была успешно добавлена.
            Синхронизируйте группу с Телеграмм-каналом по команде /%s.""";


    @Autowired
    private VkDataCheck vkDataCheck;
    @Autowired
    private ChannelGroupsRepository channelGroupsRepository;

    @Override
    public IState state() {
        return State.AddVkGroup;
    }

    @Override
    protected NonCommand.AnswerPair nonCommandExecute(long chatId, String text, Context context) {
        {
            Account currentAccount = context.currentAccount();
            if (currentAccount == null) {
                return new NonCommand.AnswerPair(String.format(WRONG_ACCOUNT, SocialMedia.VK.getName(),
                        State.MainMenu.getIdentifier()),
                        true);
            }

            for (ChannelGroup smg : channelGroupsRepository
                    .getGroupsForChannel(context.currentChannel().getChannelId())) {
                if (smg.getSocialMedia() == SocialMedia.VK) {
                    return new NonCommand.AnswerPair(String.format(SAME_SOCIAL_MEDIA_MSG, SocialMedia.VK.getName()), true);
                }
            }

            String accessToken = currentAccount.getAccessToken();

            Integer groupId = vkDataCheck.getVkGroupId(new VkAuthorizator.TokenWithId(accessToken,
                    (int) currentAccount.getAccountId()), text);

            if (groupId == null) {
                return new NonCommand.AnswerPair(GROUP_NOT_FOUND, true);
            }

            Boolean isAdmin = vkDataCheck.getIsVkGroupAdmin(new VkAuthorizator.TokenWithId(accessToken,
                    (int) currentAccount.getAccountId()), text);

            if (!isAdmin) {
                return new NonCommand.AnswerPair(USER_NOT_GROUP_ADMIN, true);
            }

            String groupName = vkDataCheck.getVkGroupName(
                    new VkAuthorizator.TokenWithId(currentAccount.getAccessToken(),
                            (int) currentAccount.getAccountId()),
                    groupId
            );

            if (Objects.equals(groupName, null)) {
                return new NonCommand.AnswerPair(GROUP_NAME_NOT_FOUND, true);
            }

            context.setCurrentGroup(new ChannelGroup(
                    currentAccount.getAccessToken(),
                    groupName,
                    currentAccount.getAccountId(),
                    chatId,
                    groupId,
                    SocialMedia.VK.getName()
            ));
            return new NonCommand.AnswerPair(String.format(VK_GROUP_ADDED, State.SyncVkTg.getIdentifier()), false);
        }
    }
}
