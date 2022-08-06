package inventory.model;

import java.util.Iterator;
import java.util.List;

public class Section implements Iterable<Shelf>{
    String name;
    List<Shelf> shelves;

    public List<Shelf> getShelves() {
        return shelves;
    }

    public void setShelves(List<Shelf> shelves) {
        this.shelves = shelves;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Section(){

    }

    @Override
    public Iterator<Shelf> iterator() {
        return shelves.iterator();
    }
}
