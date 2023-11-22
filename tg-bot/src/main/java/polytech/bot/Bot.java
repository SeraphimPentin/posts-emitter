package polytech.bot;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polytech.callbacks.CallbacksHandlerHelper;
import polytech.commands.context.Context;
import polytech.commands.context.ContextStorage;
import polytech.keyboards.Keyboard;
import polytech.posting.IPostsProcessor;
import polytech.util.IState;
import polytech.util.State;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration //Real?
@Component
public class Bot extends TelegramLongPollingCommandBot implements TgFileLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bot.class);
    private static final String DEBUG_INFO_TEXT = "Update from ";

    private final String botName;
    private final String botToken;

    @Autowired
    private Collection<IBotCommand> commands;

    @Autowired
    IPostsProcessor postsProcessor;

    @Autowired
    CallbacksHandlerHelper callbacksHandlerHelper;

    @Autowired
    private ContextStorage contextStorage;


    public Bot(
            @Value("${bot.name}") String botName,
            @Value("${bot.token}") String botToken
    ) {
        super();
        this.botName = botName;
        this.botToken = botToken;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onRegister() {
        super.onRegister();
        for (IBotCommand command : commands) {
            register(command);
        }
    }

    @Override
    public void onUpdatesReceived(List<Update> overallUpdates) {
        Map<Boolean, List<Update>> updates = overallUpdates.stream()
                .collect(Collectors.partitioningBy(Update::hasChannelPost));
        boolean channelPosts = true;

        updates.get(!channelPosts).forEach(this::processNonCommandUpdate);
        updates.get(channelPosts).stream()
                .map(Update::getChannelPost)
                .collect(Collectors.groupingBy(Message::getChatId))
                .forEach((channelId, posts) -> postsProcessor.processPostsInChannel(channelId, posts));
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        if (LOGGER.isDebugEnabled()) {
            String s = messageDebugInfo(update.getMessage());
            LOGGER.debug(s);
        }
        Message msg;
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            msg = callbackQuery.getMessage();
            String callbackQueryData = callbackQuery.getData();
            if (callbackQueryData.startsWith("/")) {
                getRegisteredCommand(callbackQueryData.replace("/", ""))
                        .processMessage(this, msg, null);
            } else {
                try {
                    callbacksHandlerHelper.handleCallback(callbackQueryData, msg);
                } catch (TelegramApiException e) {
                    LOGGER.error(String.format("Cannot perform Telegram API operation: %s", e.getMessage()));
                }
            }
            return;
        }

        msg = update.getMessage();

        if (msg == null) {
            LOGGER.warn("Message is null");
            return;
        }

        String messageText = msg.getText();

        State customCommand = messageText.startsWith("/")
                ? State.findState(messageText.replace("/", ""))
                : State.findStateByDescription(messageText);

        if (customCommand != null) {
            IBotCommand command = getRegisteredCommand(customCommand.getIdentifier());
            command.processMessage(this, msg, null);
            return;
        }


        Context context = contextStorage.getByMessage(msg);
        IState currentState = context.currentState();

        if (messageText.equals(Keyboard.GO_BACK_BUTTON_TEXT) && currentState != null) {
            IState previousState = State.getPrevState(currentState);
            if (previousState == null) {
                LOGGER.error("Previous state = null, tmp state = {}", currentState.getIdentifier());
                return;
            }
            getRegisteredCommand(previousState.getIdentifier()).processMessage(this, msg, null);
            return;
        }

        try {
            callbacksHandlerHelper.handleMessageCallback(messageText, context.currentState(), msg);
        } catch (TelegramApiException e) {
            //TODO LOG or not throw
            throw new RuntimeException(e);
        }

    }

    private static String messageDebugInfo(Message message) {
        if (message == null) {
            return "Null message";
        }
        String debugInfo = new ReflectionToStringBuilder(message).toString();
        return DEBUG_INFO_TEXT + message.getChatId() + "\n" + debugInfo;
    }
}
