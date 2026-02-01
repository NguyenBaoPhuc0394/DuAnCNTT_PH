package TKPIU.core.tree;

import TKPIU.core.database.Database;

public interface IUPFTreeBuilder {
    /**
     * Xây dựng UPF-tree từ cơ sở dữ liệu và UPF-list.
     *
     * @param db        Cơ sở dữ liệu giao dịch
     * @param upfList   UPF-list (header table + F-list)
     * @return          UPF-tree đã được xây dựng
     */
    UPFTree buildTree(Database db, UPFList upfList);
}
