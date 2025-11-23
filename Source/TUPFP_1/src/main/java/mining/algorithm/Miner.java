package main.java.mining.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import main.java.mining.config.Parameters;
import main.java.mining.model.Pattern;
import main.java.mining.topk.TopKHeap;
import main.java.mining.tree.UPFPHeaderTable;
import main.java.mining.tree.UPFPNode;
import main.java.mining.tree.UPFPTree;

public class Miner {
    private final TopKHeap topK;
    private final Parameters params;

    public Miner(Parameters params){
        this.params = params;   
        this.topK = new TopKHeap(params.getK());
    }

    private static class ConditionalPath {
        List<String> path;
        double cap;
        List<Integer> timestamps;

        ConditionalPath(List<String> path, double cap, List<Integer> timestamps) {
            this.path = path;
            this.cap = cap;
            this.timestamps = timestamps;
        }
    }

    public List<Pattern> mine(UPFPTree tree) {
        params.setMinSup(0); // ban đầu
        mineRecursive(tree, new ArrayList<>());
        return topK.getTopK();
    }

    private void mineRecursive(UPFPTree tree, List<String> prefix) {
        List<String> fList = tree.headerTable.getFlist();

        // Duyệt suffix từ dưới lên (expSup giảm dần)
        for (int i = fList.size() - 1; i >= 0; i--) {
            String suffix = fList.get(i);
            List<String> newPrefix = new ArrayList<>(prefix);
            newPrefix.add(suffix);

            // Bước 1: Xây conditional pattern base
            List<ConditionalPath> condPaths = buildConditionalPatternBase(tree, suffix);

            // Bước 2: Xây conditional tree
            UPFPTree condTree = buildConditionalTree(condPaths, suffix);

            // Bước 3: Pruning bằng UB_Esup (ESC)
            double ubEsup = calculateUBEsup(condTree, suffix);
            if (ubEsup < params.getMinSup()) {
                continue; // bỏ nhánh
            }

            // Bước 4: Pruning bằng Periodicity (Max-Gap)
            // Logic: Nếu pattern hiện tại (đại diện bởi condPaths) đã có MaxGap > maxExpPer 
            // thì mọi con cháu của nó cũng sẽ vi phạm -> Cắt tỉa ngay.
            if (pruneByPeriodicityAndMinOcc(condPaths)) {
                continue; 
            }

            // Bước 5: Tính Esup, EPer thực tế
            double esup = calculateEsup(condPaths);
            
            double eper = calculateMaxGapPer(condPaths); 

            // Bước 6: Kiểm tra và thêm vào Top-K
            if (esup >= params.getMinSup() && eper <= params.getMaxPer()) {
                Pattern pattern = new Pattern(newPrefix, esup, eper);
                topK.add(pattern);
                if (topK.getLength() == params.getK()) {
                    params.setMinSup(topK.getMinSup());
                }
            }
            
            // Bước 7: Đệ quy
            if (!condTree.headerTable.getTable().isEmpty()) {
                mineRecursive(condTree, newPrefix);
            }
        }
    }
    
    private List<ConditionalPath> buildConditionalPatternBase(UPFPTree tree, String suffix) {
        List<ConditionalPath> paths = new ArrayList<>();

        UPFPHeaderTable.ItemInfo info = tree.headerTable.getItemInfo(suffix);
        if (info == null || info.firstNode == null) {
            return paths; 
        }
        
        UPFPNode node = info.firstNode;

        while (node != null) {
            List<String> path = new ArrayList<>();
            double cap = node.getExpSupCap();  
            List<Integer> ts = new ArrayList<>(node.getTimestamps());
            UPFPNode parent = node.getParent();

            while (parent != null && parent != tree.root) {
                path.add(parent.getItem());
                parent = parent.getParent();
            }
            Collections.reverse(path);
            if (!path.isEmpty()) {
                paths.add(new ConditionalPath(path, cap, ts));
            }
            node = node.getNodeLink();
        }
        return paths;
    }

