package polytech.callbacks.justmessages;

import polytech.callbacks.Callback;

/**
 * Простой вид колбека, инкапсулирующий в себе некоторую текстовую информацию, сообщение
 *
 * @see MessageCallbackHandler
 */
public final class SomeMessage implements Callback {
    public final String text;

    public SomeMessage(String text) {
        this.text = text;
    }
}
