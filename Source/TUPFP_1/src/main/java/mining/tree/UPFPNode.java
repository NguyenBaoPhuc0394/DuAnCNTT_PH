package main.java.mining.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UPFPNode {
    private String item;
    private double expSupCap;           
    private List<Integer> timestamps;   
    private UPFPNode parent;
    private Map<String, UPFPNode> children;
    private UPFPNode nodeLink;          

    public UPFPNode(String item) {
        this.item = item;
        this.expSupCap = 0.0;
        this.timestamps = new ArrayList<>();
        this.children = new HashMap<>();
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public double getExpSupCap() {
        return expSupCap;
    }

    public void setExpSupCap(double expSupCap) {
        this.expSupCap = expSupCap;
    }

    public List<Integer> getTimestamps() {
        return timestamps;
    }

    public void setTimestamps(List<Integer> timestamps) {
        this.timestamps = timestamps;
    }

    public UPFPNode getParent() {
        return parent;
    }

    public void setParent(UPFPNode parent) {
        this.parent = parent;
    }

    public Map<String, UPFPNode> getChildren() {
        return children;
    }

    public void setChildren(Map<String, UPFPNode> children) {
        this.children = children;
    }

    public UPFPNode getNodeLink() {
        return nodeLink;
    }

    public void setNodeLink(UPFPNode nodeLink) {
        this.nodeLink = nodeLink;
    }

    @Override
    public String toString() {
        return item + "(" + expSupCap + ")";
    }
}
