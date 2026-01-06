package MTPIU.core.tree;

/**
 * Cấu trúc dữ liệu cây UPFP (Uncertain Periodic Frequent Pattern Tree).
 * Class này đóng gói Node gốc (Root) và Header Table.
 * Nó được sử dụng cho cả Global Tree (cây gốc) và Conditional Trees (cây con đệ quy).
 */
public class UPFPTree {
    public final UPFPNode root; // Node gốc của cây (khởi tạo là null)
    public UPFPHeaderTable headerTable; // Bảng Header quản lý các item và liên kết ngang

    public UPFPTree(){
        this.root = new UPFPNode(null);
        this.headerTable = new UPFPHeaderTable();
    }

}
