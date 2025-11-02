package main.java.mining.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UPFPNode {
    String item;
    double expSupCap;           // prefixed item cap
    List<Integer> timestamps;   // ts nơi P(X) >= minProb
    UPFPNode parent;
    Map<String, UPFPNode> children;
    UPFPNode nodeLink;          // cho header table

    public UPFPNode(String item) {
        this.item = item;
        this.expSupCap = 0.0;
        this.timestamps = new ArrayList<>();
        this.children = new HashMap<>();
    }
}
