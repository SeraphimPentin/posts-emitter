package polytech.callbacks.typed.parsers;

import polytech.callbacks.typed.CallbackParser;
import polytech.callbacks.typed.CallbackType;
import polytech.callbacks.typed.TypedCallback;

import java.util.Arrays;
import java.util.List;

public abstract class ACallbackParser<CB extends TypedCallback> implements CallbackParser<CB> {
    private static final String TYPE_TO_DATA_SEPARATOR = " ";
    protected static final String FIELDS_SEPARATOR = " ";

    protected abstract String toData(CB callback);

    protected abstract CB fromText(List<String> data);

    @Override
    public String toText(CB callback) {
        String callbackData = toData(callback);
        return appendType(callbackData, callback.type());
    }

    @Override
    public CB fromText(String callbackData) {
        List<String> data = Arrays.asList(callbackData.split(FIELDS_SEPARATOR));
        return fromText(data);
    }

    public static String appendType(String callBackData, CallbackType callbackType) {
        return callbackType.stringKey + TYPE_TO_DATA_SEPARATOR + callBackData;
    }

    public static CallbackTypeAndData parseTypeAndData(String callbackString) {
        String[] typeAndData = callbackString.split(TYPE_TO_DATA_SEPARATOR, 2);
        return new CallbackTypeAndData(typeAndData[0], typeAndData[1]);
    }

    public record CallbackTypeAndData(String type, String data) {
    }

}
