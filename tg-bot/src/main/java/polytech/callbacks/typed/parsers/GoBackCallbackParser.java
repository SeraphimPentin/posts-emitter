package polytech.callbacks.typed.parsers;

import org.springframework.stereotype.Component;
import polytech.callbacks.typed.objects.GoBackCallback;

import java.util.List;

@Component
public class GoBackCallbackParser extends ACallbackParser<GoBackCallback> {

    @Override
    protected String toData(GoBackCallback callback) {
        return "";
    }

    @Override
    protected GoBackCallback fromText(List<String> data) {
        return new GoBackCallback();
    }

}
