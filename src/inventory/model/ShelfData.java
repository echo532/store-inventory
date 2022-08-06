package inventory.model;

public class ShelfData {

    int quantity;
    int maxQuantity;

    Location location;

    public ShelfData(){

    }

    public String toString(){
        return "{" + quantity + " " + maxQuantity + " " + location + "}";
    }

    public ShelfData(int quantity, int maxQuantity){
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

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }





}
