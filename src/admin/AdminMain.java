package admin;

import inventory.db.MySqlCon;
import inventory.db.ReturnValue;
import inventory.model.InventoryItem;
import inventory.model.Item;
import inventory.model.Location;
import inventory.model.Store;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AdminMain {

    private static Store store = null;

    public static void main(String[] args) throws IOException {

        if(!MySqlCon.connect()){
            System.out.println("Unable to connect");
            System.exit(-1);
        }

        Scanner sc = new Scanner(System.in);
        while(true){
            System.out.println("Choose your action:");
            System.out.println("1. Admin");
            System.out.println("2. Store Manager");
            System.out.println("3. Store Employee");
            System.out.println("0. quit");
            System.out.print("> ");
            String s = sc.nextLine();

            switch (s){
                case "1":
                    //add a password function
                    adminCommands(sc);
                    return;
                case "2":

                    //incomplete
                    managerCommands(sc);
                    break;
                case "3":
                    employeeCommands(sc);

                    break;
                case "0":
                    return;
                default:
                    System.out.println("unknown inputs");
                    break;
            }
        }
    }

    public static void adminCommands(Scanner sc){
        while(true){
            System.out.println("Choose your action:");
            System.out.println("1. Add Store");
            System.out.println("2. List Stores");
            System.out.println("3. Remove Store");
            System.out.println("4. Add Store Item");
            System.out.println("5. Remove Store Item");
            System.out.println("0. quit");
            System.out.print("> ");
            String s = sc.nextLine();

            switch (s){
                case "1":
                    addStore(sc);
                    break;
                case "2":
                    printStores();
                    break;
                case "3":
                    System.out.println("not yet implemented");
                    break;
                case "4":
                    addItem(sc);
                    break;
                case "5":
                    System.out.println("not yet implemented");
                    break;
                case "0":
                    return;
                default:
                    System.out.println("unknown inputs");
                    break;
            }
        }
    }


    public static void managerCommands(Scanner sc){
        if(store == null){
            store = setStore(sc);
            if(store == null){
                System.out.println("Store could not be assigned.");
                return;
            }
        }

        System.out.println("Your store is " + store.name);

        System.out.println("Choose your action:");
        System.out.println("1. Find item in store");
        System.out.println("2. Create a store plan");
        System.out.println("3. Stock item");
        System.out.println("4. Sweep store");
        String s = sc.nextLine();
        switch(s){
            case "1":
                findItem(sc);
                break;
            case "2":
                createPlan(sc);
                break;
            case "3":
                stockItem(sc);
                break;
            case "4":
                sweepStore(sc);
                break;

        }






    }

    private static void sweepStore(Scanner sc) {

        ReturnValue<List<Item>> tempList = MySqlCon.sweepStore();
        System.out.println(tempList.description);
        for(Item item : tempList.value){
            System.out.println(item.sku);
        }
    }

    public static void employeeCommands(Scanner sc){
        if(store == null){
            store = setStore(sc);
            if(store == null){
                System.out.println("Store could not be assigned.");
                return;
            }
        }

        System.out.println("Your store is " + store.name);

        System.out.println("Choose your action:");
        System.out.println("1. sell item");

        String s = sc.nextLine();

        while(true){
            switch(s){
                case "1":
                    sellItem(sc);
                    break;

            }




        }






    }

    private static void sellItem(Scanner sc) {

        System.out.println("Enter item sku");

        String s = sc.nextLine();

        int sku = Integer.parseInt(s);

        ReturnValue<Integer> itemSold = MySqlCon.removeItemFromInventory(sku);
        System.out.println(itemSold.description);
        if(itemSold.value == 0 && itemSold.isSuccess() == false){
            //check in overstock
            itemSold = MySqlCon.removeItemFromOverstock(sku);
            System.out.println(itemSold.description);


        }


    }

    public static Store setStore(Scanner sc){
        System.out.println("Choose your action:");
        System.out.println("1. Find Store by Name");
        System.out.println("2. Find Store by Number");
        System.out.println("3. Back");
        System.out.print(">");

        String s = sc.nextLine();

        ReturnValue<Store> store;
        switch (s){
            case "1":
                //add a password function
                System.out.println("Enter your store name");
                String name = sc.nextLine();
                if(name.isEmpty()){
                    return null;
                }
                store = MySqlCon.getStore(name);
                return store.value;

            case "2":
                while(true) {

                    System.out.println("Enter your store number");
                    String tempId = sc.nextLine();
                    if(tempId.isEmpty()){
                        return null;
                    }
                    try {
                        int id = Integer.parseInt(tempId);
                        store = MySqlCon.getStore(id);
                        return store.value;

                    } catch (NumberFormatException e) {
                        System.out.println("Input must be a valid integer.");
                    }
                }

            case "3":
                return null;
            default:
                System.out.println("unknown inputs");
                break;
        }

        return null;

    }

    private static boolean addStore(Scanner sc){


            System.out.println("Enter the store name:");
            System.out.print(">");
            String s = sc.nextLine();
            if(s.isEmpty()){

                System.out.println("no input");
                return false;
            }

            ReturnValue<Store> store = MySqlCon.addStore(s);
            if(!store.isSuccess()){
                System.out.println("store could not be created: " + store.description);
                return false;
            }
            System.out.println("Congratulations! Store ID " + store.value.id + " with name " + store.value.name + " has been created!");
            return true;


    }

    private static boolean printStores(){

        ResultSet rs = MySqlCon.listStores();
        System.out.println("List of stores:");

        try {

            while (rs.next()) {


                String name = rs.getString("name");
                int id = rs.getInt("id");
                System.out.print("Store id: " + id + " Store name: " + name + "\n");


            }
            System.out.println("");
            return true;
        } catch(SQLException se){
            return false;
        }
    }

    private static boolean addItem(Scanner sc){
        System.out.println("Enter the item upc:");
        System.out.print(">");
        String upc = sc.nextLine();
        if(upc.isEmpty()){

            System.out.println("no input");
            return false;
        }

        System.out.println("Enter the item description:");
        System.out.print(">");
        String description = sc.nextLine();
        if(description.isEmpty()){

            System.out.println("no input");
            return false;
        }

        ReturnValue<Item> item = MySqlCon.addItem(upc, description);
        if(!item.isSuccess()){
            System.out.println(item.description);
            return false;
        }
        System.out.println("Congratulations! Item sku " + item.value.sku + " with upc " + item.value.upc + " with description " + item.value.description + " has been created!");
        return true;
    }

    private static boolean createPlan(Scanner sc){
        //sku, aisle, section, shelf

        System.out.println("Enter the sku of the item you want to place in the Plan:");
        System.out.println("SKU:");
        int sku = Integer.parseInt(sc.nextLine());
        ReturnValue<Boolean> value = MySqlCon.itemTypeExists(sku);
        if(value.value == false){
            System.out.println(value.description);
            return false;
        }
        System.out.println("Enter location in form of: AISLE, SECTION, SHELF:");
        String location = sc.nextLine();
        String[] locations = location.split(",");

        System.out.println("Finally, enter the maximum quantity intended for this item:");
        int maxQuantity = Integer.parseInt(sc.nextLine()); //dangerous, fix in post


        ReturnValue<Location> loc = MySqlCon.findOrCreateLocation(store, locations[0], locations[1], locations[2]);
        ReturnValue<Boolean> plan = MySqlCon.insertPlan(sku, loc.value, maxQuantity);


        System.out.println(plan.description);
        return plan.value;



    }

    private static void findItem(Scanner sc){

        System.out.println("Enter the sku number of the item you wish to find");
        String s = sc.nextLine();

        ReturnValue<Location> location = MySqlCon.findItem(Integer.parseInt(s), store);
        if(!location.isSuccess()){
            System.out.println("The item could not be found in your store.");
            return;
        }
        System.out.println("Item Location: " + location.value.toString());
    }

    private static void stockItem(Scanner sc){
        System.out.println("Enter the sku number of the item you wish to stock");
        String s = sc.nextLine();
        int sku = Integer.parseInt(s);
        ReturnValue<Location> location = MySqlCon.findItem(sku, store);
        if(!location.isSuccess()){
            System.out.println("The item could not be found in your store.");
            return;
        }
        System.out.println("Item Location " + location.value.toString());
        ReturnValue<InventoryItem> inventoryItem = MySqlCon.getInventoryInfo(sku);
        int emptyValue = inventoryItem.value.getMaxQuantity() - inventoryItem.value.getQuantity();
        if(emptyValue == 0){
            System.out.println("The Item will be placed in overstock.");
            System.out.println("Place Item? Y/N");
            s = sc.nextLine();
            if(s.equals("Y")){
                MySqlCon.placeInOverstock(sku);

            }else{
                return;
            }
        }else{
            System.out.println("The Item will be placed in the shelf. " + emptyValue + " slots will be left.");
            System.out.println("Place Item? Y/N");
            s = sc.nextLine();
            if(s.equals("Y")){
                MySqlCon.placeOnShelf(sku);

            }else{
                return;
            }

        }




    }


}
