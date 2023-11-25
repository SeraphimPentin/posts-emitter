package polytech.callbacks.typed.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polytech.bot.MessageSender;
import polytech.callbacks.typed.CallbackType;
import polytech.callbacks.typed.objects.NotificationsCallback;
import polytech.callbacks.typed.parsers.NotificationCallbackParser;
import polytech.commands.context.Context;
import polytech.data.repositories.UserChannelsRepository;
import polytech.util.State;

@Component
public class NotificationsCallbackHandler extends ATypedCallbackHandler<NotificationsCallback> {
    private static final String NOTIFICATIONS_TEXT = "Уведомления %s.";
    private static final String NOTIFICATIONS_ENABLED = "включены";
    private static final String NOTIFICATIONS_DISABLED = "выключены";

    @Autowired
    private UserChannelsRepository userChannelsRepository;
    @Lazy
    @Autowired
    private MessageSender tgNotificator;

    public NotificationsCallbackHandler(NotificationCallbackParser callbackParser) {
        this.callbackParser = callbackParser;
    }

    @Override
    public CallbackType callbackType() {
        return CallbackType.NOTIFICATIONS;
    }

    @Override
    protected void handleCallback(long userChatId, Message message, NotificationsCallback callback, Context context) throws TelegramApiException {
        boolean areEnable = callback.isEnabled;
        userChannelsRepository.setNotification(message.getChatId(), callback.chatId, areEnable);
        tgNotificator.sendNotification(userChatId, String.format(NOTIFICATIONS_TEXT,
                (areEnable ? NOTIFICATIONS_ENABLED : NOTIFICATIONS_DISABLED)));
        deleteLastMessage(message);
        processNextCommand(State.GroupDescription, message, null);
    }
}
