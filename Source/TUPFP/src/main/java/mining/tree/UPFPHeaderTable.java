package main.java.mining.tree;

import java.util.HashMap;
// import java.util.HashMap;
import java.util.Map;

import main.java.mining.model.UncertainItem;

public class UPFPHeaderTable {
    public Map<String, ItemInfo> table = new HashMap<>();

    public static class ItemInfo {
        public double expSup;
        public double periodicity;
        public UPFPNode firstNode;
    }

    // Thêm item, cập nhật nodeLink, sắp xếp theo expSup giảm dần
    
}
