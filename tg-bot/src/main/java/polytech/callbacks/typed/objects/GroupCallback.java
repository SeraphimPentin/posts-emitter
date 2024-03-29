package polytech.callbacks.typed.objects;

import polytech.callbacks.typed.CallbackType;
import polytech.callbacks.typed.TypedCallback;

public final class GroupCallback implements TypedCallback {
    public final long groupId;
    public final boolean isClickForDeletion;
    public final String socialMedia;

    public GroupCallback(long groupId, boolean isClickForDeletion, String socialMedia) {
        this.groupId = groupId;
        this.isClickForDeletion = isClickForDeletion;
        this.socialMedia = socialMedia;
    }

    @Override
    public CallbackType type() {
        return CallbackType.GROUP_CHOSEN;
    }
}
