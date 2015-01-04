package io.pivotal.example.listener;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.GemFireCache;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.asyncqueue.AsyncEvent;
import com.gemstone.gemfire.cache.asyncqueue.AsyncEventListener;
import com.gemstone.gemfire.pdx.PdxInstance;
import io.pivotal.example.data.Transaction;
import io.pivotal.example.dispatcher.HttpDispatcher;
import org.apache.http.HttpException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Process <code>Transaction</code> event types and send (Http.PUT) the endpoint region on a different
 * distributed system. If sending events fails store on <code>ERROR_REGION</code> for later processing.
 * @author wmarkito
 */
public class TransactionEventListener implements AsyncEventListener, Declarable {

    static final Logger LOGGER = Logger.getLogger(TransactionEventListener.class.getCanonicalName());
    private String ERROR_REGION = null;
    private String ID_FIELD = null;
    private HttpDispatcher dispatcher = null;

    @Override
    public void init(Properties properties) {
        // error region for reprocessing
        this.ERROR_REGION = properties.getProperty("ERROR_REGION");
        this.ID_FIELD = properties.getProperty("ID_FIELD");

        // REST service endpoint
        dispatcher.setEndpoint(properties.getProperty("ENDPOINT"));
    }

    public TransactionEventListener() {
       dispatcher = new HttpDispatcher();
    }

    public boolean processEvents(List<AsyncEvent> events) {
        List<PdxInstance> pendingBatch = new ArrayList<>();
        List<String> pendingKeys = new ArrayList<>();

        LOGGER.fine(String.format("Batch size: %d", events.size()));

        try {
            for (AsyncEvent event : events) {
                PdxInstance txEvent = (PdxInstance) event.getDeserializedValue();

                if (isEventValid(txEvent)) {
                    pendingBatch.add(txEvent);
                    // keys need to be converted to string
                    pendingKeys.add(String.valueOf(txEvent.getField(ID_FIELD)));
                } else {
                    // invalid events will be 'just' ignored/dropped
                    LOGGER.severe(String.format("Dropping unrecognized event: %s", txEvent.getClassName()));
                }
            }

            if (pendingBatch.size() > 0) {
                processTransactionBatch(pendingBatch, pendingKeys);
            } else {
                LOGGER.info("No items to process.");
            }

        } catch (Exception ex) {
            // if all fails, send to a temporary error/dead letter region to process later
            processErrorBatch(pendingBatch);
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
//        } finally {
//            // clear batch
//            pendingBatch.clear();
//        }

        // always return true to avoid queue loop
        return true;
    }

    /**
     *
     * @param pendingBatch
     * @param pendingKeys
     * @throws IOException
     */
    private void processTransactionBatch(final List<PdxInstance> pendingBatch, final List<String> pendingKeys) throws IOException {
            final String payload = dispatcher.toJSONList(pendingBatch);
            String keys = pendingKeys.toString();
            // remove [ ] and spaces from list - TODO: replace with StringUtils.join
            keys = keys.substring(1,keys.length()-1).replaceAll(" ","");

            LOGGER.finest(String.format("Payload: ---- %n %s %n----", payload));
            LOGGER.finest(String.format("Keys: %s", keys));

            try {

                if (!dispatcher.send(keys, payload)) {
                    throw new HttpException("HTTP batch wasn't successful");
                } else {
                    LOGGER.info(String.format("Batch successfully processed with %d items", pendingBatch.size()));
                }

            } catch (HttpException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);

                // send to a temporary error/dead letter region to be processed later
                if (payload != null)
                    processErrorBatch(payload);
            }
    }

    /**
     * Store JSON Message on <code>ERROR_REGION</code>
     * @param message
     */
    private void processErrorBatch(final String message) {
        try {
            GemFireCache gemFireCache = CacheFactory.getAnyInstance();
            Region region = gemFireCache.getRegion(ERROR_REGION);
            String id = String.format("%d-%s", System.currentTimeMillis(), gemFireCache.getName());

            region.put(id, message);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE
                  ,String.format("Error to store failed message. Error: %s %n Message: %n ---- %s  ---- %n", ex.getMessage(), message)
                  ,ex);
        }
    }

    /**
     * Convert pdxInstance to JSON List and store on <code>ERROR_REGION</code>
     * @param messages
     */
    private void processErrorBatch(List<PdxInstance> messages) {
            String message = dispatcher.toJSONList(messages);
            processErrorBatch(message);
    }

    /**
     * check PDXInstance className with business object
     * @param event
     * @return
     */
    private boolean isEventValid(PdxInstance event) {
        return ( (event != null) && (event.getClassName().equals(Transaction.class.getCanonicalName())) );
    }

    @Override
    public void close() {
        // closing TX listener
        LOGGER.info("Closing TransactionEventListener...");
    }
}
