package polytech.callbacks.justmessages.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import polytech.commands.NonCommand;
import polytech.commands.context.Context;
import polytech.data.domain.CurrentChannel;
import polytech.data.domain.UserChannels;
import polytech.data.repositories.UserChannelsRepository;
import polytech.telegram.TelegramDataCheck;
import polytech.util.IState;
import polytech.util.State;

@Component
public class AddTgChannelCallbackHandler extends NonCommandHandler {
    private static final String WRONG_CHAT_PARAMETERS = """
            Ошибка получения параметров чата.
            Пожалуйста, проверьте, что ссылка на канал является верной и введите ссылку еще раз.""";
    private static final String SAME_CHANNEL = """
            Телеграмм-канал <b>%s</b> уже был ранее добавлен.
            Пожалуйста, выберите другой Телеграмм-канал и попробуйте снова.""";
    private static final String WRONG_LINK_TELEGRAM = """
            Ссылка неверная.
            Пожалуйста, проверьте, что ссылка на канал является верной и введите ссылку еще раз.""";

    @Autowired
    private TelegramDataCheck telegramDataCheck;
    @Autowired
    private UserChannelsRepository userChannelsRepository;

    @Override
    public IState state() {
        return State.AddTgChannel;
    }

    @Override
    protected NonCommand.AnswerPair nonCommandExecute(long chatId, String text, Context context) {
        String[] split = text.split("/");
        if (split.length < 2) {

            return new NonCommand.AnswerPair(WRONG_LINK_TELEGRAM, true);
        }
        String checkChannelLink = text.split("/")[split.length - 1];

        NonCommand.AnswerPair answer = telegramDataCheck.checkTelegramChannelLink(checkChannelLink);
        if (!answer.getError()) {
            TelegramDataCheck.TelegramChannel channel = telegramDataCheck.getChannel(checkChannelLink);

            if (channel == null) {
                return new NonCommand.AnswerPair(WRONG_CHAT_PARAMETERS, true);
            }

            UserChannels addedChannel = userChannelsRepository.getUserChannel(channel.id(), chatId);

            if (addedChannel != null) {
                return new NonCommand.AnswerPair(String.format(SAME_CHANNEL, addedChannel.getChannelUsername()), true);
            }

            UserChannels newTgChannel = new UserChannels(
                    chatId,
                    channel.id(),
                    channel.title()
            );

            userChannelsRepository.insertUserChannel(newTgChannel);
            context.setCurrentChannel(new CurrentChannel(
                    chatId,
                    newTgChannel.getChannelId(),
                    newTgChannel.getChannelUsername()
            ));
        }
        return answer;
    }
}
