package MTPIU.experiment;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import MTPIU.algorithms.mining.*;
import MTPIU.algorithms.topk.TopKHeap;
import MTPIU.config.Parameters;
import MTPIU.core.database.Database;
import MTPIU.core.database.DatasetLoader;
import MTPIU.core.database.Scanner;
import MTPIU.core.tree.TreeBuilder;
import MTPIU.core.tree.UPFPHeaderTable;
import MTPIU.core.tree.UPFPTree;
import MTPIU.core.utils.MemoryLogger;

public class ExperimentRunner {

    private static final String DATASET_PATH = "data/uncertain/";
    private static final String RESULT_FILE = "data/output/experiment_results_final.csv";
    private static final List<String> ALGORITHMS = Arrays.asList("Baseline", "Standard", "Optimized");

    public static void main(String[] args) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RESULT_FILE))) {
            writer.write("Experiment_Type,Dataset,Algorithm,K,Alpha,Beta,Runtime(ms),Memory(MB)\n");
            List<DatasetConfig> configs = Arrays.asList(
                new DatasetConfig("Foodmart", "foodmart.txt", new int[]{500, 700, 900, 1100}, 500),
                new DatasetConfig("T10I4D100K", "T10I4D100K.txt", new int[]{750, 1000, 1250, 1500}, 1500),
                new DatasetConfig("Mushroom", "mushrooms.txt", new int[]{50, 75, 100, 125}, 400),
                new DatasetConfig("Chess", "chess.txt", new int[]{40, 50, 60, 70}, 80)
            );

            System.out.println("=== Start Experiment ===");
            // --- VÒNG LẶP QUA TỪNG DATASET ---
            for (DatasetConfig config : configs) {
                System.out.println("\nProcessing Dataset: " + config.name);
                
                Database db;
                try {
                    db = DatasetLoader.loadDatabase(DATASET_PATH + config.fileName);
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
                double[] alphaValues = {0.2, 0.4, 0.6, 0.8};

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
        MemoryLogger.getInstance().reset();

        // 4. CHẠY VÀ ĐO THỜI GIAN
        long startTime = System.currentTimeMillis();
        
        Scanner scanner = new Scanner();
        UPFPHeaderTable headerTable = scanner.scan(db, params, topK);

        TreeBuilder builder = new TreeBuilder();
        UPFPTree tree = builder.buildTree(db, headerTable);

        miner.run(tree);
        
        long endTime = System.currentTimeMillis();

        // 5. Thu thập kết quả
        long runtime = endTime - startTime;
        double maxMemory = MemoryLogger.getInstance().getMaxMemory();

        System.out.printf("      -> %-10s | K=%-4d | A=%.1f | Time: %5d ms | Mem: %6.2f MB\n", 
                          algName, k, alpha, runtime, maxMemory);

        // 7. Ghi file CSV
        // Format: Experiment_Type, Dataset, Algorithm, K, Alpha, Beta, Runtime, Memory
        writer.write(String.format("%s,%s,%s,%d,%.1f,%.1f,%d,%.2f\n", 
                     expType, datasetName, algName, k, alpha, params.getBeta(), runtime, maxMemory));
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
