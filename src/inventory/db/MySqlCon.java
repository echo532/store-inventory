package inventory.db;
import inventory.model.InventoryItem;
import inventory.model.Item;
import inventory.model.Location;
import inventory.model.Store;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;


public class MySqlCon {

    static Connection con;

    static String dbPassword = "dbPassword";
    static String dbUsername = "dbUsername";
    static String dbHostname = "dbHostname";
    static String dbDatabase = "dbDatabase";

    public static int MAX_SKU = 1000000;

//    public static void main(String args[]) throws SQLException {
//        connect();
//        Statement stmt=con.createStatement();
//        ResultSet rs=stmt.executeQuery("select * from Stores");
//        while(rs.next())
//            System.out.println(rs.getInt(1)+"  "+rs.getString(2));
//        stmt.close();
//        stmt = con.createStatement();
//        int num = stmt.executeUpdate("INSERT INTO Item (sku, upc) VALUES (162331, '133234f')");
//        System.out.println(num);
//
//        con.close();
//    }

    public static boolean connect(){

        if(con != null){
            return true;
        }

        String password = System.getenv(dbPassword);
        String username= System.getenv(dbUsername);
        String host = System.getenv(dbHostname);
        String db = System.getenv(dbDatabase);
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":3306/" + db,username,password);
            //here sonoo is database name, root is username and password
            System.out.println("Success");
            return true;

        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static ReturnValue<Store> addStore(String name){
        try{
            PreparedStatement stmt =con.prepareStatement("INSERT INTO Stores (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, name);

            int num = stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();

            if(num==1){
                rs.next();
                int id = rs.getInt(1);
                Store store = new Store(id, name);
                stmt.close();
                return new ReturnValue<>(store);

            }

            //no good solution
            stmt.close();
            return new ReturnValue<>(null, "unable to create store", false);

        } catch(SQLException se){
            return new ReturnValue<>(null, se.getMessage(), false);
        }
    }

    public static ReturnValue<Store> getStore(int id){
        try{
            PreparedStatement stmt =con.prepareStatement("SELECT * from Stores WHERE  id = ?");
            stmt.setInt(1, id);

            ResultSet rs=stmt.executeQuery();
            Store s = null;
            if(rs.next()) {
                String name = rs.getString("name");
                s = new Store(id, name);
                stmt.close();
                return new ReturnValue<>(s);


            }
            stmt.close();
            return new ReturnValue<>(null, "No store with this ID exists", false);
        } catch(SQLException se){
            return new ReturnValue<>(null, "Failure to execute command", false);
        }
    }

    public static ReturnValue<Store> getStore(String name){
        try{
            PreparedStatement stmt =con.prepareStatement("SELECT * from Stores WHERE  name = ?");
            stmt.setString(1, name);

            ResultSet rs=stmt.executeQuery();
            Store s = null;
            if(rs.next()) {
                int id = rs.getInt("id");
                s = new Store(id, name);
                stmt.close();
                return new ReturnValue<>(s);


            }
            stmt.close();
            return new ReturnValue<>(null, "No store with this Name exists", false);
        } catch(SQLException se){
            return new ReturnValue<>(null, "Failure to execute command", false);
        }
    }

    public static ResultSet listStores(){
        try{
            PreparedStatement stmt =con.prepareStatement("SELECT * from Stores");


            ResultSet rs =stmt.executeQuery();

            //stmt.close(); //doesn't work with this statement in
            return rs;

        } catch(SQLException se){
            return null;
        }
    }

    public static ReturnValue<Item> addItem(String upc, String description){


        Random rnd = new Random();
        int tries = 10;
        while(tries > 0){
            try{
                PreparedStatement stmt = con.prepareStatement("INSERT INTO Item(sku, upc, description) VALUES (?, ?, ?)");
                int sku = rnd.nextInt(MAX_SKU);


                stmt.setInt(1, sku);
                stmt.setString(2, upc);
                stmt.setString(3, description);

                int num = stmt.executeUpdate();
                stmt.close();
                if(num == 1){
                    return new ReturnValue<>(new Item(sku, upc, description), Integer.toString(tries), true);
                }

            } catch(SQLException se){
                if(se.getMessage().endsWith("key 'upc'")){
                    return new ReturnValue<>(null, "upc already exists", false);
                }

                tries--;
            }
        }

        return new ReturnValue<>(null, "Unable to create unique item code", false);



    }



    /**
     * place a certain quantity of item on a shelf location at a store
     * if maxQuantity exists, then becomes new maxQuantity
     * if item is already on shelf, then quantity increments BUT may return smaller number if maxQuantity hit
     */
    public static int placeItem(Store store, Item item, Location location, int quantity, Optional<Integer> maxQuantity){
        try{
            ActualQuantity aq = findActualQuantity(store, item, location);
            if(aq != null){
                PreparedStatement stmt =con.prepareStatement("UPDATE Inventory SET quantity = ? WHERE id = ? AND sku = ?");
                int numToAdd = quantity;
                if(quantity <= aq.remainingQuantity()) {
                }else {
                    numToAdd = aq.remainingQuantity();
                }

                stmt.setInt(1, aq.quantity + numToAdd);
                stmt.setInt(2, store.id);
                stmt.setInt(3, item.sku);
                int num = stmt.executeUpdate();
                stmt.close();
                if(num == 0){
                    return 0;
                }
                return quantity;
            }

            //need to add for the first time

            int toSet = quantity;
            if(quantity > maxQuantity.orElse(0)){
                toSet = maxQuantity.orElse(0);
            }
            PreparedStatement stmt =con.prepareStatement("INSERT INTO Inventory (sku, location, quantity, maxQuantity) VALUES (?, ?, ?, ?)");
            stmt.setInt(1, item.sku);
            stmt.setInt(2, location.id);
            stmt.setInt(3, toSet); //
            stmt.setInt(4, maxQuantity.orElse(0)); //hack
            int num = stmt.executeUpdate();
            stmt.close();
            if(num == 0){
                return 0;
            }
            return toSet;


        } catch(SQLException se){
            return 0;
        }

    }

    private static ActualQuantity findActualQuantity(Store store, Item item, Location location) {
        try{

            PreparedStatement stmt =con.prepareStatement("select quantity,maxQuantity from Inventory where sku = ? and location = ?");
            stmt.setInt(1, item.sku);
            stmt.setInt(2, location.id);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                ActualQuantity aq = new ActualQuantity(rs.getInt(1), rs.getInt(2));
                stmt.close();
                return aq;
            }

            stmt.close();
            return null;



        } catch(SQLException se){
            return null;
        }

    }

