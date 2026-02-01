package TKPIU.algorithms.mining;

import TKPIU.algorithms.topk.TopKHeap;
import TKPIU.config.Parameters;
import TKPIU.core.database.Database;

/**
 * Thuật toán này so sánh riêng lẻ từng chỉ số (Support và Periodicity) với các ngưỡng biên (Thresholds) hiện tại của Top-K Heap.
 */
public class StandardMiner extends AbstractMiner {

    public StandardMiner(TopKHeap topK, Parameters params, Database database) {
        super(topK, params, database);
    }

    @Override
    protected boolean shouldPruneBranch(double esc, double currentPer) {
        // Lấy ngưỡng động hiện tại 
        double derivedMinSup = params.getMinSup();
        double derivedMaxPer = params.getMaxPer();
        
        // Nếu ESC (Upper Bound Support) nhỏ hơn support thấp nhất trong Top-K
        // -> Pattern này và các patterns mở rộng của nó không bao giờ đủ độ phổ biến.
        if (esc < derivedMinSup) {
            return true;
        }

        // Periodicity có tính chất đơn điệu tăng:
        // Các tập mở rộng luôn có giá trị độ đo Periodicity >= tập hiện tại (tệ hơn hoặc bằng).
        // Nếu currentPer đã lớn hơn ngưỡng MaxPer cho phép thì cắt tỉa.
        if (currentPer > derivedMaxPer) {
            return true;
        }

        return false;
    }
}
