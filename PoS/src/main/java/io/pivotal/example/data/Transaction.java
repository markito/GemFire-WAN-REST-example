package io.pivotal.example.data;

import com.gemstone.gemfire.pdx.PdxReader;
import com.gemstone.gemfire.pdx.PdxSerializable;
import com.gemstone.gemfire.pdx.PdxWriter;

import java.util.List;

/**
 * POJO example to represent business object for example
 * @author wmarkito
 */
public class Transaction implements PdxSerializable {

    private String id;
    private long sequenceNumber;
    private String operation;
    private int pointOfSaleId;
    private List<TransactionItem> items;

    public Transaction()  {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public int getPointOfSaleId() {
        return pointOfSaleId;
    }

    public void setPointOfSaleId(int pointOfSaleId) {
        this.pointOfSaleId = pointOfSaleId;
    }

    public List<TransactionItem> getItems() {
        return items;
    }

    public void setItems(List<TransactionItem> items) {
        this.items = items;
    }


    @Override
    public void toData(PdxWriter pdxWriter) {
        pdxWriter.markIdentityField("id").markIdentityField("pointOfSaleId")
                .writeString("id", id)
                .writeLong("sequenceNumber", sequenceNumber)
                .writeInt("pointOfSaleId", pointOfSaleId)
                .writeString("operation", operation)
                .writeObject("items", items);
    }

    @Override
    public void fromData(PdxReader pdxReader) {

        id = pdxReader.readString("id");
        sequenceNumber = pdxReader.readLong("sequenceNumber");
        pointOfSaleId = pdxReader.readInt("pointOfSaleId");
        operation = pdxReader.readString("operation");
        items = (List<Transaction.TransactionItem>) pdxReader.readObject("items");

    }

    public static class TransactionItem implements PdxSerializable {
        private double amount;
        private String id;

        public TransactionItem() {}

        public TransactionItem(double amount, String id) {
            this.setAmount(amount);
            this.setId(id);
        }

        @Override
        public void toData(PdxWriter pdxWriter) {
            pdxWriter.writeString("id", getId())
                    .writeDouble("amount", getAmount());
        }

        @Override
        public void fromData(PdxReader pdxReader) {
             setId(pdxReader.readString("id"));
             setAmount(pdxReader.readDouble("amount"));
        }


        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Transaction{");
        sb.append("id='").append(id).append('\'');
        sb.append(", sequenceNumber=").append(sequenceNumber);
        sb.append(", operation='").append(operation).append('\'');
        sb.append(", pointOfSaleId=").append(pointOfSaleId);
        sb.append(", items=").append(items);
        sb.append('}');

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transaction that = (Transaction) o;

        if (pointOfSaleId != that.pointOfSaleId) return false;
        if (sequenceNumber != that.sequenceNumber) return false;
        if (!id.equals(that.id)) return false;
        if (items != null ? !items.equals(that.items) : that.items != null) return false;
        if (!operation.equals(that.operation)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (int) (sequenceNumber ^ (sequenceNumber >>> 32));
        result = 31 * result + operation.hashCode();
        result = 31 * result + pointOfSaleId;
        result = 31 * result + (items != null ? items.hashCode() : 0);
        return result;
    }
}
