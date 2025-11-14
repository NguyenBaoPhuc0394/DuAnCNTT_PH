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
        List<String> fList = header.getFlist();

        for(Transaction t : db){
            List<UncertainItem> sortedItems = new ArrayList<>();
            for(UncertainItem ui : t.items){
                if(fList.contains(ui.item)){
                    sortedItems.add(ui);
                }
            }
            sortedItems.sort((a,b) ->{
                int idxA = fList.indexOf(a.item);
                int idxB = fList.indexOf(b.item);
                return Integer.compare(idxA, idxB);
            });

            if (!sortedItems.isEmpty()) {
                insertTransaction(tree, sortedItems, t.timestamp, 1.0);
            }
        }

        return tree;
    }

    private void insertTransaction(UPFPTree tree, List<UncertainItem> items, int timestamp, double picap){
        UPFPNode current = tree.root;

        for(int i = 0; i < items.size(); i++){
            UncertainItem ui = items.get(i);
            String item = ui.item;
            double prob = ui.probability;

            double currentPicap = prob * picap;

            UPFPNode child = current.children.get(item);
            if(child == null){
                child = new UPFPNode(item);
                current.children.put(item, child);
                child.parent = current;

                UPFPHeaderTable.ItemInfo info = tree.headerTable.getItemInfo(item);
                if (info != null) {
                    child.nodeLink = info.firstNode;
                    info.firstNode = child;
                }
            }

            child.expSupCap += currentPicap;

            if (i == items.size() - 1 && prob > 0.0) {
                child.timestamps.add(timestamp);
            }

            picap = Math.max(picap, prob);

            current = child;
        }
    }
}
