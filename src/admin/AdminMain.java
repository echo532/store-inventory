package admin;

import inventory.db.MySqlCon;
import inventory.db.ReturnValue;
import inventory.model.Item;
import inventory.model.Location;
import inventory.model.Store;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;

public class AdminMain {

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
            System.out.println("0. quit");
            System.out.print("> ");
            String s = sc.nextLine();

            switch (s){
                case "1":
                    //add a password function
                    adminCommands(sc);
                    return;
                case "2":
                    System.out.println("Enter your store number");
                    s = sc.nextLine();
                    //incomplete
                    printStores();
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
}