    private UPFPTree buildConditionalTree(List<ConditionalPath> paths, String suffix) {
        UPFPTree condTree = new UPFPTree();
        UPFPHeaderTable condHeader = new UPFPHeaderTable();

        for (ConditionalPath cp : paths) {
            UPFPNode current = condTree.root;

            for (String item : cp.path) {
                UPFPNode child = current.getChildren().get(item);
                if (child == null) {
                    child = new UPFPNode(item);
                    current.getChildren().put(item, child);
                    child.setParent(current);

                    UPFPHeaderTable.ItemInfo info = condHeader.getItemInfo(item);
                    if (info == null) {
                        info = condHeader.addItem(item, 0.0); 
                    }
                    child.setNodeLink(info.firstNode);
                    info.firstNode = child;
                }
                
                child.setExpSupCap(child.getExpSupCap() + cp.cap);
                current = child; 
            }

            if (current != condTree.root) {
                current.getTimestamps().addAll(cp.timestamps);
                condTree.suffixNode = current;
            }
        }
        condHeader.sortFlist(); 
        condTree.attachConditionalHeader(condHeader);
        return condTree;
    }

    private double calculateUBEsup(UPFPTree tree, String suffix) {
        UPFPHeaderTable.ItemInfo info = tree.headerTable.getItemInfo(suffix);
        if (info == null || info.firstNode == null) return 0.0;
        double ub = 0.0;
        UPFPNode node = info.firstNode;
        while (node != null) {
            ub += node.getExpSupCap();
            node = node.getNodeLink();
        }
        return ub;
    }

    private double calculateEsup(List<ConditionalPath> paths) {
        double esup = 0.0;
        for (ConditionalPath cp : paths) {
            esup += cp.cap;
        }
        return esup;
    }

    private boolean pruneByPeriodicityAndMinOcc(List<ConditionalPath> paths) {
        TreeSet<Integer> sortedTimestamps = new TreeSet<>();
        for (ConditionalPath cp : paths) {
            sortedTimestamps.addAll(cp.timestamps);
        }

        // [CHECK 1]: Kiểm tra Min Occurrence (Integer comparison)
        // Nếu số lượng giao dịch xuất hiện < minOcc -> CẮT TỈA
        if (sortedTimestamps.size() < params.getMinOcc()) {
            return true; 
        }

        List<Integer> tsList = new ArrayList<>(sortedTimestamps);
        int maxGap = 0;
        
        // [CHECK 2]: Kiểm tra Periodicity (Max Gap)
        for (int i = 0; i < tsList.size() - 1; i++) {
            int gap = tsList.get(i+1) - tsList.get(i);
            if (gap > maxGap) maxGap = gap;
        }
        
        // Tính Gap đầu/cuối (như đã bàn)
        int startGap = tsList.get(0) - params.getDbMinTimestamp();
        int endGap = params.getDbMaxTimestamp() - tsList.get(tsList.size() - 1);
        maxGap = Math.max(maxGap, Math.max(startGap, endGap));

        // Nếu MaxGap > maxExpPer -> CẮT TỈA
        return maxGap > params.getMaxPer();
    }

    private double calculateMaxGapPer(List<ConditionalPath> paths) {
        TreeSet<Integer> sortedTimestamps = new TreeSet<>();
        for (ConditionalPath cp : paths) {
            sortedTimestamps.addAll(cp.timestamps);
        }

        if (sortedTimestamps.isEmpty()) return Double.MAX_VALUE;

        List<Integer> tsList = new ArrayList<>(sortedTimestamps);
        int maxGap = 0;
        
        for (int i = 0; i < tsList.size() - 1; i++) {
            int gap = tsList.get(i+1) - tsList.get(i);
            if (gap > maxGap) {
                maxGap = gap;
            }
        }
        int startGap = tsList.get(0) - params.getDbMinTimestamp();
        int endGap = params.getDbMaxTimestamp() - tsList.get(tsList.size() - 1);

        maxGap = Math.max(maxGap, Math.max(startGap, endGap));
        return (double) maxGap;
    }
}