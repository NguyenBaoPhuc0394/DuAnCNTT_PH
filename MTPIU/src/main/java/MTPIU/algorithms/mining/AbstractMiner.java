package main.java.MTPIU.algorithms.mining;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import main.java.MTPIU.algorithms.topk.TopKHeap;
import main.java.MTPIU.config.Parameters;
import main.java.MTPIU.core.database.Database;
import main.java.MTPIU.core.pattern.Pattern;
import main.java.MTPIU.core.tree.UPFPHeaderTable;
import main.java.MTPIU.core.tree.UPFPNode;
import main.java.MTPIU.core.tree.UPFPTree;

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

    public void run(UPFPTree tree) {
        mine(tree, new ArrayList<>());
    }

    /**
     * Helper Class: Conditional Path
     * Lưu path items và danh sách TIDs cục bộ ứng với path đó.
     */
    protected static class ConditionalPath {
        List<String> pathItems;
        List<Integer> tids; // TIDs cục bộ của nhánh này
        double cap;         // ExpSupCap của node suffix

        public ConditionalPath(List<String> pathItems, List<Integer> tids, double cap) {
            this.pathItems = pathItems;
            this.tids = tids;
            this.cap = cap;
        }
    }

    // protected void mine(UPFPTree tree, List<String> prefix) {
    //     List<String> fList = tree.headerTable.getFlist();
    //     // System.out.println(fList);
    //     // 1. Duyệt Header Table (Bottom-Up)
    //     for (int i = fList.size() - 1; i >= 0; i--) {
    //         String suffix = fList.get(i);
    //         UPFPHeaderTable.ItemInfo itemInfo = tree.headerTable.getItemInfo(suffix);
            
    //         // 2. Sinh pattern mới: pattern = alpha U {item}
    //         List<String> pattern = new ArrayList<>(prefix);
    //         pattern.add(suffix);
    //         // System.out.println(pattern);

    //         // =================================================================
    //         // BƯỚC 3: THU THẬP TIMESTAMP LIST (pattern) & PREPARE CPB
    //         // Ta thực hiện duyệt Node-link 1 lần duy nhất ở đây để lấy hết dữ liệu cần thiết.
    //         // =================================================================
            
    //         List<Integer> tsList = new ArrayList<>(); // Danh sách tổng hợp tất cả timestamp của pattern
    //         List<ConditionalPath> condPath = new ArrayList<>(); // CPB tiềm năng (chưa lọc)

    //         UPFPNode node = itemInfo.firstNode;
    //         while (node != null) {
    //             // Lấy timestamp từ tail-nodes của nhánh này
    //             List<Integer> tsFromSubtree = collectTimestampsFromSubtree(node);
                
    //             if (!tsFromSubtree.isEmpty()) {
    //                 // a. Gom vào Timestamps
    //                 tsList.addAll(tsFromSubtree);

    //                 // b. Chuẩn bị sẵn path cho CPB (để nếu pattern OK thì dùng luôn)
    //                 // Lần ngược lên cha để lấy Prefix Path
    //                 List<String> path = new ArrayList<>();
    //                 UPFPNode parent = node.getParent();
    //                 while (parent != null && parent.getItem() != null) {
    //                     path.add(parent.getItem());
    //                     parent = parent.getParent();
    //                 }
    //                 Collections.reverse(path);
                    
    //                 // Lưu lại path + tids cục bộ + cap
    //                 condPath.add(new ConditionalPath(path, tsFromSubtree, node.getExpSupCap()));
    //             }
    //             node = node.getNodeLink();
    //         }
    //         // System.out.println("Mining item: " + suffix + ", CPB size: " + condPath.size());

    //         // =================================================================
    //         // BƯỚC 4: EXACT CHECKING (Kiểm tra điều kiện chính xác & Cắt tỉa)
    //         // =================================================================

    //         // 4.2 Tính toán Exact Metrics và Update Top-K
    //         // Chỉ tính nếu đây không phải là 1-itemset (vì 1-itemset đã làm ở Scanner)
    //         if (!prefix.isEmpty()) {
    //             // 4.1 Kiểm tra minOcc (Trên tổng số lần xuất hiện của pattern)
    //             if (tsList.size() < params.getMinOcc()) {
    //                 continue; // Pattern rác -> Bỏ qua ngay
    //             }

    //             double exactExpSup = calculateExactExpSup(pattern, tsList);
                
    //             Collections.sort(tsList); // Sort để tính MaxGap
    //             double exactPer = calculateMaxGap(tsList);

    //             Pattern p = new Pattern(pattern, exactExpSup, exactPer, 0.0);
    //             topK.add(p);
    //         }

    //         // 4.3 Cắt tỉa nhánh (Pruning Hook)
    //         // Dựa trên thông tin ước lượng (ESC) từ HeaderTable
    //         if (shouldPruneBranch(itemInfo.expSup, itemInfo.periodicity)) {
    //             continue; // Cắt nhánh
    //         }

    //         // Nếu không có path nào để mở rộng -> Next
    //         if (condPath.isEmpty()) continue;

    //         // =================================================================
    //         // BƯỚC 5: XÂY DỰNG CONDITIONAL UPFP-TREE (của alpha)
    //         // Chỉ thực hiện khi alpha đã vượt qua các bài test ở trên.
    //         // =================================================================
            
    //         UPFPTree condTree = buildConditionalTree(condPath);
    //         // System.out.println("Built tree for " + suffix + ", Header size: " + condTree.headerTable.getFlist().size());

    //         // =================================================================
    //         // BƯỚC 6: ĐỆ QUY (Mở rộng pattern)
    //         // =================================================================
    //         if (!condTree.headerTable.getFlist().isEmpty()) {
    //             mine(condTree, pattern);
    //         }
    //     }
    // }

    protected void mine(UPFPTree tree, List<String> prefix) {
        List<String> fList = tree.headerTable.getFlist();

        // 1. Duyệt Header Table (Bottom-Up)
        for (int i = fList.size() - 1; i >= 0; i--) {
            String suffix = fList.get(i);
            UPFPHeaderTable.ItemInfo itemInfo = tree.headerTable.getItemInfo(suffix);

            // =================================================================
            // [OPTIMIZATION 1] EARLY PRUNING (CẮT TỈA SỚM)
            // Kiểm tra ngay dựa trên thông tin ước lượng từ Header Table
            // Trước khi làm bất cứ việc gì tốn kém!
            // =================================================================
            
            // Logic: Support thực tế luôn <= itemInfo.expSup (ước lượng)
            //        Periodicity thực tế luôn >= itemInfo.periodicity (ước lượng)
            // Nếu ước lượng tốt nhất đã không qua được Strategy 3 -> CẮT LUÔN.
            if (shouldPruneBranch(itemInfo.expSup, itemInfo.periodicity)) {
                continue; 
            }

            // 2. Sinh pattern mới
            List<String> pattern = new ArrayList<>(prefix);
            pattern.add(suffix);

            // =================================================================
            // BƯỚC 3: THU THẬP TIMESTAMP LIST & PREPARE CPB
            // Chỉ làm khi đã qua vòng gửi xe ở trên
            // =================================================================
            
            List<Integer> tsList = new ArrayList<>();
            List<ConditionalPath> condPath = new ArrayList<>();

            UPFPNode node = itemInfo.firstNode;
            while (node != null) {
                // [OPTIMIZATION 2] Chỉ collect nếu node có dữ liệu
                // Logic đệ quy của bạn đã đúng, giữ nguyên
                List<Integer> tsFromSubtree = collectTimestampsFromSubtree(node);
                
                if (!tsFromSubtree.isEmpty()) {
                    tsList.addAll(tsFromSubtree);

                    // Build CPB Path (giữ nguyên code cũ)
                    List<String> path = new ArrayList<>();
                    UPFPNode parent = node.getParent();
                    while (parent != null && parent.getItem() != null) {
                        path.add(parent.getItem());
                        parent = parent.getParent();
                    }
                    Collections.reverse(path);
                    condPath.add(new ConditionalPath(path, tsFromSubtree, node.getExpSupCap()));
                }
                node = node.getNodeLink();
            }

            // 4.1 Kiểm tra minOcc (Cực nhanh - check size trước)
            if (tsList.size() < params.getMinOcc()) {
                continue; 
            }

            // =================================================================
            // BƯỚC 4: EXACT CHECKING (NẶNG NHẤT - LÀM SAU CÙNG)
            // =================================================================
            
            // Chỉ tính nếu đây không phải là 1-itemset (Scanner đã làm rồi)
            // Hoặc nếu bạn muốn chắc chắn, cứ tính lại cũng được, nhưng tốn time.
            if (!prefix.isEmpty()) {
                
                // [OPTIMIZATION 3] Tính Exact ExpSup
                // Đây là đoạn tốn kém vì phải tra Map Lookup
                double exactExpSup = calculateExactExpSup(pattern, tsList);

                // [OPTIMIZATION 4] Sort và tính MaxGap
                // Sort tốn O(NlogN), chỉ làm khi thực sự cần
                Collections.sort(tsList); 
                double exactPer = calculateMaxGap(tsList);

                // Tạo pattern và đưa vào Top-K
                Pattern p = new Pattern(pattern, exactExpSup, exactPer, 0.0);
                
                // TopKHeap sẽ tự động trả về true/false nếu add thành công
                // Bạn có thể tận dụng điều này để update lại minScore ngay
                topK.add(p);
            }

            // Nếu không có đường dẫn nào để mở rộng -> Next
            if (condPath.isEmpty()) continue;

            // =================================================================
            // BƯỚC 5: XÂY DỰNG CONDITIONAL TREE & ĐỆ QUY
            // =================================================================
            
            UPFPTree condTree = buildConditionalTree(condPath);
            if (!condTree.headerTable.getFlist().isEmpty()) {
                mine(condTree, pattern);
            }
        }
    }
    
    // --- ABSTRACT HOOK ---
    protected abstract boolean shouldPruneBranch(double esc, double currentPer);

    // --- CORE HELPERS ---

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

    private List<Integer> collectTimestampsFromSubtree(UPFPNode node) {
        List<Integer> collected = new ArrayList<>();
        if (!node.getTimestamps().isEmpty()) {
            collected.addAll(node.getTimestamps());
        }
        for (UPFPNode child : node.getChildren().values()) {
            collected.addAll(collectTimestampsFromSubtree(child));
        }
        return collected;
    }

    /**
     * Xây dựng Conditional Tree từ danh sách CPB đã chuẩn bị sẵn.
     */
    private UPFPTree buildConditionalTree(List<ConditionalPath> cpb) {
        UPFPTree tree = new UPFPTree();
        
        Map<String, Double> localSupMap = new HashMap<>();
        Map<String, TreeSet<Integer>> localTidsMap = new HashMap<>(); 

        // 1. Thống kê Support và Timestamp cho các item trong CPB
        for (ConditionalPath cp : cpb) {
            if (cp.pathItems.isEmpty()) continue;

            for (String item : cp.pathItems) {
                localSupMap.put(item, localSupMap.getOrDefault(item, 0.0) + cp.cap);
                // Vẫn cần dùng timestamps của từng path để tính Periodicity cục bộ chính xác 
                localTidsMap.computeIfAbsent(item, k -> new TreeSet<>()).addAll(cp.tids);
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
                TreeSet<Integer> tids = localTidsMap.get(item);
                if (tids != null && !tids.isEmpty()) {
                    List<Integer> sortedList = new ArrayList<>(tids);
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
            filteredPath.sort((a, b) -> Integer.compare(localFList.indexOf(a), localFList.indexOf(b)));
            
            insertPathToTree(tree, filteredPath, cp.cap, cp.tids);
        }

        return tree;
    }

    private void insertPathToTree(UPFPTree tree, List<String> path, double pathCap, List<Integer> tids) {
        UPFPNode currentNode = tree.root;
        for (String item : path) {
            UPFPNode child = currentNode.getChildren().get(item);
            if (child == null) {
                child = new UPFPNode(item);
                child.setParent(currentNode);
                currentNode.getChildren().put(item, child);
                
                UPFPHeaderTable.ItemInfo info = tree.headerTable.getItemInfo(item);
                child.setNodeLink(info.firstNode);
                info.firstNode = child;
            }
            child.setExpSupCap(child.getExpSupCap() + pathCap);
            currentNode = child;
        }
        if (currentNode != tree.root) {
                currentNode.getTimestamps().addAll(tids);
        }
    }

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
