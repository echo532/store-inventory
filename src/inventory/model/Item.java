package inventory.model;

public class Item {

    public final int sku;
    public final String upc;

    public Item(int sku, String upc){
        this.upc = upc;
        this.sku = sku;
    }


    public String toString(){
        return "" + sku + " " + upc + " ";
    }





}