    private static Location findExistingLocation(Store store, String aisle, String shelf, String section) throws SQLException {
        PreparedStatement stmt =con.prepareStatement("select * from Locations where storeid = ? and aisle = ? and shelf = ? and section = ?");
        stmt.setInt(1, store.id);
        stmt.setString(2, aisle);
        stmt.setString(3, shelf);
        stmt.setString(4, section);
        ResultSet rs = stmt.executeQuery();
        if(rs.next()){
            Location loc = new Location(rs.getInt("id"), rs.getInt("storeid"), rs.getString("aisle"), rs.getString("shelf"), rs.getString("section"));
            stmt.close();
            return loc;
        }

        stmt.close();
        return null;
    }


    public static ReturnValue<Location> findOrCreateLocation(Store store, String aisle, String shelf, String section) {
        try{

            Location loc = findExistingLocation(store, aisle, shelf, section);
            if(loc != null){
                return new ReturnValue<>(loc);
            }



            PreparedStatement stmt =con.prepareStatement("insert into Locations (storeid, aisle, shelf, section) VALUES (?, ?, ?, ?)");
            stmt.setInt(1, store.id);
            stmt.setString(2, aisle);
            stmt.setString(3, shelf);
            stmt.setString(4, section);

            int num = stmt.executeUpdate();
            if(num== 0){
                return new ReturnValue<>(null, "cannot create location", false);
            }


            return new ReturnValue<>(findExistingLocation(store, aisle, shelf, section));



        } catch(SQLException se){
            return new ReturnValue<>(null, se.getMessage(), false);
        }


    }


    public static ReturnValue<Boolean> insertPlan(int sku, Location location, int maxQuantity) {

        try{
            PreparedStatement stmt =con.prepareStatement("insert into Plans (sku, location) VALUES (?, ?)");
            stmt.setInt(1, sku);
            stmt.setInt(2, location.id);

            int num = stmt.executeUpdate();
            if(num== 0){
                return new ReturnValue<>(false, "cannot create plan", false);
            }

            //plan could exist in multiple places, but inventory cannot
            //have to check if inventory already exists for said item
            stmt.close();
            stmt =con.prepareStatement("select * from Inventory where sku=?");
            stmt.setInt(1, sku);

            ResultSet rs = stmt.executeQuery();

            if(!rs.next()){
                stmt.close();
                stmt =con.prepareStatement("insert into Inventory (sku, location, quantity, maxQuantity) VALUES (?, ?, ?, ?)");
                stmt.setInt(1, sku);
                stmt.setInt(2, location.id);
                stmt.setInt(3, 0);
                stmt.setInt(4, maxQuantity);
                num = stmt.executeUpdate();

            }
            stmt.close();


            return new ReturnValue<>(true, "plan successfully created", true);



        } catch(SQLException se){
            return new ReturnValue<>(false, se.getMessage(), false);
        }



    }

