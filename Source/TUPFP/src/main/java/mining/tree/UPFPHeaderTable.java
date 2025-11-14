package main.java.mining.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
// import java.util.HashMap;
import java.util.Map;

// import main.java.mining.model.UncertainItem;

public class UPFPHeaderTable {
    public final Map<String, ItemInfo> table = new HashMap<>();
    private List<String> fList = new ArrayList<>();

    public static class ItemInfo {
        public double expSup;
        public double periodicity;
        public UPFPNode firstNode;
    }

    public List<String> getFlist(){
        return this.fList;
    }

    public void sortFlist(){
        this.fList = new ArrayList<>(table.keySet());
        this.fList.sort((a,b) -> Double.compare(table.get(b).expSup, table.get(a).expSup));
    }

    public ItemInfo getItemInfo(String item){
        return this.table.get(item);
    }

    // Thêm item, cập nhật nodeLink, sắp xếp theo expSup giảm dần
    
}
