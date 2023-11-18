package polytech.data.domain;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;
import polytech.util.IState;
import polytech.util.State;
import polytech.util.Substate;

@Table("current_state")
public class CurrentState {
    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, name = "chat_id")
    private final long chatId;

    @Column(value = "state")
    private final String state;

    public CurrentState(Long chatId, String state) {
        this.state = state;
        this.chatId = chatId;
    }

    public long getChatId() {
        return chatId;
    }

    public IState getState() {
        State findState = State.findState(state);
        return findState == null ? Substate.findSubstate(state) : findState;
    }

    @Override
    public String toString() {
        return "CurrentState{"
                + "chatId=" + chatId
                + ", state='" + state + '\''
                + "}";
    }
}
