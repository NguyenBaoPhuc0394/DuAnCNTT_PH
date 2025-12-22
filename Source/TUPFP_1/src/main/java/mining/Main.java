package main.java.mining;


import java.util.List;

import main.java.mining.algorithm.Miner;
import main.java.mining.algorithm.Scanner;
import main.java.mining.algorithm.TreeBuilder;
import main.java.mining.config.Parameters;
import main.java.mining.model.Pattern;
import main.java.mining.model.Transaction;
import main.java.mining.tree.UPFPHeaderTable;
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

        //#region SCANNER
        List<Transaction> db = Utils.loadDatabase("src/resources/Retail_dataset.txt");
        long startTime = System.nanoTime(); 

        Scanner scanner = new Scanner();
        UPFPHeaderTable header = scanner.scanFirstPass(db, parameters);

        long endTime = System.nanoTime(); 
        double elapsedTimeInSeconds = (double) (endTime - startTime) / 1_000_000_000.0; 
        System.out.println("Scanner: " + elapsedTimeInSeconds + " seconds");
        //#endregion
        
        //#region TREE BUILDER
        startTime = System.nanoTime();

        TreeBuilder builder = new TreeBuilder();
        UPFPTree tree = builder.buildTree(db, header);

        endTime = System.nanoTime(); 
        elapsedTimeInSeconds = (double) (endTime - startTime) / 1_000_000_000.0; 
        System.out.println("TreeBuilder: " + elapsedTimeInSeconds + " seconds");
        //#endregion

        //#region MINER
        startTime = System.nanoTime();

        Miner miner = new Miner(parameters);
        List<Pattern> result = miner.mine(tree); 
        System.out.println("Top-" + parameters.getK() + " patterns:");
        result.forEach(System.out::println);

        endTime = System.nanoTime(); 
        elapsedTimeInSeconds = (double) (endTime - startTime) / 1_000_000_000.0; 
        System.out.println("Miner: " + elapsedTimeInSeconds + " seconds");
        //#endregion
    }

}
