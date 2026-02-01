package TKPIU.core.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import TKPIU.algorithms.mining.AbstractMiner;
import TKPIU.algorithms.mining.AbstractMiner.ConditionalPath;
import TKPIU.config.Parameters;
import TKPIU.core.database.Database;

public class ConditionalTreeBuilder implements IConditionalTreeBuilder{
    private final Database db;
    private final Parameters params;

    public ConditionalTreeBuilder(Database db, Parameters params) {
            this.db = db;
            this.params = params;
    }

    private static class LocalStatistics {
        Map<String, Double> localExpSup;
        Map<String, List<Integer>> localTimestamps;

        public LocalStatistics(Map<String, Double> localExpSup, Map<String, List<Integer>> localTimestamps){
            this.localExpSup = localExpSup;
            this.localTimestamps = localTimestamps;
        }
    }

    @Override
    public UPFTree buildConditionalTree(List<AbstractMiner.ConditionalPath> cpb) {

        LocalStatistics stats = collectLocalStatistics(cpb);
        UPFList header = buildLocalHeaderTable(stats);

        if (header.getFlist().isEmpty()) {
            return new UPFTree(); // cây rỗng
        }

        return constructTreeFromCPB(cpb, header);
    }

    private LocalStatistics collectLocalStatistics(List<AbstractMiner.ConditionalPath> cpb) {
        Map<String, Double> supMap = new HashMap<>();
        Map<String, List<Integer>> tsMap = new HashMap<>();

        for (ConditionalPath cp : cpb) {
            for (String item : cp.pathItems) {
                supMap.merge(item, cp.weight, Double::sum);
                tsMap.computeIfAbsent(item, k -> new ArrayList<>()).addAll(cp.timestamps);
            }
        }
        return new LocalStatistics(supMap, tsMap);
    }

    private UPFList buildLocalHeaderTable(LocalStatistics stats) {
        UPFList header = new UPFList();
        List<String> fList = new ArrayList<>();

        for (String item : stats.localExpSup.keySet()) {

            double esc = stats.localExpSup.get(item);
            if (esc < params.getMinSup()) continue;

            List<Integer> ts = stats.localTimestamps.get(item);
            double per = (ts == null || ts.isEmpty()) ? Double.MAX_VALUE : calculateMaxGap(ts);

            if (per > params.getMaxPer()) continue;

            UPFList.ItemInfo info = new UPFList.ItemInfo();
            info.expSup = esc;
            info.periodicity = per;
            info.firstNode = null;

            header.getHeaderTable().put(item, info);
            fList.add(item);
        }

        fList.sort((a, b) -> Double.compare(stats.localExpSup.get(b), stats.localExpSup.get(a)));

        header.setfList(fList);
        return header;
    }

    private double calculateMaxGap(List<Integer> tsList) {
            if (tsList.isEmpty()) return Double.MAX_VALUE;
            Collections.sort(tsList);
            int maxGap = 0;
            maxGap = Math.max(maxGap, tsList.get(0) - db.getMinTs());
            for (int i = 0; i < tsList.size() - 1; i++) {
                maxGap = Math.max(maxGap, tsList.get(i + 1) - tsList.get(i));
            }
            maxGap = Math.max(maxGap, db.getMaxTs() - tsList.get(tsList.size() - 1));
            return (double) maxGap;
    }

    private List<String> filterAndSortPath(List<String> pathItems, UPFList header, Map<String, Integer> rankMap) {

        List<String> filtered = new ArrayList<>();
        for (String item : pathItems) {
            if (header.getHeaderTable().containsKey(item)) {
                filtered.add(item);
            }
        }
        filtered.sort(Comparator.comparingInt(rankMap::get));
        return filtered;
    }

    private void insertPath(UPFTree tree, List<String> path, double pathCap, List<Integer> timestamps) {
        UPFNode current = tree.root;

        for (String item : path) {
            UPFNode child = current.getChildren().get(item);

            if (child == null) {
                child = new UPFNode(item);
                child.setParent(current);
                current.getChildren().put(item, child);

                UPFList.ItemInfo info = tree.headerTable.getItemInfo(item);
                child.setNodeLink(info.firstNode);
                info.firstNode = child;
            }

            child.setExpSupCap(child.getExpSupCap() + pathCap);
            current = child;
        }

        if (current != tree.root) {
            current.getTimestamps().addAll(timestamps);
        }
    }

    private UPFTree constructTreeFromCPB(List<ConditionalPath> cpb, UPFList header) {
        UPFTree tree = new UPFTree();
        tree.headerTable.setHeaderTable(header.getHeaderTable());
        tree.headerTable.setfList(header.getFlist());

        Map<String, Integer> rank = buildRankMap(header.getFlist());

        for (ConditionalPath cp : cpb) {
            List<String> filtered = filterAndSortPath(cp.pathItems, header, rank);

            if (!filtered.isEmpty()) {
                insertPath(tree, filtered, cp.weight, cp.timestamps);
            }
        }
        return tree;
    }

    private Map<String, Integer> buildRankMap(List<String> fList) {
        Map<String, Integer> rankMap = new HashMap<>();
        for (int i = 0; i < fList.size(); i++) {
            rankMap.put(fList.get(i), i);
        }
        return rankMap;
    }


}
