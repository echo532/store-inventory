package inventory.model;

public class Location {


    public final int id; //key
    public final int storeId;
    public final String aisle;
    public final String section;
    public final String shelf;



    public Location(int id, int storeId, String anAisle, String aSection, String aShelf) {
        this.id = id;
        this.storeId = storeId;
        this.aisle = anAisle;
        this.section = aSection;
        this.shelf = aShelf;
    }



    public String toString(){
        return "aisle: " + aisle + " section: " + section + " shelf: " + shelf;
    }
}
