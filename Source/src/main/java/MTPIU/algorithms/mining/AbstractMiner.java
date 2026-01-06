package MTPIU.algorithms.mining;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import MTPIU.algorithms.topk.TopKHeap;
import MTPIU.config.Parameters;
import MTPIU.core.database.Database;
import MTPIU.core.pattern.Pattern;
import MTPIU.core.tree.UPFPHeaderTable;
import MTPIU.core.tree.UPFPNode;
import MTPIU.core.tree.UPFPTree;

/**
 * Lớp trừu tượng định nghĩa khung sườn cho thuật toán khai phá MTPI.
 * Lớp này chịu trách nhiệm:
 * 1. Quản lý các tài nguyên chung (Database, Top-K Heap, Parameters).
 * 2. Thực hiện quy trình đệ quy (Mining Loop).
 * 3. Cung cấp các hàm tiện ích để xây dựng cây con và tính toán chỉ số.
 */
public abstract class AbstractMiner {

    protected final TopKHeap topK;
    protected final Parameters params;
    protected final Database db;
    protected final double N;

    public AbstractMiner(TopKHeap topK, Parameters params, Database db) {
        this.topK = topK;
        this.params = params;
        this.db = db;
        this.N = (double) db.getMaxTs() - db.getMinTs() + 1;
    }

    /**
     * Điểm bắt đầu của thuật toán.
     * @param tree Cây UPFP gốc (Global Tree).
     */
    public void run(UPFPTree tree) {
        mine(tree, new ArrayList<>());
    }

    /**
     * Helper Class: Conditional Path (CPB - Conditional Pattern Base).
     * Đại diện cho một nhánh (path) trong cây, kèm theo danh sách timestamps cục bộ và trọng số của nó.
     */
    protected static class ConditionalPath {
        List<String> pathItems;
        List<Integer> timestamps; // timestamps cục bộ của nhánh này
        double cap;         // trọng số của nhánh

        public ConditionalPath(List<String> pathItems, List<Integer> timestamps, double cap) {
            this.pathItems = pathItems;
            this.timestamps = timestamps;
            this.cap = cap;
        }
    }

    /**
     * Hàm đệ quy chính để khai phá pattern (The Core Mining Loop).
     * @param tree   Cây UPFP hiện tại (có thể là Global hoặc Conditional).
     * @param prefix Prefix pattern hiện tại (đã được khai phá ở các bước trước).
     */
    protected void mine(UPFPTree tree, List<String> prefix) {
        List<String> fList = tree.headerTable.getFlist();

        // Bước 1. Duyệt Header Table theo thứ tự ngược (Bottom-Up)
        for (int i = fList.size() - 1; i >= 0; i--) {
            String suffix = fList.get(i);
            UPFPHeaderTable.ItemInfo itemInfo = tree.headerTable.getItemInfo(suffix);

            // Kiểm tra ngay lập tức xem suffix này (và các tập con) có tiềm năng không.
            // Dựa trên ngưỡng minScore hiện tại của Top-K Heap.
            if (shouldPruneBranch(itemInfo.expSup, itemInfo.periodicity)) {
                continue; 
            }

            // Bước 2. Sinh pattern mới và tính toán thông tin cho nó
            List<String> pattern = new ArrayList<>(prefix);
            pattern.add(suffix);

            List<Integer> tsList = new ArrayList<>();   
            List<ConditionalPath> condPath = new ArrayList<>(); 
             
            UPFPNode node = itemInfo.firstNode;
            while (node != null) {
                // Thu thập Timestamps
                List<Integer> tsFromSubtree = collectTimestampsFromSubtree(node);
                if (!tsFromSubtree.isEmpty()) {
                    tsList.addAll(tsFromSubtree);

                    // Xây dựng đường dẫn (Path) từ Root -> Parent of Node
                    List<String> path = new ArrayList<>();
                    UPFPNode parent = node.getParent();
                    while (parent != null && parent.getItem() != null) {
                        path.add(parent.getItem());
                        parent = parent.getParent();
                    }
                    Collections.reverse(path); // Đảo ngược để có thứ tự từ Root xuống

                    // Lưu path vào danh sách CPB. 
                    // node.getExpSupCap() là trọng số của nhánh này.
                    condPath.add(new ConditionalPath(path, tsFromSubtree, node.getExpSupCap()));
                }

                node = node.getNodeLink(); // Chuyển sang node tiếp theo cùng item
            }

            Collections.sort(tsList);

            // Bước 3: Đánh giá Pattern hiện tại (Prefix + Suffix)
            // Chỉ thực hiện nếu pattern có độ dài > 1 (Prefix không rỗng)
            if (!prefix.isEmpty()) {

                // Kiểm tra minOcc (Số lần xuất hiện tối thiểu)
                if (tsList.size() < params.getMinOcc()) {
                    continue; 
                }
                
                // Tính Exact ExpSup, tổng xác suất thực tế của pattern
                double exactExpSup = calculateExactExpSup(pattern, tsList);

                // tính MaxGap
                double exactPer = itemInfo.periodicity;

                // Tính Score và cập nhật vào Top-K
                double finalScore = topK.calculateScore(exactExpSup, exactPer);
                Pattern p = new Pattern(pattern, exactExpSup, exactPer, finalScore);
                
                // TopKHeap sẽ tự động thêm nếu pattern này tốt hơn phần tử tệ nhất, và cập nhật lại giá trị các ngưỡng
                topK.add(p);
            }

            // if (topK.isFull()) {
            //     if (shouldPruneBranch(itemInfo.expSup, itemInfo.periodicity)) {
            //         continue; 
            //     }
            // }

            // Nếu không có đường dẫn nào để mở rộng thì bỏ qua vì không thể xây cây
            if (condPath.isEmpty()) continue;

            // Bước 5. Xây dựng cây con và Đệ quy
            UPFPTree condTree = buildConditionalTree(condPath);
            if (!condTree.headerTable.getFlist().isEmpty()) {
                mine(condTree, pattern);
            }
        }
    }
    
