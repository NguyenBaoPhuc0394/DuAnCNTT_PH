package main.java.mining;


import java.util.List;
import java.util.Map;

import main.java.mining.algorithm.Miner;
import main.java.mining.algorithm.Scanner;
import main.java.mining.algorithm.TreeBuilder;
import main.java.mining.config.Parameters;
import main.java.mining.model.Pattern;
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

        /// SCANNER
        List<Transaction> db = Utils.loadDatabase("src/resources/Retail_dataset.txt");
        long startTime = System.nanoTime(); // Record the start time 

        Scanner scanner = new Scanner();
        UPFPHeaderTable header = scanner.scanFirstPass(db, parameters);

        long endTime = System.nanoTime(); // Record the end time
        double elapsedTimeInSeconds = (double) (endTime - startTime) / 1_000_000_000.0; // Convert to seconds
        System.out.println("Scanner: " + elapsedTimeInSeconds + " seconds");
        
        /// TREE BUILDER
        startTime = System.nanoTime();

        TreeBuilder builder = new TreeBuilder();
        UPFPTree tree = builder.buildTree(db, header);
        // tree.attachHeaderTable(header);

        endTime = System.nanoTime(); // Record the end time
        elapsedTimeInSeconds = (double) (endTime - startTime) / 1_000_000_000.0; // Convert to seconds
        System.out.println("TreeBuilder: " + elapsedTimeInSeconds + " seconds");

        // for(var i : header.table.keySet()){
        //     System.out.println(tree.headerTable.table.get(i).firstNode==null);
        // }

        /// MINER
        startTime = System.nanoTime();

        Miner miner = new Miner(parameters);
        List<Pattern> result = miner.mine(tree); 
        System.out.println("Top-" + parameters.getK() + " patterns:");
        result.forEach(System.out::println);

        endTime = System.nanoTime(); // Record the end time
        elapsedTimeInSeconds = (double) (endTime - startTime) / 1_000_000_000.0; // Convert to seconds
        System.out.println("Miner: " + elapsedTimeInSeconds + " seconds");

    }

}
