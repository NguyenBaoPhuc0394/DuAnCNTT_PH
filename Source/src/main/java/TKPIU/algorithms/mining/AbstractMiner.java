package TKPIU.algorithms.mining;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import TKPIU.algorithms.topk.TopKHeap;
import TKPIU.config.Parameters;
import TKPIU.core.database.Database;
import TKPIU.core.pattern.Pattern;
import TKPIU.core.tree.ConditionalTreeBuilder;
import TKPIU.core.tree.IConditionalTreeBuilder;
import TKPIU.core.tree.UPFList;
import TKPIU.core.tree.UPFNode;
import TKPIU.core.tree.UPFTree;

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
    protected final double dbSpan;
    private final IConditionalTreeBuilder condTreeBuilder;

    protected AbstractMiner(TopKHeap topK, Parameters params, Database db) {
        this.topK = topK;
        this.params = params;
        this.db = db;
        this.dbSpan = (double) db.getMaxTs() - db.getMinTs() + 1;
        this.condTreeBuilder = new ConditionalTreeBuilder(this.db, this.params);
    }

    protected static class ConditionalMiningData {
        final List<ConditionalPath> conditionalPaths;
        final List<Integer> timestamps;

        public ConditionalMiningData(List<ConditionalPath> paths, List<Integer> timestamps) {
            this.conditionalPaths = paths;
            this.timestamps = timestamps;
        }
    }

    public class ConditionalPath {
        public List<String> pathItems;
        public List<Integer> timestamps;
        public double weight;

        public ConditionalPath(List<String> pathItems, List<Integer> timestamps, double weight) {
            this.pathItems = pathItems;
            this.timestamps = timestamps;
            this.weight = weight;
        }
    }

    public final void run(UPFTree tree) {
        mineTree(tree, new ArrayList<>());
    }

    protected void mineTree(UPFTree tree, List<String> prefix) {

        // System.out.println(prefix);

        List<String> fList = tree.headerTable.getFlist();

        // if(prefix.size()==0 || prefix == null)
        //     System.out.println(fList);

        for (int i = fList.size() - 1; i >= 0; i--) {

            String suffix = fList.get(i);
            UPFList.ItemInfo info = tree.headerTable.getItemInfo(suffix);

            if (shouldPruneBranch(info.expSup, info.periodicity)) {
                continue;
            }


            List<String> newPattern = extendPattern(prefix, suffix);

            ConditionalMiningData miningData = collectConditionalData(tree, info);

            if (!prefix.isEmpty()) {
                evaluateAndPushPattern(newPattern, miningData.timestamps, info);
            }

            if (miningData.conditionalPaths.isEmpty()) {
                continue;
            }

            UPFTree conditionalTree = buildConditionalTree(miningData.conditionalPaths);

            if (!conditionalTree.headerTable.getFlist().isEmpty()) {
                mineTree(conditionalTree, newPattern);
            }
        }
    }

    protected List<String> extendPattern(List<String> prefix, String suffix) {
        List<String> pattern = new ArrayList<>(prefix);
        pattern.add(suffix);
        return pattern;
    }

    protected void evaluateAndPushPattern(List<String> pattern, List<Integer> timestamps, UPFList.ItemInfo info) {

        Collections.sort(timestamps);

        double exactExpSup = calculateExactExpectedSupport(pattern, timestamps);
        double exactPer = info.periodicity;

        double score = topK.calculateScore(exactExpSup, exactPer);
        Pattern p = new Pattern(pattern, exactExpSup, exactPer, score);

        topK.add(p);
    }
    
    protected ConditionalMiningData collectConditionalData(UPFTree tree, UPFList.ItemInfo info) {
        List<ConditionalPath> paths = new ArrayList<>();
        List<Integer> allTimestamps = new ArrayList<>();

        UPFNode node = info.firstNode;
        while (node != null) {
            List<Integer> ts = collectTimestampsFromSubtree(node);

            if (!ts.isEmpty()) {
                allTimestamps.addAll(ts);

                List<String> path = extractPrefixPath(node);

                paths.add(new ConditionalPath(path, ts, node.getExpSupCap()));
            }
            node = node.getNodeLink();
        }
        return new ConditionalMiningData(paths, allTimestamps);
    }

    protected List<String> extractPrefixPath(UPFNode node) {
        List<String> path = new ArrayList<>();
        UPFNode parent = node.getParent();

        while (parent != null && parent.getItem() != null) {
            path.add(parent.getItem());
            parent = parent.getParent();
        }
        Collections.reverse(path);
        return path;
    }

    protected double calculateExactExpectedSupport(List<String> items, List<Integer> timestamps) {

        double total = 0.0;
        Map<Integer, Map<String, Double>> lookup = db.getProbLookupMap();

        for (int ts : timestamps) {
            Map<String, Double> probs = lookup.get(ts);
            if (probs == null) continue;

            double p = 1.0;
            boolean valid = true;

            for (String item : items) {
                Double v = probs.get(item);
                if (v == null) {
                    valid = false;
                    break;
                }
                p *= v;
            }
            if (valid) total += p;
        }
        return total;
    }

    protected List<Integer> collectTimestampsFromSubtree(UPFNode node) {
        List<Integer> result = new ArrayList<>();

        if (!node.getTimestamps().isEmpty()) {
            result.addAll(node.getTimestamps());
        }

        for (UPFNode child : node.getChildren().values()) {
            result.addAll(collectTimestampsFromSubtree(child));
        }
        return result;
    }

    protected UPFTree buildConditionalTree(List<ConditionalPath> cpb) {
        return this.condTreeBuilder.buildConditionalTree(cpb);
    }

    protected abstract boolean shouldPruneBranch(double esc,double currentPer);
}
