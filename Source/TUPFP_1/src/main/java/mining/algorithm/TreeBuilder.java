package main.java.mining.algorithm;

import java.util.ArrayList;
import java.util.List;

import main.java.mining.model.Transaction;
import main.java.mining.model.UncertainItem;
import main.java.mining.tree.UPFPHeaderTable;
import main.java.mining.tree.UPFPNode;
import main.java.mining.tree.UPFPTree;

public class TreeBuilder {
    // Sắp xếp transaction theo L-order
    // Tính PIcap
    // Insert vào cây, cập nhật timestamps ở tail-node
    public UPFPTree buildTree(List<Transaction> db, UPFPHeaderTable header) {

        UPFPTree tree = new UPFPTree();
        tree.headerTable.setTable(header.getTable());
        tree.headerTable.setfList(new ArrayList<>(header.getFlist()));;
        List<String> fList = header.getFlist();

        for(Transaction t : db){
            List<UncertainItem> sortedItems = new ArrayList<>();
            for(UncertainItem ui : t.getItems()){
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
        // Duyệt qua mỗi Transaction và thêm các pattern vào cây (thêm nhánh vào cây)
        UPFPNode current = tree.root;
        double prefixMaxProb = 1.0;

        for (int i = 0; i < items.size(); i++) {
            UncertainItem ui = items.get(i);
            String item = ui.getItem();
            double prob = ui.getProbability();

            // PICap = prob × max của tất cả prob đứng TRƯỚC nó
            double currentPicap = prob * prefixMaxProb;

            UPFPNode child = current.getChildren().get(item);
            if (child == null) {
                child = new UPFPNode(item);
                current.getChildren().put(item, child);
                child.setParent(current);

                // nodeLink
                UPFPHeaderTable.ItemInfo info = tree.headerTable.getItemInfo(item);
                if (info != null) {
                    child.setNodeLink(info.firstNode);
                    info.firstNode = child;
                }
            }

            child.setExpSupCap(child.getExpSupCap() + currentPicap);  // cộng dồn

            if (i == items.size() - 1 && prob > 0.0) {
                child.getTimestamps().add(timestamp);
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
