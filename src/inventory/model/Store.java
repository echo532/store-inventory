package inventory.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class Store {

    public final int id;
    public final String name;


    public Store(int id, String name){
        this.name = name;
        this.id = id;
    }


}
