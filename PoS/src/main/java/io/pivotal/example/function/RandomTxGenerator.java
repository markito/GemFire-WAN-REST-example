package io.pivotal.example.function;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.GemFireCache;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.FunctionAdapter;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.distributed.DistributedMember;
import io.pivotal.example.data.Transaction;

import java.util.*;
import java.util.logging.Logger;

/**
 * GemFire function that creates random <code>Transaction</code> objects to simulate traffic on
 * each point of sale
 * @author wmarkito
 */
public class RandomTxGenerator extends FunctionAdapter implements Declarable {

    static final Logger LOGGER = Logger.getLogger(RandomTxGenerator.class.getName());

    private static final String FUNCTION_ID = "RandomTxGenerator";
    private static final String REGION = "transaction";

    private static final Random random = new Random();
    private GemFireCache gemFireCache;

    public RandomTxGenerator() {
            gemFireCache = CacheFactory.getAnyInstance();
    }

    public void init(Properties prop) {

    }

    /**
     * Populate with random transactions in batches
     *
     * @param functionContext
     */
    public void execute(FunctionContext functionContext) {

        int batchSize = Integer.parseInt( ((String[]) functionContext.getArguments())[0] );

        if (batchSize <= 0)
            throw new IllegalArgumentException("Function argument is batch size and should be > 0");

        Region<String, Transaction> region = gemFireCache.getRegion(REGION);

        DistributedMember member = gemFireCache.getDistributedSystem().getDistributedMember();

        LOGGER.info(String.format("##### Running at %s ...", member.getId()));

        Map<String, Transaction> transactionBatch = generateTransactions(batchSize);

        LOGGER.info(String.format("##### Generating %d transaction entries.", transactionBatch.size()));
        region.putAll(transactionBatch);
    }

    /**
     * Generate "random" <code>Transaction</code> batches for given size
     * For validation purposes only, its *not meant for production* ready randomness or performance testing
     * @param batchSize
     * @return
     */
    private Map<String, Transaction> generateTransactions(int batchSize) {

        Map<String, Transaction> transactionBatch = new HashMap<>(batchSize);
        UUID uuid = UUID.randomUUID();

        // transaction batch
        for (int i = 0; i < batchSize; i++) {
            Transaction tx = new Transaction();

            List<Transaction.TransactionItem> itemList = new ArrayList<>();

//            // items list
            for (int j=0; j < random.nextInt(5); j++) {
//                // transaction item
                  String itemID = random.nextInt(100) + "";
                  Transaction.TransactionItem txItem = new Transaction.TransactionItem(random.nextDouble(), itemID);
                  itemList.add(txItem);
            }
            tx.setItems(itemList);

            tx.setId(String.format("%s%s",random.nextInt(200000), uuid.getMostSignificantBits()));
            tx.setOperation("operation-1");
            tx.setPointOfSaleId(random.nextInt(4));
            tx.setSequenceNumber(i);

            transactionBatch.put(tx.getId(), tx);
        }

        return transactionBatch;
    }

    public boolean hasResult() {
        return false;
    }

    public boolean isHA() {
        return false;
    }

    public String getId() {
        return FUNCTION_ID;
    }
}
