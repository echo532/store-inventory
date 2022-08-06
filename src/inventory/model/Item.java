package inventory.model;

public class Item {

    public final int sku; //up to 11 digits
    public final String upc;
    public final String description;

    public Item(int sku, String upc, String description){
        this.upc = upc;
        this.sku = sku;
        this.description = description;
    }


    public String toString(){
        return "" + sku + " " + upc + " " + description;
    }





}
