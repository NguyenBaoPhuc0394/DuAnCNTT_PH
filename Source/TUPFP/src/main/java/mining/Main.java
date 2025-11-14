package main.java.mining;


import java.util.List;
import java.util.Map;

import main.java.mining.algorithm.Scanner;
import main.java.mining.algorithm.TreeBuilder;
import main.java.mining.config.Parameters;
import main.java.mining.model.Transaction;
import main.java.mining.tree.UPFPHeaderTable;
import main.java.mining.tree.UPFPHeaderTable.ItemInfo;
import main.java.mining.tree.UPFPTree;
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

        List<Transaction> db = Utils.loadDatabase("src/resources/data.txt");
        // System.out.println(db.size());
        Scanner scanner = new Scanner();
        UPFPHeaderTable header = scanner.scanFirstPass(db, parameters);
        // List<String> fList = header.getFlist(); // L-order
        
        TreeBuilder builder = new TreeBuilder();
        UPFPTree tree = builder.buildTree(db, header);
        tree.setHeaderTable(header); // gán lại để có nodeLink

    }

}
