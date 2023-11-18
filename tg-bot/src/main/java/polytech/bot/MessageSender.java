package polytech.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polytech.keyboards.ReplyKeyboard;

import java.util.List;

import static polytech.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

@Component
public class MessageSender {
    private static final Logger logger = LoggerFactory.getLogger(MessageSender.class);

    @Autowired
    ReplyKeyboard replyKeyboard;
    @Autowired
    @Lazy
    AbsSender sender;

    public void sendNotification(long userChatId, String message) {
        sendAnswer(userChatId, message);
    }

    public void sendAnswer(Long chatId, String text) {
        sendAnswer(chatId, null, text);
    }

    public void sendAnswer(Long chatId, String userName, String text) {
        //todo
    }

    public void deleteLastMessage(Message msg) throws TelegramApiException {
        long chatId = msg.getChatId();
        DeleteMessage lastMessage = new DeleteMessage();
        lastMessage.setChatId(chatId);
        lastMessage.setMessageId(msg.getMessageId());
        sender.execute(lastMessage);
    }
}
