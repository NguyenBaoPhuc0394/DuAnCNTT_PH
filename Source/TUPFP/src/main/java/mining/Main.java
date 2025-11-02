package main.java.mining;


import java.util.List;
import java.util.Map;

import main.java.mining.algorithm.Scanner;
import main.java.mining.config.Parameters;
import main.java.mining.model.Transaction;
import main.java.mining.tree.UPFPHeaderTable;
import main.java.mining.tree.UPFPHeaderTable.ItemInfo;
import main.java.mining.util.Utils;

public class Main {

    static Parameters parameters;

    public static void main(String[] args){
        if(args.length == 2){
            if(Utils.checkInputs(args[0], args[1])){
                parameters = new Parameters(Integer.parseInt(args[0]), Double.parseDouble(args[1]));
            }
            else{
                parameters = Utils.inputHandler();
            }
        }
        else{
            parameters = Utils.inputHandler();
        }

        List<Transaction> db = Utils.loadDatabase("src/resources/Retail_dataset.txt");
        // System.out.println(db.size());


    }

}
