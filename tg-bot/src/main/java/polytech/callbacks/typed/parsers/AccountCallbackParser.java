package polytech.callbacks.typed.parsers;

import org.springframework.stereotype.Component;
import polytech.callbacks.typed.objects.AccountCallback;

import java.util.List;

@Component
public class AccountCallbackParser extends ACallbackParser<AccountCallback> {

    @Override
    protected String toData(AccountCallback callback) {
        return String.join(FIELDS_SEPARATOR,
                String.valueOf(callback.accountId),
                Util.booleanFlag(callback.isClickedForDeletion),
                callback.socialMedia
        );
    }

    @Override
    protected AccountCallback fromText(List<String> data) {
        return new AccountCallback(
                Long.parseLong(data.get(0)),
                Util.booleanFlag(data.get(1)),
                data.get(2)
        );
    }
}
