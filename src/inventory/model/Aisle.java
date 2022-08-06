package inventory.model;

import java.util.Iterator;
import java.util.List;

public class Aisle implements Iterable<Section>{
    String name;
    List<Section> sections;

    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Aisle(){

    }

    @Override
    public Iterator<Section> iterator() {
        return sections.iterator();
    }
}
