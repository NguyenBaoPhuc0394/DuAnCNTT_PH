package MTPIU;


import MTPIU.algorithms.mining.AbstractMiner;
import MTPIU.algorithms.mining.BaselineMiner;
import MTPIU.algorithms.mining.OptimizedMiner;
import MTPIU.algorithms.mining.StandardMiner;
import MTPIU.algorithms.topk.TopKHeap;
import MTPIU.config.Parameters;
import MTPIU.core.database.Database;
import MTPIU.core.database.DatasetLoader;
import MTPIU.core.database.Scanner;
import MTPIU.core.tree.TreeBuilder;
import MTPIU.core.tree.UPFPHeaderTable;
import MTPIU.core.tree.UPFPTree;
import MTPIU.core.utils.PatternWriter;
import MTPIU.core.utils.InputProcessing;

public class Main {
    public static void main(String[] args) {

        Parameters params = InputProcessing.inputHandler();

        long startTime = System.nanoTime(); 

        String pathFile = "data/uncertain/chess.txt";
        Database db = DatasetLoader.loadDatabase(pathFile);
        TopKHeap topKHeap = new TopKHeap(params.getK(), params, db);

        Scanner scanner = new Scanner();
        UPFPHeaderTable headerTable = scanner.scan(db, params, topKHeap);

        TreeBuilder builder = new TreeBuilder();
        UPFPTree tree = builder.buildTree(db, headerTable);

        AbstractMiner miner = new OptimizedMiner(topKHeap, params, db);
        miner.run(tree);

        long endTime = System.nanoTime(); 
        double elapsedTimeInSeconds = (double) (endTime - startTime) / 1_000_000_000.0; 
        PatternWriter.writePatternsToFile(topKHeap.getTopKPatterns(), pathFile, params.getK(), params.getAlpha(), elapsedTimeInSeconds);
        System.out.println("Runtime: "+elapsedTimeInSeconds);
        
    }
}