    public static ReturnValue<Location> findItem(int sku, Store store){

        //find item location from item.sku -> inventory -> location
        //print out location
        //check max quantity, quantity, determine where it should go
        //if max has been reached, place in overstock
        try{
            PreparedStatement stmt =con.prepareStatement("SELECT * FROM Plans where sku=?");
            stmt.setInt(1, sku);

            ResultSet rs1=stmt.executeQuery();
            int locationId = 0;
            while(rs1.next()){
                locationId = rs1.getInt("location");
            }

            stmt.close();
            stmt =con.prepareStatement("SELECT * FROM Locations where id=?");
            stmt.setInt(1, locationId);
            ResultSet rs2 = stmt.executeQuery();
            String aisle = "";
            String section = "";
            String shelf = "";
            while(rs2.next()){
                aisle = rs2.getString("aisle");
                section = rs2.getString("section");
                shelf = rs2.getString("shelf");
            }

            return new ReturnValue<>(new Location(locationId, store.id, aisle, section, shelf));






        } catch(SQLException se){
            return new ReturnValue<>(null, se.getMessage(), false);
        }


    }

    public static ReturnValue<InventoryItem> getInventoryInfo(int sku){

        try{
            PreparedStatement stmt =con.prepareStatement("SELECT * FROM Inventory where sku=?");
            stmt.setInt(1, sku);

            ResultSet rs=stmt.executeQuery();

            int quantity;
            int maxQuantity;
            while(rs.next()){

                quantity = rs.getInt("quantity");
                maxQuantity = rs.getInt("maxQuantity");
                return new ReturnValue<>(new InventoryItem(sku, quantity, maxQuantity), "found item in inventory", true);

            }

            return new ReturnValue<>(null, "could not find in inventory", false);




        } catch(SQLException se){
            return new ReturnValue<>(null, se.getMessage(), false);
        }

    }

    public static ReturnValue<Boolean> itemTypeExists(int sku){
        try{
            PreparedStatement stmt =con.prepareStatement("SELECT * FROM Item where sku=?");
            stmt.setInt(1, sku);

            ResultSet rs = stmt.executeQuery();

            if(rs.next()){
                return new ReturnValue<>(true, "item found", true);
            }
            return new ReturnValue<>(false, "item does not exist", true);



        } catch(SQLException se){
            return new ReturnValue<>(null, se.getMessage(), false);
        }
    }

    public static ReturnValue<Boolean> placeInOverstock(int sku){
        //check if overstock stack already exists
        try{
            PreparedStatement stmt =con.prepareStatement("SELECT * FROM Overstock where sku=?");
            stmt.setInt(1, sku);

            ResultSet rs = stmt.executeQuery();

            if(rs.next()){
                //existing overstock stack
                int quantity = rs.getInt("quantity");
                quantity++;
                stmt.close();
                stmt = con.prepareStatement("UPDATE Overstock SET quantity=? WHERE sku=?");
                stmt.setInt(1, quantity);
                stmt.setInt(2, sku);
                int num = stmt.executeUpdate();
                if(num!=0){
                    return new ReturnValue<>(true, "item placed in overstock successfully", true);
                }
                return new ReturnValue<>(false, "item could not be placed in overstock", false);


            }
            //overstock does not exist
            stmt.close();
            stmt =con.prepareStatement("insert into Overstock (sku, quantity) VALUES (?, ?)");
            stmt.setInt(1, sku);
            stmt.setInt(2, 1);
            int num = stmt.executeUpdate();
            if(num!=0){
                return new ReturnValue<>(true, "item placed in overstock successfully", true);
            }
            return new ReturnValue<>(false, "item could not be placed in overstock", false);



        } catch(SQLException se){
            return new ReturnValue<>(false, se.getMessage(), false);
        }
    }

    public static ReturnValue<Boolean> placeOnShelf(int sku){
        //check if overstock stack already exists
        try{
            PreparedStatement stmt =con.prepareStatement("SELECT * FROM Inventory where sku=?");
            stmt.setInt(1, sku);

            ResultSet rs = stmt.executeQuery();

            if(rs.next()){

                int quantity = rs.getInt("quantity");
                quantity++;
                stmt.close();
                stmt = con.prepareStatement("UPDATE Inventory SET quantity=? WHERE sku=?");
                stmt.setInt(1, quantity);
                stmt.setInt(2, sku);
                int num = stmt.executeUpdate();
                if(num!=0){
                    return new ReturnValue<>(true, "item placed in overstock successfully", true);
                }
                return new ReturnValue<>(false, "item could not be placed on shelf", false);


            }
            return new ReturnValue<>(false, "item could not be placed on shelf", false);




        } catch(SQLException se){
            return new ReturnValue<>(false, se.getMessage(), false);
        }
    }

