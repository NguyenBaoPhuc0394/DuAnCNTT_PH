package main.java.mining.algorithm;

import java.util.ArrayList;
import java.util.List;

// import main.java.mining.config.Parameters;
import main.java.mining.model.Transaction;
import main.java.mining.model.UncertainItem;
import main.java.mining.tree.UPFPHeaderTable;
import main.java.mining.tree.UPFPNode;
import main.java.mining.tree.UPFPTree;

public class TreeBuilder {
    public UPFPTree buildTree(List<Transaction> db, UPFPHeaderTable header) {
        // Sắp xếp transaction theo L-order
        // Tính PIcap
        // Insert vào cây, cập nhật timestamps ở tail-node

        UPFPTree tree = new UPFPTree();
        tree.headerTable.table = header.table;
        tree.headerTable.fList = new ArrayList<>(header.getFlist());
        List<String> fList = header.getFlist();

        for(Transaction t : db){
            List<UncertainItem> sortedItems = new ArrayList<>();
            for(UncertainItem ui : t.items){
                if(fList.contains(ui.getItem())){
                    sortedItems.add(ui);
                }
            }
            sortedItems.sort((a,b) ->{
                int idxA = fList.indexOf(a.getItem());
                int idxB = fList.indexOf(b.getItem());
                return Integer.compare(idxA, idxB);
            });

            if (!sortedItems.isEmpty()) {
                insertTransaction(tree, sortedItems, t.getTimestamp());
            }
        }

        return tree;
    }

    public void insertTransaction(UPFPTree tree, List<UncertainItem> items, int timestamp) {
        UPFPNode current = tree.root;
        double prefixMaxProb = 1.0;

        for (int i = 0; i < items.size(); i++) {
            UncertainItem ui = items.get(i);
            String item = ui.getItem();
            double prob = ui.getProbability();

            // PICap = prob × max của tất cả prob đứng TRƯỚC nó
            double currentPicap = prob * prefixMaxProb;

            UPFPNode child = current.children.get(item);
            if (child == null) {
                child = new UPFPNode(item);
                current.children.put(item, child);
                child.parent = current;

                // nodeLink...
                UPFPHeaderTable.ItemInfo info = tree.headerTable.getItemInfo(item);
                if (info != null) {
                    child.nodeLink = info.firstNode;
                    info.firstNode = child;
                }
            }

            child.expSupCap += currentPicap;  // cộng dồn

            if (i == items.size() - 1 && prob > 0.0) {
                child.timestamps.add(timestamp);
            }

            // CẬP NHẬT prefixMaxProb CHO ITEM TIẾP THEO
            if(i == 0){
                prefixMaxProb = prob;
            }else
                prefixMaxProb = Math.max(prefixMaxProb, prob);

            current = child;
        }
    }
}
