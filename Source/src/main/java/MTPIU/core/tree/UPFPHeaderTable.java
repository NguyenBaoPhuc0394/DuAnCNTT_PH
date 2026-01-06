package MTPIU.core.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Bảng Header (Header Table) quản lý danh sách các item xuất hiện trong cây.
 * 1. Lưu trữ thông tin thống kê (Support, Periodicity) của từng item.
 * 2. Cung cấp điểm bắt đầu (entry point) để duyệt các node-link (duyệt ngang).
 * 3. Quản lý thứ tự duyệt khai phá (F-List).
 */
public class UPFPHeaderTable {
    // table đại diện cho headerTable, lưu tên item và thông tin của nó, phục vụ cho quá trình xây cây
    private Map<String, ItemInfo> table = new HashMap<>();
    // Flist là danh sách các item đủ điều kiện về tính định kỳ qua đợt quét CSDL đầu tiên, sắp xếp giảm dần theo ExpSup của item
    private List<String> fList = new ArrayList<>();

    /// class lưu thông tin của item trong headerTable
    public static class ItemInfo {
        public double expSup;
        public double periodicity;
        public UPFPNode firstNode; // Con trỏ đến node đầu tiên chứa item này trong cây (đầu danh sách liên kết)
    }

    //#region setter & getter

    public List<String> getFlist(){
        return this.fList;
    }

    public Map<String, ItemInfo> getTable() {
        return table;
    }

    public void setTable(Map<String, ItemInfo> table) {
        this.table = table;
    }

    public ItemInfo getItemInfo(String item){
        return this.table.get(item);
    }

    public void setfList(List<String> fList) {
        this.fList = fList;
    }

    //#endregion 

}
