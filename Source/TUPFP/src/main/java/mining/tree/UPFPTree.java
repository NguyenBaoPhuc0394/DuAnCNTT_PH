package main.java.mining.tree;

import java.util.ArrayList;
import java.util.List;

import main.java.mining.model.Transaction;

public class UPFPTree {
    public final UPFPNode root;
    public UPFPHeaderTable headerTable;
    public UPFPNode suffixNode;

    public UPFPTree(){
        this.root = new UPFPNode(null);
        this.headerTable = new UPFPHeaderTable();
    }

    // DÙNG CHO CÂY GỐC – KHÔNG reset firstNode
    // public void attachHeaderTable(UPFPHeaderTable externalHeader) {
    //     this.headerTable.table.clear();
    //     this.headerTable.table.putAll(externalHeader.table);
    //     this.headerTable.fList = new ArrayList<>(externalHeader.fList);
    //     // firstNode KHÔNG ĐỘNG VÀO → TreeBuilder đã gán đúng rồi
    // }

    // DÙNG CHO CONDITIONAL TREE – CẦN reset firstNode
    public void attachConditionalHeader(UPFPHeaderTable condHeader) {
        this.headerTable.table.clear();
        this.headerTable.table.putAll(condHeader.table);
        this.headerTable.fList = new ArrayList<>(condHeader.fList);
        
        // Reset firstNode vì đây là cây mới
        // for (UPFPHeaderTable.ItemInfo info : this.headerTable.table.values()) {
        //     info.firstNode = null;
        // }
    }
}
