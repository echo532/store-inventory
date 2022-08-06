package inventory.db;

public class ReturnValue<E> {

    public final E value;

    public final String description;

    private boolean status;

    public ReturnValue(E value){
        status = true;
        this.value = value;
        description = "";
    }

    public ReturnValue(E value, String description, boolean status){

        this.value = value;
        this.description = description;
        this.status = status;
    }

    public boolean isSuccess(){
        return status;
    }

}
