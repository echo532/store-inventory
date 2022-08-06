package inventory.db;

public class ActualQuantity {

    public final int quantity;
    public final int maxQuantity;

    public ActualQuantity(int quantity, int maxQuantity){
        this.maxQuantity = maxQuantity;
        this.quantity = quantity;
    }

    public int remainingQuantity() {
        return maxQuantity - quantity;
    }
}
