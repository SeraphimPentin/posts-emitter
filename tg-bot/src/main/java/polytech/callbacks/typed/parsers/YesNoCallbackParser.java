package polytech.callbacks.typed.parsers;

import org.springframework.stereotype.Component;
import polytech.callbacks.typed.objects.YesNoCallback;

import java.util.List;

@Component
public class YesNoCallbackParser extends ACallbackParser<YesNoCallback> {

    @Override
    protected String toData(YesNoCallback callback) {
        return Util.booleanFlag(callback.yes);
    }

    @Override
    protected YesNoCallback fromText(List<String> data) {
        return new YesNoCallback(Util.booleanFlag(data.get(0)));
    }

}
