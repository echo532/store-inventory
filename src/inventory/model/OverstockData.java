package inventory.model;

public class OverstockData {

    int quantity;

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public OverstockData(){

    }

    public String toString(){
        return "{" + quantity + "}";
    }


}
