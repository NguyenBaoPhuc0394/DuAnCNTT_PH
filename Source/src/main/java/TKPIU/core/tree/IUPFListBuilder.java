package TKPIU.core.tree;

import TKPIU.algorithms.topk.TopKHeap;
import TKPIU.config.Parameters;
import TKPIU.core.database.Database;

public interface IUPFListBuilder {
    /**
     * Xây dựng UPF-list (header table + F-list) từ cơ sở dữ liệu.
     *
     * @param db        Cơ sở dữ liệu giao dịch không chắc chắn
     * @param params    Tham số cấu hình (K, alpha, beta, ...)
     * @param topKHeap  Heap quản lý Top-K pattern
     * @return          UPFList dùng để xây dựng UPF-tree
     */
    UPFList buildUPFList(Database db, Parameters params, TopKHeap topKHeap);
}
