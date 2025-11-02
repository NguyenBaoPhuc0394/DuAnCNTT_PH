package main.java.mining.algorithm;

import main.java.mining.config.Parameters;
import main.java.mining.topk.TopKHeap;
import main.java.mining.tree.UPFPTree;

public class Miner {
    private TopKHeap topK;
    private Parameters params;

    public void mine(UPFPTree tree) {
        // Duyệt suffix từ header table
        // Xây conditional tree
        // Tính Esup, Eper từ timestamps
        // Pruning: UB_Esup < minSup || LB_Eper > maxExpPer
        // Cập nhật Top-K → tự động tăng minSup
    }
}
