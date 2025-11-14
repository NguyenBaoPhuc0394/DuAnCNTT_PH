package main.java.mining.tree;

import java.util.List;

import main.java.mining.model.Transaction;

public class UPFPTree {
    public final UPFPNode root;
    public final UPFPHeaderTable headerTable;

    public UPFPTree(){
        this.root = new UPFPNode(null);
        this.headerTable = new UPFPHeaderTable();
    }

    public void setHeaderTable(UPFPHeaderTable headerTable){
        this.headerTable.table.putAll(headerTable.table);
        for(UPFPHeaderTable.ItemInfo info : this.headerTable.table.values()){
            info.firstNode = null;
        }
    }

    // public void insertTransaction(Transaction t, Parameters params);
    // public List<UPFPNode> getPrefixPath(String item);
}
