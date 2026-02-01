package TKPIU.core.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import TKPIU.core.database.Database;
import TKPIU.core.database.Transaction;
import TKPIU.core.database.UncertainItem;

/**
 * Lớp TreeBuilder: Chịu trách nhiệm xây dựng cây UPFP-Tree gốc từ cơ sở dữ liệu.
 */
public class UPFTreeBuilder implements IUPFTreeBuilder{

    /**
     * Hàm chính để xây dựng cây UPFP-Tree từ Database.
     * @param db Danh sách các giao dịch.
     * @param header HeaderTable đã được tạo từ bước Scanner (chứa thông tin ExpSup toàn cục).
     * @return Cây {@link UPFTree} đã hoàn thiện.
     */
    @Override
    public UPFTree buildTree(Database db, UPFList upfList) {

        UPFTree tree = initializeTree(upfList);
        Map<String, Integer> itemRank = buildItemRank(upfList.getFlist());

        for (Transaction transaction : db.getTransactions()) {
            List<UncertainItem> filtered = filterAndSortTransaction(transaction, itemRank);

            if (!filtered.isEmpty()) {
                insertTransaction(tree, filtered, transaction.getTimestamp());
            }
        }

        return tree;
    }

    private UPFTree initializeTree(UPFList upfList) {
        UPFTree tree = new UPFTree();
        tree.headerTable.setHeaderTable(upfList.getHeaderTable());
        tree.headerTable.setfList(new ArrayList<>(upfList.getFlist()));
        return tree;
    }

    private Map<String, Integer> buildItemRank(List<String> fList) {
        Map<String, Integer> rankMap = new HashMap<>();
        for (int i = 0; i < fList.size(); i++) {
            rankMap.put(fList.get(i), i);
        }
        return rankMap;
    }

    private List<UncertainItem> filterAndSortTransaction(Transaction transaction, Map<String, Integer> rankMap) {
        List<UncertainItem> result = new ArrayList<>();

        for (UncertainItem ui : transaction.getItems()) {
            if (rankMap.containsKey(ui.getItem())) {
                result.add(ui);
            }
        }

        result.sort((a, b) -> Integer.compare(rankMap.get(a.getItem()), rankMap.get(b.getItem())));

        return result;
    }

    private void insertTransaction(UPFTree tree, List<UncertainItem> items, int timestamp) {
        UPFNode current = tree.root;
        double prefixMaxProb = 1.0;

        for (int i = 0; i < items.size(); i++) {

            UncertainItem ui = items.get(i);
            double pic = computePIC(ui.getProbability(), prefixMaxProb);

            current = getOrCreateChild(tree, current, ui.getItem());
            updateNodeStatistics(current, pic, timestamp, i == items.size() - 1);

            if(i == 0)
                prefixMaxProb = ui.getProbability();
            else
                prefixMaxProb = updatePrefixMax(prefixMaxProb, ui.getProbability());
        }
    }

    private double computePIC(double prob, double prefixMaxProb) {
        return prob * prefixMaxProb;
    }

    private UPFNode getOrCreateChild(UPFTree tree, UPFNode parent, String item) {

        UPFNode child = parent.getChildren().get(item);

        if (child == null) {
            child = new UPFNode(item);
            parent.getChildren().put(item, child);
            child.setParent(parent);

            UPFList.ItemInfo info = tree.headerTable.getItemInfo(item);

            if (info != null) {
                child.setNodeLink(info.firstNode);
                info.firstNode = child;
            }
        }
        return child;
    }

    private void updateNodeStatistics(UPFNode node, double pic, int timestamp, boolean isTail) {

        node.setExpSupCap(node.getExpSupCap() + pic);

        if (isTail) {
            node.getTimestamps().add(timestamp);
        }
    }

    private double updatePrefixMax(double currentMax, double prob) {
        return Math.max(currentMax, prob);
    }
}



