package main.java.mining.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UPFPNode {
    public String item;
    public double expSupCap;           // prefixed item cap
    public List<Integer> timestamps;   // ts nơi P(X) >= minProb
    public UPFPNode parent;
    public Map<String, UPFPNode> children;
    public UPFPNode nodeLink;          // cho header table

    public UPFPNode(String item) {
        this.item = item;
        this.expSupCap = 0.0;
        this.timestamps = new ArrayList<>();
        this.children = new HashMap<>();
    }

    @Override
    public String toString() {
        return item + "(" + expSupCap + ")";
    }
}
