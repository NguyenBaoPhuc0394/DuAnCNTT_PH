package main.java.MTPIU.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.java.MTPIU.algorithms.topk.TopKHeap;
import main.java.MTPIU.config.Parameters;
import main.java.MTPIU.core.pattern.Pattern;
import main.java.MTPIU.core.tree.UPFPHeaderTable;

public class Scanner {
    // public static Top
    public UPFPHeaderTable scan(Database db, Parameters params, TopKHeap topKHeap){
        // Parameters params = Parameters.getInstance();

        Map<String, Double> expSupMap = new HashMap<>();
        Map<String, List<Integer>> tsMap = new HashMap<>();

        for(Transaction tran : db.getTransactions()){
            int ts = tran.getTimestamp();

            for(UncertainItem ui : tran.getItems()){
                String itemName = ui.getItem();
                double prob = ui.getProbability();
                expSupMap.put(itemName, expSupMap.getOrDefault(itemName, 0.0)+prob);
                tsMap.computeIfAbsent(itemName, k -> new ArrayList<>()).add(ts);
            }
        }
        List<Pattern> oneList = new ArrayList<>();
        for (String item : expSupMap.keySet()) {
            List<Integer> tsList = tsMap.get(item);
            
            // Lọc minOcc (Ngưỡng cứng tối thiểu)
            if (tsList == null || tsList.size() < params.getMinOcc()) {
                continue;
            }

            double expSup = expSupMap.get(item);
            double periodicity = calculateMaxGap(tsList, db.getMinTs(), db.getMaxTs());

            Pattern pattern = new Pattern(Collections.singletonList(item), expSup, periodicity, 0);
            double score = topKHeap.calculateScore(pattern);
            pattern.setScore(score);
            oneList.add(pattern);
        }

        oneList.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        int limit = Math.min(oneList.size(), params.getK());
        for (int i = 0; i < limit; i++) {
            topKHeap.add(oneList.get(i)); 
        }

        double scoreThreshold = topKHeap.getMinScore();

        UPFPHeaderTable headerTable = new UPFPHeaderTable();
        List<String> fList = new ArrayList<>();
        for (Pattern p : oneList) {
            if (p.getScore() >= scoreThreshold) {
                
                UPFPHeaderTable.ItemInfo itemInfo = new UPFPHeaderTable.ItemInfo();
                itemInfo.expSup = p.getEsup();
                itemInfo.periodicity = p.getEper();
                itemInfo.firstNode = null;
                
                headerTable.getTable().put(p.getItems().get(0), itemInfo);
                fList.add(p.getItems().get(0));
            }
            else{
                break;
            }
            // UPFPHeaderTable.ItemInfo itemInfo = new UPFPHeaderTable.ItemInfo();
            // itemInfo.expSup = p.getEsup();
            // itemInfo.periodicity = p.getEper();
            // itemInfo.firstNode = null;
            
            // headerTable.getTable().put(p.getItems().get(0), itemInfo);
            // fList.add(p.getItems().get(0));
        }
        headerTable.setfList(fList);

        // Debug: In ra thông báo
        System.out.println("Scanner finished.");
        System.out.println("  Total Items scanned: " + expSupMap.size());
        System.out.println("  Items in F-List: " + headerTable.getFlist().size());
        System.out.println("  Initial Derived MinSup: " + params.getMinSup());
        System.out.println("  Initial Derived MaxPer: " + params.getMaxPer());

        return headerTable;
    }

    private double calculateMaxGap(List<Integer> tsList, int minDbTs, int maxDbTs) {
        Collections.sort(tsList); // Đảm bảo thứ tự tăng dần
        
        int maxGap = 0;
        
        // 1. Start Gap
        int startGap = tsList.get(0) - minDbTs;
        maxGap = Math.max(maxGap, startGap);

        // 2. Internal Gaps
        for (int i = 0; i < tsList.size() - 1; i++) {
            int gap = tsList.get(i + 1) - tsList.get(i);
            maxGap = Math.max(maxGap, gap);
        }

        // 3. End Gap
        int endGap = maxDbTs - tsList.get(tsList.size() - 1);
        maxGap = Math.max(maxGap, endGap);

        return (double) maxGap;
    }
}