    /**
     * Phương thức trừu tượng để kiểm tra xem một nhánh có nên bị cắt tỉa hay không.
     * Cần được implement bởi lớp con (ví dụ: based on Utility or pure Probability).
     * @param esc Expected Support Capacity (Cận trên support).
     * @param currentPer Periodicity hiện tại.
     * @return true nếu nhánh này nên bị cắt.
     */
    protected abstract boolean shouldPruneBranch(double esc, double currentPer);

    /**
     * Tính Expected Support chính xác cho một pattern.
     * Duyệt qua từng transaction (timestamp) và nhân xác suất của các item trong pattern.
     * @param patternItems Danh sách item trong pattern.
     * @param tsList Danh sách timestamps mà pattern có khả năng xuất hiện.
     * @return Tổng xác suất thực tế.
     */
    private double calculateExactExpSup(List<String> patternItems, List<Integer> tsList) {
        double totalExpSup = 0.0;
        Map<Integer, Map<String, Double>> lookup = db.getProbLookupMap();

        for (Integer ts : tsList) {
            Map<String, Double> transProbs = lookup.get(ts);
            if (transProbs == null) continue;

            double productProb = 1.0;
            boolean valid = true;
            for (String pItem : patternItems) {
                Double p = transProbs.get(pItem);
                if (p == null) { valid = false; break; }
                productProb *= p;
            }
            if (valid) totalExpSup += productProb;
        }
        return totalExpSup;
    }

    /**
     * Thu thập tất cả timestamps từ một node và các node con của nó (subtree).
     * @param node Node bắt đầu.
     * @return Danh sách timestamps tổng hợp.
     */
    private List<Integer> collectTimestampsFromSubtree(UPFPNode node) {
        List<Integer> collected = new ArrayList<>();
        // Lấy timestamp tại node hiện tại (nếu node này là node lá của 1 transaction nào đó)
        if (!node.getTimestamps().isEmpty()) {
            collected.addAll(node.getTimestamps());
        }
        // Đệ quy lấy từ các node con
        for (UPFPNode child : node.getChildren().values()) {
            collected.addAll(collectTimestampsFromSubtree(child));
        }
        return collected;
    }

