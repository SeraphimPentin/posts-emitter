package polytech.callbacks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.ICommandRegistry;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polytech.bot.MessageSender;
import polytech.commands.context.Context;
import polytech.commands.context.ContextStorage;
import polytech.keyboards.ReplyKeyboard;
import polytech.util.IState;

public abstract class UtilCallbackHandler<CB extends Callback> implements CallbackHandler<CB> {
    @Lazy
    @Autowired
    protected ICommandRegistry commandRegistry;

    @Lazy
    @Autowired
    protected AbsSender sender;

    @Autowired
    protected MessageSender messageSender;

    @Autowired
    protected ContextStorage contextStorage;

    @Autowired
    protected ReplyKeyboard replyKeyboard;

    protected abstract void handleCallback(long userChatId, Message message, CB callback, Context context) throws TelegramApiException;

    @Override
    public void handleCallback(Message message, CB callback) throws TelegramApiException {
        Context context = contextStorage.getByMessage(message);
        Long userChatId = message.getChatId();
        handleCallback(userChatId, message, callback, context);
    }

    protected void processNextCommand(IState state, Message message, String[] args) {
        IBotCommand command = commandRegistry.getRegisteredCommand(state.getIdentifier());
        command.processMessage(sender, message, args);
    }

    protected String getUserName(Message msg) {
        User user = msg.getFrom();
        String userName = user.getUserName();
        return (userName != null) ? userName : String.format("%s %s", user.getLastName(), user.getFirstName());
    }

    protected void sendAnswer(long chatId, String username, String text) {
        messageSender.sendAnswer(chatId, username, text);
    }

    protected void deleteLastMessage(Message msg) throws TelegramApiException {
        messageSender.deleteLastMessage(msg);
    }
}
