package io.pivotal.example.listener;

import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;

import java.util.Properties;

/**
 * @author wmarkito
 */
public class SimpleCacheListener extends CacheListenerAdapter implements Declarable {

    public void init(Properties props) {

    }
    @Override
    public void afterCreate(EntryEvent event) {
        logEvent("created", event);
    }

    @Override
    public void afterDestroy(EntryEvent event) {
        logEvent("destroyed", event);
    }

    @Override
    public void afterUpdate(EntryEvent event) {
        logEvent("updated", event);
    }

    private void logEvent(String operation, EntryEvent event) {
//        out.println(String.format("In region [%s] %s key[%s] value [%s]", event.getRegion().getName(), operation, event.getKey(), event.getNewValue()));
//        out.println("JSON = " + JSONFormatter.toJSON( (PdxInstance) event.getNewValue()));
}
}