package polytech.bot;


import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Configuration
@Component("Bot")

public class Bot extends TelegramLongPollingCommandBot  {

    @Override
    public String getBotUsername() {
        return null;
    }

    @Override
    public void processNonCommandUpdate(Update update) {

    }
}
