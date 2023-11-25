package polytech.callbacks.justmessages.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polytech.callbacks.UtilCallbackHandler;
import polytech.callbacks.justmessages.MessageCallbackHandler;
import polytech.callbacks.justmessages.SomeMessage;
import polytech.commands.context.Context;
import polytech.data.domain.Account;
import polytech.data.repositories.AccountsRepository;
import polytech.datacheck.OkDataCheck;
import polytech.OkAuthorizator;
import polytech.exceptions.CodeExpiredException;
import polytech.exceptions.OkApiException;
import polytech.util.IState;
import polytech.util.SocialMedia;
import polytech.util.State;

import java.io.IOException;
import java.net.URISyntaxException;

@Component
public class OkAuthCodeCallbackHandler extends UtilCallbackHandler<SomeMessage> implements MessageCallbackHandler {
    public static final String OK_AUTH_STATE_ANSWER = """
            Вы были успешно авторизованы в социальной сети Одноклассники.
            Вы можете посмотреть информацию по аккаунту, если введете команду /%s.""";

    @Autowired
    private OkAuthorizator okAuthorizator;
    @Autowired
    private OkDataCheck okDataCheck;
    @Autowired
    private AccountsRepository accountsRepository;

    @Override
    public void handleCallback(long chatId, Message message, SomeMessage callback, Context context) throws TelegramApiException {
        String okAuthCode = callback.text;
        OkAuthorizator.TokenPair pair;
        try {
            pair = okAuthorizator.getToken(okAuthCode);
        } catch (IOException | URISyntaxException e) {
            return;
            //TODO LOG
        } catch (CodeExpiredException e) {
            //TODO LOG
            return;
        } catch (OkApiException e) {
            //TODO LOG
            return;
        }
        Long userId = okDataCheck.getOKUserId(pair.accessToken());
        if (userId == null) {
            //TODO LOG
            return;
        }
        if (accountsRepository.getUserAccount(chatId, userId, SocialMedia.OK.getName()) != null) {
            //TODO LOG
            return;
        }
        String username = okDataCheck.getOKUsername(pair.accessToken());
        if (username == null || username.isEmpty()) {
            //TODO LOG
            return;
        }
        Account newAccount = new Account(
                chatId,
                SocialMedia.OK.getName(),
                userId,
                username,
                pair.accessToken(),
                pair.refreshToken()
        );
        context.setCurrentAccount(newAccount);
        accountsRepository.insertNewAccount(newAccount);
        sendAnswer(chatId, username, String.format(OK_AUTH_STATE_ANSWER, State.OkAccountDescription.getIdentifier()));
    }

    @Override
    public IState state() {
        return State.AddOkAccount;
    }
}
