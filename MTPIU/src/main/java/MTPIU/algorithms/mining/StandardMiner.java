package main.java.MTPIU.algorithms.mining;

import main.java.MTPIU.algorithms.topk.TopKHeap;
import main.java.MTPIU.config.Parameters;
import main.java.MTPIU.core.database.Database;

public class StandardMiner extends AbstractMiner {

    public StandardMiner(TopKHeap topK, Parameters params, Database database) {
        super(topK, params, database);
    }

    @Override
    protected boolean shouldPruneBranch(double esc, double currentPer) {
        // STANDARD: Thêm chiến lược 3 (Rectangular Pruning)
        
        // Lấy ngưỡng động hiện tại từ Heap
        double derivedMinSup = params.getMinSup();
        double derivedMaxPer = params.getMaxPer();
        
        // Nếu Heap đã đầy, derivedMinSup và derivedMaxPer sẽ được cập nhật chặt hơn
        // AbstractMiner/TopKHeap đã lo việc cập nhật giá trị vào params (hoặc ta lấy từ heap)
        // Để an toàn và đồng bộ, ta nên lấy trực tiếp từ TopKHeap nếu params chưa kịp sync,
        // nhưng theo kiến trúc của bạn, params được update khi add.

        // Check 1: Support quá thấp
        if (esc < derivedMinSup) {
            return true;
        }

        // Check 2: Periodicity quá tệ (Periodicity có tính đơn điệu tăng)
        // Nếu node hiện tại đã tệ hơn ngưỡng, con cháu chắc chắn tệ hơn.
        if (currentPer > derivedMaxPer) {
            return true;
        }

        return false;
    }
}
