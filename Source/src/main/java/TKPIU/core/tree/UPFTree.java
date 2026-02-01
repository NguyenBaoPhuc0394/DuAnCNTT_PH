package TKPIU.core.tree;

/**
 * Cấu trúc dữ liệu cây UPFP (Uncertain Periodic Frequent Pattern Tree).
 * Class này đóng gói Node gốc (Root) và Header Table.
 * Nó được sử dụng cho cả Global Tree (cây gốc) và Conditional Trees (cây con đệ quy).
 */
public class UPFTree {
    public final UPFNode root; // Node gốc của cây (khởi tạo là null)
    public UPFList headerTable; // Bảng Header quản lý các item và liên kết ngang

    public UPFTree(){
        this.root = new UPFNode(null);
        this.headerTable = new UPFList();
    }

}
