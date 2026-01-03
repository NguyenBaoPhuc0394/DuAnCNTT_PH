package main.java.MTPIU.core.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UPFPHeaderTable {
    /// table đại diện cho headerTable, lưu tên item và thông tin của nó, phục vụ cho quá trình xây cây
    /// Flist là danh sách các item đủ điều kiện về tính định kỳ qua đợt quét CSDL đầu tiên, sắp xếp giảm dần theo Esup của item
    private Map<String, ItemInfo> table = new HashMap<>();
    private List<String> fList = new ArrayList<>();

    /// class lưu thông tin của item trong headerTable
    public static class ItemInfo {
        public double expSup;
        public double periodicity;
        public UPFPNode firstNode;
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

    // public void sortFlist(){
    //     this.fList = new ArrayList<>(table.keySet());
    //     this.fList.sort((a,b) -> Double.compare(table.get(b).expSup, table.get(a).expSup));
    // }

    public ItemInfo addItem(String item, double expSup) {
        ItemInfo info = new ItemInfo();
        info.expSup = expSup;
        info.periodicity = 0.0;
        info.firstNode = null;
        table.put(item, info);
        return info;
    }

}
