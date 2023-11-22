package polytech.commands.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polytech.commands.Command;
import polytech.commands.context.Context;
import polytech.data.domain.UserChannels;
import polytech.data.repositories.UserChannelsRepository;
import polytech.util.Emojis;
import polytech.util.IState;
import polytech.util.State;

import java.util.ArrayList;
import java.util.List;

@Component
public class TgChannelsList extends Command {
    private static final String TG_CHANNELS_LIST_MSG = """
            Список добавленных Телеграмм-каналов.
            Нажмите на Телеграмм-канал, чтобы выбрать определенный.
            Для удаления Телеграмм-канала нажмите 'Удалить' справа от канала.""";
    private static final String NO_TG_CHANNELS = """
            Список добавленных Телеграмм-каналов пуст.
            Пожалуйста, добавьте хотя бы один канал.""";
    private static final String GET_TELEGRAM_CHANNEL = "tg_channel %s %d";
    private static final String DELETE_TELEGRAM_CHANNEL = "tg_channel %s %d";
    private static final int ROWS_COUNT = 2;
    private static final List<String> KEYBOARD_COMMANDS = List.of(
            State.AddTgChannel.getDescription(),
            State.MainMenu.getDescription()
    );

    @Autowired
    private UserChannelsRepository userChannelsRepository;


    @Override
    public IState state() {
        return State.TgChannelsList;
    }

    @Override
    protected void doExecute(AbsSender absSender, User user, Chat chat, Context context) {
        List<UserChannels> channels = userChannelsRepository.getUserChannels(chat.getId());
        if (channels != null && !channels.isEmpty()) {
            sendAnswerWithInlineKeyboard(
                    absSender,
                    chat.getId(),
                    TG_CHANNELS_LIST_MSG,
                    channels.size(),
                    getUserTgChannelsArray(channels),
                    loggingInfo(user.getUserName()));
        } else {
            sendAnswerWithReplyKeyboard(
                    absSender,
                    chat.getId(),
                    NO_TG_CHANNELS,
                    ROWS_COUNT,
                    KEYBOARD_COMMANDS,
                    loggingInfo(user.getUserName()));
        }
    }

    private static List<String> getUserTgChannelsArray(List<UserChannels> channels) {
        List<String> buttons = new ArrayList<>(channels.size() * 4);
        for (UserChannels channel : channels) {
            String telegramChannelUsername = channel.getChannelUsername();
            Long telegramChannelId = channel.getChannelId();
            buttons.add(telegramChannelUsername);
            buttons.add(String.format(GET_TELEGRAM_CHANNEL, telegramChannelId, 0));
            buttons.add(Emojis.TRASH + DELETE_MESSAGE);
            buttons.add(String.format(DELETE_TELEGRAM_CHANNEL, telegramChannelId, 1));
        }
        return buttons;
    }
}
