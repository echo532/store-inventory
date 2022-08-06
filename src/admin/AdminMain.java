package admin;

import inventory.db.MySqlCon;
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
                    System.out.println("not yet implemented");
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

            Store store = MySqlCon.addStore(s);
            if(store == null){
                System.out.println("store could not be created.");
                return false;
            }
            System.out.println("Congratulations! Store ID " + store.id + " with name " + store.name + " has been created!");
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
}
