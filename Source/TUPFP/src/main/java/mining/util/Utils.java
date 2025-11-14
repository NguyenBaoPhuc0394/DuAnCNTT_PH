package main.java.mining.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import main.java.mining.config.Parameters;
import main.java.mining.model.Transaction;
import main.java.mining.model.UncertainItem;

public class Utils {

    static Scanner sc = new Scanner(System.in);
    
    public static boolean checkInputs(String topKInput, String maxExpPerInput){
        try{
            int K = Integer.parseInt(topKInput);
            double maxExpPer = Double.parseDouble(maxExpPerInput);
            if(K <= 0 || maxExpPer <= 0){
                return false;
            }
        }catch(NumberFormatException e){
            return false;
        }
        return true;
    }

    public static Parameters inputHandler(){
        boolean valid = true;
        String K;
        String maxExpPer;
        do{
            valid = true;
            System.out.print("Please enter Top-K value: ");
            K = sc.nextLine();
            System.out.print("Please enter maxExpPer value: ");
            maxExpPer = sc.nextLine();
            valid = Utils.checkInputs(K, maxExpPer);
        }while(!valid);
        return new Parameters(Integer.parseInt(K), Double.parseDouble(maxExpPer));
    } 

    public static List<Transaction> loadDatabase(String path){
        List<Transaction> result = new ArrayList<>();
        try{
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            while((line = reader.readLine()) != null){
                String[] items = line.split("\\s+");
                List<UncertainItem> uncertainItems = new ArrayList<>();
                for(int i = 1; i<items.length ; i++){
                    UncertainItem uncertainItemsValue = getUncertainItem(items[i]); 
                    if(uncertainItemsValue != null){
                        uncertainItems.add(uncertainItemsValue);
                    }
                }

                result.add(new Transaction(Integer.parseInt(items[0]), Integer.parseInt(items[0]), uncertainItems));
                uncertainItems = null;
                // reader.close();
            }
            reader.close();
        }catch(FileNotFoundException e){
            System.out.println("File not found");
            System.err.println(e);
        }catch(IOException ioe){
            System.out.println("File error");
            System.err.println(ioe);
        }
        // catch(NumberFormatException nfe){
        //     System.out.println("NumberFormatException");
        //     System.err.println(nfe);
        //     // System.out.println(nfe.getMessage());
        // }
        
        return result;
    }

    public static UncertainItem getUncertainItem(String input){
        int idx1 = input.indexOf("(");
        int idx2 = input.lastIndexOf(")");
        String item = input.substring(0, idx1);
        String prop = input.substring(idx1 + 1, idx2);
        // System.out.println(item);
        // System.out.println(prop);
        double propability;
        try{
            propability = Double.parseDouble(prop);
        }catch(NumberFormatException e){
            return null;
        }
        return new UncertainItem(item, propability);
    }

}
