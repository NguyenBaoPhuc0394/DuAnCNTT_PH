package main.java.MTPIU;


import main.java.MTPIU.algorithms.mining.AbstractMiner;
import main.java.MTPIU.algorithms.mining.BaselineMiner;
import main.java.MTPIU.algorithms.mining.OptimizedMiner;
import main.java.MTPIU.algorithms.mining.StandardMiner;
import main.java.MTPIU.algorithms.topk.TopKHeap;
import main.java.MTPIU.config.Parameters;
import main.java.MTPIU.core.database.Database;
import main.java.MTPIU.core.database.DatasetLoader;
import main.java.MTPIU.core.database.Scanner;
import main.java.MTPIU.core.tree.TreeBuilder;
import main.java.MTPIU.core.tree.UPFPHeaderTable;
import main.java.MTPIU.core.tree.UPFPTree;
import main.java.MTPIU.core.utils.PatternWriter;
import main.java.MTPIU.core.utils.Utils;

public class Main {
    public static void main(String[] args) {

        // System.out.println(db.getTransactions().size());
        Parameters params = Utils.inputHandler();

        long startTime = System.nanoTime(); 
        String pathFile = "data/uncertain/chess_uniform.txt";
        Database db = DatasetLoader.loadDatabase(pathFile);
        TopKHeap topKHeap = new TopKHeap(params.getK());
        Scanner scanner = new Scanner();
        UPFPHeaderTable headerTable = scanner.scan(db, params, topKHeap);
        long endTime = System.nanoTime(); 
        double elapsedTimeInSeconds = (double) (endTime - startTime) / 1_000_000_000.0; 
        System.out.println("Scanner: "+elapsedTimeInSeconds);

        startTime = System.nanoTime(); 
        TreeBuilder builder = new TreeBuilder();
        UPFPTree tree = builder.buildTree(db, headerTable);
        endTime = System.nanoTime(); 
        elapsedTimeInSeconds = (double) (endTime - startTime) / 1_000_000_000.0; 
        System.out.println("TreeBuilder: "+elapsedTimeInSeconds);

        startTime = System.nanoTime(); 
        AbstractMiner miner = new OptimizedMiner(topKHeap, params, db);
        miner.run(tree);

        PatternWriter.writePatternsToFile(topKHeap.getTopKPatterns(), pathFile);

        endTime = System.nanoTime(); 
        elapsedTimeInSeconds = (double) (endTime - startTime) / 1_000_000_000.0; 
        System.out.println("Miner:"+elapsedTimeInSeconds);
    }
}
