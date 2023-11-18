package polytech.callbacks.typed.objects;

import polytech.callbacks.typed.CallbackType;
import polytech.callbacks.typed.TypedCallback;

public final class GoBackCallback implements TypedCallback {
    public static final GoBackCallback INSTANCE = new GoBackCallback();

    @Override
    public CallbackType type() {
        return CallbackType.GO_BACK;
    }
}
