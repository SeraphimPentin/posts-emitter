package polytech.commands.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polytech.commands.Command;
import polytech.commands.context.Context;
import polytech.OkAuthorizator;
import polytech.util.IState;
import polytech.util.State;

import java.net.URISyntaxException;

@Component
public class AddOkAccount extends Command {
    private static final String OK_AUTH_ANSWER_MSG = """
                    Для авторизации в социальной сети Одноклассники перейдите по ссылке:
                    %s
                    После авторизации скопируйте код авторизации из адресной строки и отправьте его в этот диалог.""";
    private static final Logger LOGGER = LoggerFactory.getLogger(AddOkAccount.class);

    @Override
    public IState state() {
        return State.AddOkAccount;
    }

    @Override
    public void doExecute(AbsSender absSender, User user, Chat chat, Context context) {
        try {
            String messageText = String.format(OK_AUTH_ANSWER_MSG, OkAuthorizator.formAuthorizationUrl());
            sendAnswerWithOnlyBackButton(
                    absSender,
                    chat.getId(),
                    messageText,
                    loggingInfo(user.getUserName()));
        } catch (URISyntaxException e) {
            LOGGER.error(String.format("Cannot form link: %s", e));
        }
    }
}
