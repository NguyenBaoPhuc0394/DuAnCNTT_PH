package main.java.mining.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

            // Bước 3: Pruning bằng UB_Esup
            double ubEsup = calculateUBEsup(condTree, suffix);
            if (ubEsup < params.getMinSup()) {
                continue; // bỏ nhánh
            }

            // Bước 4: Pruning bằng LB_EPer
            double lbEper = calculateLBEper(condPaths);
            if (lbEper > params.getMaxExpPer()) {
                continue; // bỏ nhánh
            }

            // Bước 5: Tính Esup, EPer thực tế
            double esup = calculateEsup(condPaths);
            double eper = calculateEper(condPaths);

            // Bước 6: Kiểm tra và thêm vào Top-K
            if (esup >= params.getMinSup() && eper <= params.getMaxExpPer()) {
                Pattern pattern = new Pattern(newPrefix, esup, eper);
                topK.add(pattern);
                if (topK.getLength()==params.getK()) {
                    params.setMinSup(topK.getMinSup());
                }
            }
            
            // Bước 7: Đệ quy
            if (!condTree.headerTable.table.isEmpty()) {
                mineRecursive(condTree, newPrefix);
            }
        }
    }

    private List<ConditionalPath> buildConditionalPatternBase(UPFPTree tree, String suffix) {
        List<ConditionalPath> paths = new ArrayList<>();

        UPFPHeaderTable.ItemInfo info = tree.headerTable.getItemInfo(suffix);
        if (info == null || info.firstNode == null) {
            return paths; // không có node nào → trả rỗng, không lỗi
        }
        
        UPFPNode node = info.firstNode;

        while (node != null) {
            List<String> path = new ArrayList<>();
            double cap = node.expSupCap;  // DÙNG expSupCap TẠI TAIL-NODE
            List<Integer> ts = new ArrayList<>(node.timestamps);
            UPFPNode parent = node.parent;

            while (parent != null && parent != tree.root) {
                path.add(parent.item);
                parent = parent.parent;
            }
            Collections.reverse(path);
            if (!path.isEmpty()) {
                paths.add(new ConditionalPath(path, cap, ts));
            }
            node = node.nodeLink;
        }
        return paths;
    }

    private UPFPTree buildConditionalTree(List<ConditionalPath> paths, String suffix) {
        UPFPTree condTree = new UPFPTree();
        UPFPHeaderTable condHeader = new UPFPHeaderTable();

        for (ConditionalPath cp : paths) {
            UPFPNode current = condTree.root;

            // Bắt đầu chèn đường đi tiền tố vào condTree
            for (String item : cp.path) {
                UPFPNode child = current.children.get(item);
                if (child == null) {
                    child = new UPFPNode(item);
                    current.children.put(item, child);
                    child.parent = current;

                    // Cập nhật nodeLink cho conditional header
                    UPFPHeaderTable.ItemInfo info = condHeader.getItemInfo(item);
                    if (info == null) {
                        // Cây có điều kiện không cần expSup ban đầu, chỉ cần cấu trúc
                        info = condHeader.addItem(item, 0.0); 
                    }
                    child.nodeLink = info.firstNode;
                    info.firstNode = child;
                }
                
                child.expSupCap += cp.cap; 

                current = child; // Đi xuống nút con
            }

            if (current != condTree.root) {
                current.timestamps.addAll(cp.timestamps);
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
            ub += node.expSupCap;
            node = node.nodeLink;
        }
        return ub;
    }

    private double calculateLBEper(List<ConditionalPath> paths) {
        double maxWeightedDist = Double.MAX_VALUE;
        
        for (ConditionalPath cp : paths) {
            List<Integer> ts = cp.timestamps;
            if (ts.size() < 2) continue;
            
            for (int i = 0; i < ts.size(); i++) {
                for (int j = i + 1; j < ts.size(); j++) {
                    double weightedDist = (ts.get(j) - ts.get(i)) / (cp.cap * cp.cap);
                    if (weightedDist < maxWeightedDist) {
                        maxWeightedDist = weightedDist;
                    }
                }
            }
        }
        
        return maxWeightedDist == Double.MAX_VALUE ? Double.MAX_VALUE : maxWeightedDist;
    }

    private double calculateEsup(List<ConditionalPath> paths) {
        double esup = 0.0;
        for (ConditionalPath cp : paths) {
            esup += cp.cap;
        }
        return esup;
    }

    private double calculateEper(List<ConditionalPath> paths) {
        double sumWeightedDist = 0.0;
        double sumWeight = 0.0;

        for (ConditionalPath cp : paths) {
            List<Integer> ts = cp.timestamps;
            for (int i = 0; i < ts.size(); i++) {
                for (int j = i + 1; j < ts.size(); j++) {
                    int dist = ts.get(j) - ts.get(i);
                    double w = cp.cap * cp.cap;
                    sumWeightedDist += dist * w;
                    sumWeight += w;
                }
            }
        }
        // System.out.println(sumWeight);
        return sumWeight > 0 ? sumWeightedDist / sumWeight : Double.MAX_VALUE;
    }
}
