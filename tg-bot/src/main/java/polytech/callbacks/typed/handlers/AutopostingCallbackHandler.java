package polytech.callbacks.typed.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polytech.bot.MessageSender;
import polytech.callbacks.typed.CallbackType;
import polytech.callbacks.typed.objects.AutopostingCallback;
import polytech.callbacks.typed.parsers.AutopostingCallbackParser;
import polytech.commands.context.Context;
import polytech.data.repositories.UserChannelsRepository;

@Component
public class AutopostingCallbackHandler extends ATypedCallbackHandler<AutopostingCallback> {
    private static final String TURN_ON_NOTIFICATIONS_MSG = "\nВы также можете включить уведомления, чтобы быть в "
            + "курсе автоматически опубликованных записей с помощью команды /notifications";
    private static final String AUTOPOSTING_FUNCTION_ENABLED = "включена";
    private static final String AUTOPOSTING_FUNCTION_DISABLED = "выключена";
    private static final String AUTOPOSTING_ENABLE = "Функция автопостинга %s.";

    @Autowired
    private UserChannelsRepository userChannelsRepository;
    @Lazy
    @Autowired
    MessageSender tgNotificator;

    public AutopostingCallbackHandler(AutopostingCallbackParser callbackParser) {
        this.callbackParser = callbackParser;
    }

    @Override
    public CallbackType callbackType() {
        return CallbackType.AUTOPOSTING;
    }

    @Override
    protected void handleCallback(long userChatId, Message message, AutopostingCallback callback, Context context) throws TelegramApiException {
        String enable = AUTOPOSTING_FUNCTION_ENABLED;
        if (callback.disable()) {
            userChannelsRepository.setAutoposting(userChatId, callback.channelId, false);
            enable = AUTOPOSTING_FUNCTION_DISABLED;
        } else {
            userChannelsRepository.setAutoposting(userChatId, callback.channelId, true);
        }
        deleteLastMessage(message);
        String text = String.format(AUTOPOSTING_ENABLE, enable);
        if (AUTOPOSTING_FUNCTION_ENABLED.equals(enable)) {
            text += TURN_ON_NOTIFICATIONS_MSG;
        }
        tgNotificator.sendNotification(message.getChatId(), text);
    }
}
