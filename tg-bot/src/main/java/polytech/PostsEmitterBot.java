package polytech;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import polytech.bot.Bot;

@SpringBootApplication
public class PostsEmitterBot {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostsEmitterBot.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(PostsEmitterBot.class, args);
        Bot bot = context.getBean(Bot.class);
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            LOGGER.error(String.format("Cannot register new bot: %s", e.getMessage()));
        }
    }
}
