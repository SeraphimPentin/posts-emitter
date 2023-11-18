package polytech.callbacks.typed.handlers;

import org.springframework.stereotype.Component;
import polytech.callbacks.UtilCallbackHandler;
import polytech.callbacks.typed.CallbackParser;
import polytech.callbacks.typed.TypedCallback;
import polytech.callbacks.typed.TypedCallbackHandler;

@Component
public abstract class ATypedCallbackHandler<CB extends TypedCallback>
        extends UtilCallbackHandler<CB>
        implements TypedCallbackHandler<CB> {

    protected CallbackParser<CB> callbackParser;

    @Override
    public CallbackParser<CB> callbackParser() {
        return callbackParser;
    }
}