    public static ReturnValue<Integer> removeItemFromInventory(int sku) {
        try{
            PreparedStatement stmt =con.prepareStatement("SELECT * FROM Inventory where sku=?");
            stmt.setInt(1, sku);

            ResultSet rs = stmt.executeQuery();

            if(rs.next()){

                int quantity = rs.getInt("quantity");
                if(quantity == 0){
                    return new ReturnValue<>(0, "Item not supposed to be on shelf!", false);
                }
                quantity--;
                stmt.close();
                stmt = con.prepareStatement("UPDATE Inventory SET quantity=? WHERE sku=?");
                stmt.setInt(1, quantity);
                stmt.setInt(2, sku);
                int num = stmt.executeUpdate();
                if(num!=0){
                    return new ReturnValue<>(quantity, "item sold successfully", true);
                }
                return new ReturnValue<>(quantity, "item could not be sold", false);


            }
            return new ReturnValue<>(0, "item could not be found", false);




        } catch(SQLException se){
            return new ReturnValue<>(0, "item could not be found", false);
        }

    }

    public static ReturnValue<Integer> removeItemFromOverstock(int sku) {

        try{
            PreparedStatement stmt =con.prepareStatement("SELECT * FROM Overstock where sku=?");
            stmt.setInt(1, sku);

            ResultSet rs = stmt.executeQuery();

            if(rs.next()){

                int quantity = rs.getInt("quantity");
                if(quantity == 0){
                    return new ReturnValue<>(0, "No items in overstock", false);
                }
                quantity--;
                stmt.close();
                stmt = con.prepareStatement("UPDATE Overstock SET quantity=? WHERE sku=?");
                stmt.setInt(1, quantity);
                stmt.setInt(2, sku);
                int num = stmt.executeUpdate();
                if(num!=0){
                    return new ReturnValue<>(quantity, "item sold successfully (from overstock)", true);
                }
                return new ReturnValue<>(quantity, "item could not be sold", false);


            }
            return new ReturnValue<>(0, "item could not be found", false);




        } catch(SQLException se){
            return new ReturnValue<>(0, "item could not be found", false);
        }
    }

    public static ReturnValue<Item> findItem(int sku){

        try{
            PreparedStatement stmt =con.prepareStatement("SELECT * FROM Item where sku=?");
            stmt.setInt(1, sku);
            ResultSet rs = stmt.executeQuery();

            if(rs.next()) {

                String upc = rs.getString("upc");
                String description = rs.getString("description");

                return new ReturnValue<>(new Item(sku, upc, description), "Item found", true);
            }
            return new ReturnValue<>(null, "no item found", false);

        }catch(SQLException se){
            return new ReturnValue<>(null, "item could not be found", false);
        }

    }

    public static ReturnValue<List<Item>> findEmptyInStore() {
        try{
            PreparedStatement stmt =con.prepareStatement("SELECT * FROM Inventory where quantity=?");
            stmt.setInt(1, 0);

            ResultSet rs = stmt.executeQuery();

            List<Item> emptyShelfList = new ArrayList<>();

            while(rs.next()){

                int sku = rs.getInt("sku");
                ReturnValue<Item> item = findItem(sku);

                emptyShelfList.add(item.value);

            }
            stmt.close();
            return new ReturnValue<>(emptyShelfList, "full list", true);

        } catch(SQLException se){
            return new ReturnValue<>(null, "list could not be made", false);
        }
    }

//    public static ReturnValue<List<Item>> findInvalidInStore() {
//        try{
//            PreparedStatement stmt =con.prepareStatement("SELECT * FROM Inventory");
//            stmt.setInt(1, 0);
//
//            ResultSet rs = stmt.executeQuery();
//
//            List<Item> emptyShelfList = new ArrayList<>();
//
//            while(rs.next()){
//
//                int sku = rs.getInt("sku");
//                ReturnValue<Item> item = findItem(sku);
//
//                emptyShelfList.add(item.value);
//
//            }
//            stmt.close();
//            return new ReturnValue<>(emptyShelfList, "full list", true);
//
//        } catch(SQLException se){
//            return new ReturnValue<>(null, "list could not be made", false);
//        }
//    }



    public static ReturnValue<Boolean> removeItem(int sku) {

        try{
            PreparedStatement stmt =con.prepareStatement("DELETE FROM Item where sku=?");
            stmt.setInt(1, sku);

            int num = stmt.executeUpdate();

            if(num == 0) {
                return new ReturnValue<>(false, "item could not be deleted", false);
            }
            return new ReturnValue<>(true, "item deleted successfully", true);


        } catch(SQLException se){
            return new ReturnValue<>(false, "item could not be found", false);
        }
    }
}
