package inventory.db;
import inventory.model.Item;
import inventory.model.Location;
import inventory.model.Store;

import java.sql.*;
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

    public static Store getStore(int id){
        try{
            PreparedStatement stmt =con.prepareStatement("SELECT * from Stores WHERE  id = ?");
            stmt.setInt(1, id);

            ResultSet rs=stmt.executeQuery();
            Store s = null;
            if(rs.next()) {
                String name = rs.getString("name");
                s = new Store(id, name);
                stmt.close();
                return s;


            }
            stmt.close();
            return null;
        } catch(SQLException se){
            return null;
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

    private static Location findExisting(Store store, String aisle, String shelf, String section) throws SQLException {
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


    public static Location findOrCreate(Store store, String aisle, String shelf, String section) {
        try{

            Location loc = findExisting(store, aisle, shelf, section);
            if(loc != null){
                return loc;
            }



            PreparedStatement stmt =con.prepareStatement("insert into Locations (storeid, aisle, shelf, section) VALUES (?, ?, ?, ?)");
            stmt.setInt(1, store.id);
            stmt.setString(2, aisle);
            stmt.setString(3, shelf);
            stmt.setString(4, section);

            int num = stmt.executeUpdate();
            if(num== 0){
                return null;
            }


            return findExisting(store, aisle, shelf, section);



        } catch(SQLException se){
            return null;
        }


    }
}
