package TKPIU.algorithms.mining;

import TKPIU.algorithms.topk.TopKHeap;
import TKPIU.config.Parameters;
import TKPIU.core.database.Database;

/**
 * OptimizedMiner: Áp dụng chiến lược cắt tỉa dựa trên cận trên của giá trị Score.
 * Thuật toán tính toán giá trị cận trên Score và so sánh trực tiếp với minScore của Heap.
 */
public class OptimizedMiner extends AbstractMiner {

    public OptimizedMiner(TopKHeap topK, Parameters params, Database database) {
        super(topK, params, database);
    }

    @Override
    protected boolean shouldPruneBranch(double esc, double currentPer) {
        if (esc < params.getMinSup()) return true;
        if (currentPer > params.getMaxPer()) return true;

        // Chỉ cắt tỉa dựa trên Score khi Heap đã đầy.
        if (!topK.isFull()) return false;

        double minScore = topK.getMinScore();

        // Tính toán Upper Bound Score
        // esc: Support tốt nhất có thể (Upper Bound)
        // currentPer: Periodicity tốt nhất có thể (Lower Bound của Periodicity thực tế)
        double ubScore = topK.calculateScore(esc, currentPer);
        // Nếu điểm tiềm năng tối đa vẫn thua điểm thấp nhất trong Top-K thì cắt tỉa
        if (ubScore < minScore) {
            return true;
        }

        return false;
    }
}

