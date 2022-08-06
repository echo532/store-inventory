package inventory.model;

import java.util.Iterator;
import java.util.List;

public class Inventory implements Iterable<Item>{

    List<Item> items;

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Inventory(){

    }

    @Override
    public Iterator<Item> iterator() {
        return items.iterator();
    }
}
