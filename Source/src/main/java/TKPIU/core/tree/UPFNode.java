package TKPIU.core.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Đại diện cho một nút (Node) trong cây UPFP-Tree.
 */
public class UPFNode {
    private String item; // Tên item.
    private double expSupCap; // Tổng xác suất tích lũy của các path đi qua node này.
    private List<Integer> timestamps; // Danh sách các thời điểm xuất hiện của item tại nhánh này.
    private UPFNode parent; // Liên kết đến node cha
    private Map<String, UPFNode> children; // Danh sách node con
    private UPFNode nodeLink; // Liên kết đến node tiếp theo cùng mang item này trong cây 

    public UPFNode(String item) {
        this.item = item;
        this.expSupCap = 0.0;
        this.timestamps = new ArrayList<>();
        this.children = new HashMap<>();
    }

    //#region setter & getter

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

    public UPFNode getParent() {
        return parent;
    }

    public void setParent(UPFNode parent) {
        this.parent = parent;
    }

    public Map<String, UPFNode> getChildren() {
        return children;
    }

    public void setChildren(Map<String, UPFNode> children) {
        this.children = children;
    }

    public UPFNode getNodeLink() {
        return nodeLink;
    }

    public void setNodeLink(UPFNode nodeLink) {
        this.nodeLink = nodeLink;
    }

    //#endregion setter & getter

    @Override
    public String toString() {
        return item + "(" + expSupCap + ")";
    }
}
