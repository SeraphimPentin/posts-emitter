package polytech.commands.context;

import polytech.data.domain.Account;
import polytech.data.domain.ChannelGroup;
import polytech.data.domain.CurrentChannel;
import polytech.util.IState;

public class ContextImpl implements Context {
    private final long currentUserChatId;
    private IState currentState;
    private CurrentChannel currentChannel;
    private Account currentAccount;
    private ChannelGroup currentGroup;

    public ContextImpl(long currentUserChatId) {
        this.currentUserChatId = currentUserChatId;
    }

    @Override
    public long getCurrentUserChatId() {
        return currentUserChatId;
    }

    @Override
    public IState currentState() {
        return currentState;
    }

    @Override
    public void setCurrentState(IState state) {
        this.currentState = state;
    }

    @Override
    public CurrentChannel currentChannel() {
        return currentChannel;
    }

    @Override
    public void setCurrentChannel(CurrentChannel channel) {
        this.currentChannel = channel;
    }

    @Override
    public Account currentAccount() {
        return currentAccount;
    }

    @Override
    public void setCurrentAccount(Account account) {
        this.currentAccount = account;
    }

    @Override
    public ChannelGroup currentGroup() {
        return currentGroup;
    }

    @Override
    public void setCurrentGroup(ChannelGroup group) {
        this.currentGroup = group;
    }
}
