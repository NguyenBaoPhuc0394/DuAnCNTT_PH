package main.java.mining.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UPFPHeaderTable {
    private Map<String, ItemInfo> table = new HashMap<>();
    private List<String> fList = new ArrayList<>();

    public static class ItemInfo {
        public double expSup;
        public double periodicity;
        public UPFPNode firstNode;
    }

    public List<String> getFlist(){
        return this.fList;
    }

    public Map<String, ItemInfo> getTable() {
        return table;
    }

    public void setTable(Map<String, ItemInfo> table) {
        this.table = table;
    }

    public void sortFlist(){
        this.fList = new ArrayList<>(table.keySet());
        this.fList.sort((a,b) -> Double.compare(table.get(b).expSup, table.get(a).expSup));
    }

    public ItemInfo getItemInfo(String item){
        return this.table.get(item);
    }

    public ItemInfo addItem(String item, double expSup) {
        ItemInfo info = new ItemInfo();
        info.expSup = expSup;
        info.periodicity = 0.0;
        info.firstNode = null;
        table.put(item, info);
        return info;
    }

    public void setfList(List<String> fList) {
        this.fList = fList;
    }

}
