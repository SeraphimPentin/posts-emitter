package polytech.callbacks.typed.objects;

import polytech.callbacks.typed.CallbackType;
import polytech.callbacks.typed.TypedCallback;

public final class NotificationsCallback implements TypedCallback {
    public final long chatId;
    public final boolean isEnabled;

    public NotificationsCallback(long chatId, boolean isEnabled) {
        this.chatId = chatId;
        this.isEnabled = isEnabled;
    }

    @Override
    public CallbackType type() {
        return CallbackType.NOTIFICATIONS;
    }
}
