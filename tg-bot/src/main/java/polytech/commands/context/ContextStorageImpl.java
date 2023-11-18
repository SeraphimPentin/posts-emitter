package polytech.commands.context;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ContextStorageImpl implements ContextStorage {
    private final Map<Long, Context> contextStorage = new ConcurrentHashMap<>();

    @Override
    public Context getContext(long userChatId) {
        return contextStorage.computeIfAbsent(userChatId, k -> new ContextImpl(userChatId));
    }

    @Override
    public void setContext(long userChatId, Context context) {
        contextStorage.put(userChatId, context);
    }
}
