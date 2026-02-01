package TKPIU.experiment;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import TKPIU.algorithms.mining.*;
import TKPIU.algorithms.topk.TopKHeap;
import TKPIU.config.Parameters;
import TKPIU.core.database.Database;
import TKPIU.core.io.DatasetLoader;
import TKPIU.core.io.IDatasetLoader;
import TKPIU.core.tree.UPFListBuilder;
import TKPIU.core.tree.UPFTreeBuilder;
import TKPIU.core.tree.UPFList;
import TKPIU.core.tree.UPFTree;

public class ExperimentRunner {

    private static final String DATASET_PATH = "data/uncertain/";
    private static final String RESULT_FILE = "data/output/experiment_results_final.csv";
    // private static final List<String> ALGORITHMS = Arrays.asList("Optimized", "Standard", "Baseline");
    private static final List<String> ALGORITHMS = Arrays.asList("Optimized");
    static IDatasetLoader datasetLoader = new DatasetLoader();

    public static void main(String[] args) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RESULT_FILE))) {
            writer.write("Experiment_Type,Dataset,Algorithm,K,Alpha,Beta,Runtime(ms),Memory(MB)\n");
            List<DatasetConfig> configs = Arrays.asList(
                // new DatasetConfig("Retail", "retail.txt", new int[]{200, 300, 400, 500}, 300),
                // new DatasetConfig("T10I4D100K", "T10I4D100K.txt", new int[]{200, 300, 400, 500}, 300)
                new DatasetConfig("Mushroom", "mushrooms.txt", new int[]{75, 100, 125, 150}, 100),
                new DatasetConfig("Chess", "chess.txt", new int[]{20, 25, 30, 35}, 30)
            );

            System.out.println("=== Start Experiment ===");
            // --- VÒNG LẶP QUA TỪNG DATASET ---
            for (DatasetConfig config : configs) {
                System.out.println("\nProcessing Dataset: " + config.name);
                
                Database db;
                try {
                    db = datasetLoader.loadDatabase(DATASET_PATH + config.fileName);
                    System.out.println("Loading complete DB: " + config.fileName);
                } catch (Exception e) {
                    System.err.println("File not found: " + config.fileName);
                    continue; 
                }

                System.out.println("   [Exp 1] Run Varying K...");
                double fixedAlpha = 0.5;
                
                for (int k : config.kValuesToTest) {
                    for (String algName : ALGORITHMS) {
                        runSingleTest(writer, "Varying_K", config.name, algName, db, k, fixedAlpha);
                    }
                }

                System.out.println("   [Exp 2] Run Varying Alpha...");
                int fixedK = config.fixedKForAlphaTest;
                double[] alphaValues = {0.4, 0.5, 0.6, 0.7};

                for (double alpha : alphaValues) {
                    for (String algName : ALGORITHMS) {
                        runSingleTest(writer, "Varying_Alpha", config.name, algName, db, fixedK, alpha);
                    }
                }
            }

            System.out.println("\n=== Experiment complete ===");
            System.out.println("The result are saved in: " + RESULT_FILE);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void runSingleTest(BufferedWriter writer, String expType, String datasetName, 
                                      String algName, Database db, int k, double alpha) throws IOException {
        
        // 1. Chuẩn bị tham số
        Parameters params = new Parameters(k, alpha, 1.0-alpha);
        params.setBeta(1-alpha);

        // 2. Chuẩn bị Heap & Miner
        TopKHeap topK = new TopKHeap(k, params, db); 
        AbstractMiner miner = null;

        switch (algName) {
            case "Baseline":
                miner = new BaselineMiner(topK, params, db);
                break;
            case "Standard":
                miner = new StandardMiner(topK, params, db);
                break;
            case "Optimized":
                miner = new OptimizedMiner(topK, params, db);
                break;
        }

        if (miner == null) return;

        // 3. Clean Memory & Reset Logger trước khi đo
        System.gc();
        try { Thread.sleep(100); } catch (InterruptedException e) {} 
        // MemoryLogger.getInstance().reset();

        // 4. CHẠY VÀ ĐO THỜI GIAN
        long startTime = System.currentTimeMillis();
        
        UPFListBuilder scanner = new UPFListBuilder();
        UPFList ufList = scanner.buildUPFList(db, params, topK);

        UPFTreeBuilder builder = new UPFTreeBuilder();
        UPFTree tree = builder.buildTree(db, ufList);

        miner.run(tree);
        
        long endTime = System.currentTimeMillis();

        // 5. Thu thập kết quả
        long runtime = endTime - startTime;
        // double maxMemory = MemoryLogger.getInstance().getMaxMemory();

        System.out.printf("      -> %-10s | K=%-4d | A=%.1f | Time: %5d ms\n", 
                          algName, k, alpha, runtime);

        // 7. Ghi file CSV
        // Format: Experiment_Type, Dataset, Algorithm, K, Alpha, Beta, Runtime, Memory
        writer.write(String.format("%s,%s,%s,%d,%.1f,%.1f,%d\n", 
                     expType, datasetName, algName, k, alpha, params.getBeta(), runtime));
        writer.flush();
    }

    static class DatasetConfig {
        String name;
        String fileName;
        int[] kValuesToTest;   // Mảng K dùng cho thí nghiệm 1
        int fixedKForAlphaTest; // Giá trị K cố định dùng cho thí nghiệm 2

        public DatasetConfig(String name, String fileName, int[] kValuesToTest, int fixedKForAlphaTest) {
            this.name = name;
            this.fileName = fileName;
            this.kValuesToTest = kValuesToTest;
            this.fixedKForAlphaTest = fixedKForAlphaTest;
        }
    }
}
