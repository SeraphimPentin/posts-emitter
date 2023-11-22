package polytech.callbacks.justmessages.handlers;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polytech.callbacks.UtilCallbackHandler;
import polytech.callbacks.justmessages.MessageCallbackHandler;
import polytech.callbacks.justmessages.SomeMessage;
import polytech.commands.NonCommand;
import polytech.commands.context.Context;

//TODO GET RID OF REMAINS OF THE NONCOMMAND class
public abstract class NonCommandHandler extends UtilCallbackHandler<SomeMessage> implements MessageCallbackHandler {
    protected abstract NonCommand.AnswerPair nonCommandExecute(long chatId, String text, Context context);

    @Override
    protected void handleCallback(long userChatId, Message message, SomeMessage callback, Context context) throws TelegramApiException {
        NonCommand.AnswerPair answerPair = nonCommandExecute(userChatId, callback.text, context);
        sendAnswer(userChatId, getUserName(message), answerPair.getAnswer());
    }
}
