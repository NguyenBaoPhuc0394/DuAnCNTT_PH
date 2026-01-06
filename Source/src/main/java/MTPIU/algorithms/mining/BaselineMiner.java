package MTPIU.algorithms.mining;

import MTPIU.algorithms.topk.TopKHeap;
import MTPIU.config.Parameters;
import MTPIU.core.database.Database;

/**
 * BaselineMiner: Thuật toán cơ sở KHÔNG áp dụng các chiến lược cắt tỉa nâng cao.
 */
public class BaselineMiner extends AbstractMiner {

    public BaselineMiner(TopKHeap topK, Parameters params, Database db) {
        super(topK, params, db);
    }

    /**
     * Luôn trả về false để vô hiệu hóa việc cắt tỉa nhánh.
     * Thuật toán sẽ buộc phải duyệt và tính toán chính xác cho mọi nhánh.
     */
    @Override
    protected boolean shouldPruneBranch(double esc, double currentPer) {
        return false; 
    }
}