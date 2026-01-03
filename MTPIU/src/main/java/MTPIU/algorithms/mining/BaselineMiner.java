package main.java.MTPIU.algorithms.mining;

import main.java.MTPIU.algorithms.topk.TopKHeap;
import main.java.MTPIU.config.Parameters;
import main.java.MTPIU.core.database.Database;

public class BaselineMiner extends AbstractMiner {

    public BaselineMiner(TopKHeap topK, Parameters params, Database db) {
        super(topK, params, db);
    }

    @Override
    protected boolean shouldPruneBranch(double esc, double currentPer) {
        // BASELINE: Chỉ dùng chiến lược 1 (MinOcc) và 2 (Score Check)
        // Hai chiến lược này đã được thực hiện trong AbstractMiner.
        // Hàm này trả về false nghĩa là KHÔNG cắt tỉa nhánh nào cả,
        // thuật toán sẽ duyệt hết cây (trừ khi pattern rỗng/không đủ minOcc).
        
        return false; 
    }
}