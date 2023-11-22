package polytech.commands.impl;

import org.springframework.stereotype.Component;
import polytech.util.IState;
import polytech.util.State;

@Component
public class SyncOkTg extends SyncGroupWithChannel {
    @Override
    public IState state() {
        return State.SyncOkTg;
    }
}
