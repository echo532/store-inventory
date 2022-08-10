package inventory.model;

public class InventoryItem {

    public final int sku;

    private int quantity;
    private int maxQuantity;

    public InventoryItem(int sku){

        this.sku = sku;

    }

    public InventoryItem(int sku, int quantity, int maxQuantity){
        this.sku = sku;
        this.quantity = quantity;
        this.maxQuantity = maxQuantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getMaxQuantity() {
        return maxQuantity;
    }

    public void setMaxQuantity(int maxQuantity) {
        this.maxQuantity = maxQuantity;
    }
}
