package polytech.commands.context;

import polytech.data.domain.Account;
import polytech.data.domain.ChannelGroup;
import polytech.data.domain.CurrentChannel;
import polytech.util.IState;

public interface Context {

    long getCurrentUserChatId();

    IState currentState();

    CurrentChannel currentChannel();

    Account currentAccount();

    ChannelGroup currentGroup();

    void setCurrentState(IState state);

    void setCurrentChannel(CurrentChannel channel);

    void setCurrentAccount(Account account);

    void setCurrentGroup(ChannelGroup group);
}
