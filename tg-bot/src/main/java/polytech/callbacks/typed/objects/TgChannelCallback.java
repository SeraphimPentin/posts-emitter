package polytech.callbacks.typed.objects;

import polytech.callbacks.typed.CallbackType;
import polytech.callbacks.typed.TypedCallback;

public final class TgChannelCallback implements TypedCallback {
    public final long channelId;
    public final boolean isClickedForDeletion;

    public TgChannelCallback(long channelId, boolean isClickedForDeletion) {
        this.channelId = channelId;
        this.isClickedForDeletion = isClickedForDeletion;
    }

    @Override
    public CallbackType type() {
        return CallbackType.TG_CHANNEL_CHOSEN;
    }
}
