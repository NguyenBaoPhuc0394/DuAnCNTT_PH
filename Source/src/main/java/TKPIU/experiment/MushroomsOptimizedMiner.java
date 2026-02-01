package TKPIU.experiment;

import TKPIU.algorithms.mining.AbstractMiner;
import TKPIU.algorithms.mining.OptimizedMiner;
import TKPIU.algorithms.topk.TopKHeap;
import TKPIU.config.Parameters;
import TKPIU.core.database.Database;
import TKPIU.core.io.DatasetLoader;
import TKPIU.core.io.IDatasetLoader;
import TKPIU.core.tree.UPFListBuilder;
import TKPIU.core.tree.UPFTreeBuilder;
import TKPIU.core.tree.IUPFTreeBuilder;
import TKPIU.core.tree.UPFList;
import TKPIU.core.tree.UPFTree;
import TKPIU.core.utils.InputProcessing;
import TKPIU.core.utils.PatternWriter;

public class MushroomsOptimizedMiner {
    public static void main(String[] args) {

        Parameters params = InputProcessing.inputHandler();
        IDatasetLoader datasetLoader = new DatasetLoader();

        long startTime = System.nanoTime(); 

        String pathFile = "data/uncertain/mushrooms.txt";
        Database db = datasetLoader.loadDatabase(pathFile);
        params.setMaxPer(db.getMaxTs());
        TopKHeap topKHeap = new TopKHeap(params.getK(), params, db);

        UPFListBuilder scanner = new UPFListBuilder();
        UPFList ufList = scanner.buildUPFList(db, params, topKHeap);

        IUPFTreeBuilder builder = new UPFTreeBuilder();
        UPFTree tree = builder.buildTree(db, ufList);

        AbstractMiner miner = new OptimizedMiner(topKHeap, params, db);
        miner.run(tree);

        long endTime = System.nanoTime(); 
        double elapsedTimeInSeconds = (double) (endTime - startTime) / 1_000_000_000.0; 
        PatternWriter.writePatternsToFile(topKHeap.getTopKPatterns(), pathFile, params.getK(), params.getAlpha(), elapsedTimeInSeconds);
        System.out.println("Runtime: "+ elapsedTimeInSeconds);
    }
}
