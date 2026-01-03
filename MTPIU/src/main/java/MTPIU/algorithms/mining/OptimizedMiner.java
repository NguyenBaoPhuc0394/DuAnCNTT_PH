package main.java.MTPIU.algorithms.mining;

import main.java.MTPIU.algorithms.topk.TopKHeap;
import main.java.MTPIU.config.Parameters;
import main.java.MTPIU.core.database.Database;

public class OptimizedMiner extends AbstractMiner {

    public OptimizedMiner(TopKHeap topK, Parameters params, Database database) {
        super(topK, params, database);
    }

    @Override
    protected boolean shouldPruneBranch(double esc, double currentPer) {
        // --- Chiến lược 3: Rectangular Pruning ---
        if (esc < params.getMinSup()) return true;
        if (currentPer > params.getMaxPer()) return true;

        // Nếu Heap chưa đầy (minScore rất thấp), chưa cần cắt tỉa nâng cao
        if (!topK.isFull()) return false;

        double minScore = topK.getMinScore();

        // --- Chiến lược 4: UB_Score Check ---
        // Tính điểm dựa trên trạng thái hiện tại của Node (ESC, CurrentPer)
        double ubScore = calculateEstimatedScore(esc, currentPer);
        if (ubScore < minScore) {
            return true;
        }

        return false;
    }

    // --- Helper tính Score để dùng cho Chiến lược 4 và 5 ---
    // (Sao chép logic tính Score từ TopKHeap để các class con dùng cắt tỉa)
    protected double calculateEstimatedScore(double expSup, double per) {
        double N = this.N;
        if (N <= 1) N = 1;
        
        double normEsup = expSup / N;
        double normPer = 1 - ((per - 1.0) / (N - 1.0));
        if (normPer < 0) normPer = 0;

        return params.getAlpha() * normEsup + params.getBeta() * normPer;
    }
}