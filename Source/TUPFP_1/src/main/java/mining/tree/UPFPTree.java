package main.java.mining.tree;

import java.util.ArrayList;

/*
    Được sử dụng trong quá trình xây cây gốc và các conditional tree, lưu lại toàn bộ các thông tin quan trọng của cây để phục vụ cho việc khai thác sau này.
*/
public class UPFPTree {
    public final UPFPNode root;
    public UPFPHeaderTable headerTable;
    // public UPFPNode suffixNode;

    public UPFPTree(){
        this.root = new UPFPNode(null);
        this.headerTable = new UPFPHeaderTable();
    }

    public void attachConditionalHeader(UPFPHeaderTable condHeader) {
        this.headerTable.getTable().clear();
        this.headerTable.getTable().putAll(condHeader.getTable());
        this.headerTable.setfList(new ArrayList<>(condHeader.getFlist()));
    }
}
