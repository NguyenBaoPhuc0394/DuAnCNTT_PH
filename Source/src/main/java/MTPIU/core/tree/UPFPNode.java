package MTPIU.core.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Đại diện cho một nút (Node) trong cây UPFP-Tree.
 */
public class UPFPNode {
    private String item; // Tên item.
    private double expSupCap; // Tổng xác suất tích lũy của các path đi qua node này.
    private List<Integer> timestamps; // Danh sách các thời điểm xuất hiện của item tại nhánh này.
    private UPFPNode parent; // Liên kết đến node cha
    private Map<String, UPFPNode> children; // Danh sách node con
    private UPFPNode nodeLink; // Liên kết đến node tiếp theo cùng mang item này trong cây 

    public UPFPNode(String item) {
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

    //#endregion setter & getter

    @Override
    public String toString() {
        return item + "(" + expSupCap + ")";
    }
}
