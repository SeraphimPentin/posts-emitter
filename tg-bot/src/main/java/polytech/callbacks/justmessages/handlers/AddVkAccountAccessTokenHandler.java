package polytech.callbacks.justmessages.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import polytech.commands.NonCommand;
import polytech.commands.context.Context;
import polytech.datacheck.VkDataCheck;
import polytech.util.IState;
import polytech.util.State;

@Component
public class AddVkAccountAccessTokenHandler extends NonCommandHandler {

    @Autowired
    private VkDataCheck vkDataCheck;

    @Override
    public IState state() {
        return State.AddVkAccount;
    }

    @Override
    protected NonCommand.AnswerPair nonCommandExecute(long chatId, String text, Context context) {
        return vkDataCheck.getVkAccessToken(text, chatId);
    }
}
