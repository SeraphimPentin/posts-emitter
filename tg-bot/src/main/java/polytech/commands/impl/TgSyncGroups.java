package polytech.commands.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polytech.callbacks.typed.objects.GroupCallback;
import polytech.callbacks.typed.parsers.GroupCallbackParser;
import polytech.commands.Command;
import polytech.commands.context.Context;
import polytech.data.domain.ChannelGroup;
import polytech.data.domain.CurrentChannel;
import polytech.data.repositories.ChannelGroupsRepository;
import polytech.util.Emojis;
import polytech.util.IState;
import polytech.util.State;

import java.util.ArrayList;
import java.util.List;

@Component
public class TgSyncGroups extends Command {
    private static final String TG_SYNC_GROUPS_MSG = """
            Список синхронизированных групп.
            Для выбора определенной группы нажмите на нужную группу.
            Для удаления группы нажмите 'Удалить' справа от группы.""";
    private static final String NO_SYNC_GROUPS = """
            Список синхронизированных групп пуст.
            Пожалуйста, вернитесь в описание Телеграмм-канала (/%s) и добавьте хотя бы одну группу.""";
    private static final String GROUP_INFO = "%s (%s)";
    private static final int ROWS_COUNT = 1;
    private static final List<String> KEYBOARD_COMMANDS_IN_ERROR_CASE = List.of(
            State.TgChannelDescription.getDescription());

    @Autowired
    private ChannelGroupsRepository channelGroupsRepository;

    @Autowired
    private GroupCallbackParser groupCallbackParser;

    @Override
    public IState state() {
        return State.TgSyncGroups;
    }

    @Override
    public void doExecute(AbsSender absSender, User user, Chat chat, Context context) {
        CurrentChannel currentChannel = context.currentChannel();

        if (currentChannel != null) {
            List<ChannelGroup> channelGroups =
                    channelGroupsRepository.getGroupsForChannel(currentChannel.getChannelId());

            if (channelGroups != null && !channelGroups.isEmpty()) {
                sendAnswerWithInlineKeyboard(
                        absSender,
                        chat.getId(),
                        TG_SYNC_GROUPS_MSG,
                        channelGroups.size(),
                        getButtonsForTgChannelGroups(channelGroups),
                        loggingInfo(user.getUserName()));
                return;
            }
        }
        sendAnswerWithReplyKeyboard(
                absSender,
                chat.getId(),
                String.format(NO_SYNC_GROUPS, State.TgChannelDescription.getIdentifier()),
                ROWS_COUNT,
                KEYBOARD_COMMANDS_IN_ERROR_CASE,
                loggingInfo(user.getUserName()));
    }

    private List<String> getButtonsForTgChannelGroups(List<ChannelGroup> groups) {
        List<String> buttons = new ArrayList<>(groups.size() * 4);
        for (ChannelGroup group : groups) {
            String socialMediaName = group.getSocialMedia().getName();
            long groupId = group.getGroupId();
            buttons.add(String.format(GROUP_INFO, group.getGroupName(), socialMediaName));
            buttons.add(groupCallbackParser.toText(new GroupCallback(groupId, false, socialMediaName)));
            buttons.add(Emojis.TRASH + DELETE_MESSAGE);
            buttons.add(groupCallbackParser.toText(new GroupCallback(groupId, true, socialMediaName)));
        }
        return buttons;
    }
}