    /**
     * Xây dựng Conditional Tree từ danh sách các Conditional Paths (CPB).
     * Quá trình này bao gồm tính lại support cục bộ và tạo Header Table mới.
     * @param cpb Danh sách các đường dẫn điều kiện.
     * @return Cây conditional tree mới.
     */
    private UPFPTree buildConditionalTree(List<ConditionalPath> cpb) {
        UPFPTree tree = new UPFPTree();
        
        Map<String, Double> localSupMap = new HashMap<>();
        Map<String, List<Integer>> localTsMap = new HashMap<>();

        // 1. Tính expected support và tìm timestamp cho các item trong CPB
        for (ConditionalPath cp : cpb) {
            if (cp.pathItems.isEmpty()) continue;

            for (String item : cp.pathItems) {
                // Cộng dồn expected support (sử dụng support của suffix ban đầu)
                localSupMap.put(item, localSupMap.getOrDefault(item, 0.0) + cp.cap);
                // Gom tất cả timestamps liên quan đến item này
                localTsMap.computeIfAbsent(item, k -> new ArrayList<>()).addAll(cp.timestamps);
            }
        }

        // 2. Lọc và Tạo HeaderTable 
        List<String> localFList = new ArrayList<>();
        double currentMinSup = params.getMinSup();

        for (Map.Entry<String, Double> entry : localSupMap.entrySet()) {
            String item = entry.getKey();   
            double esc = entry.getValue();
            
            if (esc >= currentMinSup) { // Local Pruning
                UPFPHeaderTable.ItemInfo info = new UPFPHeaderTable.ItemInfo();
                info.expSup = esc;
                info.firstNode = null;
                
                // Tính Periodicity cục bộ
                List<Integer> ts = localTsMap.get(item);
                if (ts != null && !ts.isEmpty()) {
                    List<Integer> sortedList = new ArrayList<>(ts);
                    info.periodicity = calculateMaxGap(sortedList);
                } else {
                    info.periodicity = Double.MAX_VALUE;
                }
                
                tree.headerTable.getTable().put(item, info);
                localFList.add(item);
            }
        }

        // Sort F-List
        localFList.sort((a, b) -> Double.compare(localSupMap.get(b), localSupMap.get(a)));
        tree.headerTable.setfList(localFList);

        Map<String, Integer> rankMap = new HashMap<>();
        for (int i = 0; i < localFList.size(); i++) {
            rankMap.put(localFList.get(i), i);
        }

        // 3. Chèn path vào cây
        for (ConditionalPath cp : cpb) {
            if (cp.pathItems.isEmpty()) continue;

            List<String> filteredPath = new ArrayList<>();
            for (String item : cp.pathItems) {
                if (tree.headerTable.getTable().containsKey(item)) {
                    filteredPath.add(item);
                }
            }
            
            if (filteredPath.isEmpty()) continue;
            // filteredPath.sort((a, b) -> Integer.compare(localFList.indexOf(a), localFList.indexOf(b)));
            filteredPath.sort(Comparator.comparingInt(rankMap::get));

            insertPathToTree(tree, filteredPath, cp.cap, cp.timestamps);
        }

        return tree;
    }

    /**
     * Chèn một đường dẫn vào cây conditional tree
     * @param tree Cây đích.
     * @param path Đường dẫn các item.
     * @param pathCap Trọng số của đường dẫn.
     * @param tids Danh sách timestamps gắn với đường dẫn này (chỉ gán ở node cuối).
     */
    private void insertPathToTree(UPFPTree tree, List<String> path, double pathCap, List<Integer> timestamps) {
        UPFPNode currentNode = tree.root;
        for (String item : path) {
            UPFPNode child = currentNode.getChildren().get(item);
            // Nếu chưa có node con, tạo mới và link vào Header Table
            if (child == null) {
                child = new UPFPNode(item);
                child.setParent(currentNode);
                currentNode.getChildren().put(item, child);
                
                UPFPHeaderTable.ItemInfo info = tree.headerTable.getItemInfo(item);
                child.setNodeLink(info.firstNode);
                info.firstNode = child;
            }
            // Cộng dồn support cho node
            child.setExpSupCap(child.getExpSupCap() + pathCap);
            currentNode = child;
        }
        // Tại node cuối cùng của path, lưu trữ danh sách timestamps
        if (currentNode != tree.root) {
                currentNode.getTimestamps().addAll(timestamps);
        }
    }

    /**
     * Tính khoảng cách lớn nhất (MaxGap) trong danh sách timestamps.
     * @param tsList Danh sách timestamps (đã sort).
     * @return Giá trị Periodicity.
     */
    private double calculateMaxGap(List<Integer> tsList) {
        if (tsList.isEmpty()) return Double.MAX_VALUE;
        int maxGap = 0;
        maxGap = Math.max(maxGap, tsList.get(0) - db.getMinTs());
        for (int i = 0; i < tsList.size() - 1; i++) {
            maxGap = Math.max(maxGap, tsList.get(i+1) - tsList.get(i));
        }
        maxGap = Math.max(maxGap, db.getMaxTs() - tsList.get(tsList.size()-1));
        return (double) maxGap;
    }
}
