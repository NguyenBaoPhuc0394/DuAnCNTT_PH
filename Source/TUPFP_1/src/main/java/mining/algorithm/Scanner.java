package main.java.mining.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.java.mining.config.Parameters;
import main.java.mining.model.Transaction;
import main.java.mining.model.UncertainItem;
import main.java.mining.tree.UPFPHeaderTable;

public class Scanner {

    // Tính expSup và Max Periodicity (Max Gap) cho từng item
    // Lọc: expSup >= minSup && per <= maxExpPer
    public UPFPHeaderTable scanFirstPass(List<Transaction> db, Parameters params) {
        
        // Map luu Expected Support (Esup)
        Map<String, Double> expSupMap = new HashMap<>();
        // Map luu danh sach Timestamps (ts)
        Map<String, List<Integer>> tsMap = new HashMap<>();
        
        int minTs = db.isEmpty() ? 0 : db.get(0).getTimestamp();
        int maxTs = db.isEmpty() ? 0 : db.get(db.size() - 1).getTimestamp();
        params.setDbMinTimestamp(minTs);
        params.setDbMaxTimestamp(maxTs);
        /*
            Bước 1: Quét DB lần 1:
            - Tính Esup cho từng Item.
            - Thu thập Timestamps cho từng Item.
        */
        for (Transaction t : db) {
            int timestamp = t.getTimestamp();

            for (UncertainItem itemProb : t.getItems()) {
                double prob = itemProb.getProbability();
                String item = itemProb.getItem();

                if (prob > 0.0) { 
                    // Tính expSup
                    expSupMap.merge(item, prob, Double::sum); 
                    
                    // Thu thập timestamps
                    tsMap.computeIfAbsent(item, k -> new ArrayList<>()).add(timestamp);
                }
            }
        }

        /*
            Bước 2: Tính Periodicity (Max Gap) cho từng item.
        */
        Map<String, Double> perMap = new HashMap<>();
        
        for (String item : expSupMap.keySet()) {
            List<Integer> tsList = tsMap.get(item);
            
            // Cần ít nhất 2 lần xuất hiện để tính Periodicity
            if (tsList == null || tsList.size() < params.getMinOcc())
            // if (tsList == null || tsList.size() < 2) 
            {
                continue;
            }

            // Đảm bảo timestamps được sắp xếp tăng dần để tính Gap
            Collections.sort(tsList);

            int maxGap = 0;

            // Tìm khoảng cách lớn nhất
            for (int i = 0; i < tsList.size() - 1; i++) {
                int gap = tsList.get(i+1) - tsList.get(i);
                if (gap > maxGap) {
                    maxGap = gap;
                }
            }
            
            int startGap = tsList.get(0) - minTs;
            // Gap từ lần xuất hiện cuối cùng đến hết DB
            int endGap = maxTs - tsList.get(tsList.size() - 1);

            // Cập nhật MaxGap
            maxGap = Math.max(maxGap, Math.max(startGap, endGap));

            perMap.put(item, (double) maxGap);
        }

        /*
            Bước 3: Lọc và Xây dựng Header Table (F-List)
        */
        UPFPHeaderTable header = new UPFPHeaderTable();
        
        for (String item : expSupMap.keySet()) {
            double expSup = expSupMap.get(item);
            double per = perMap.getOrDefault(item, Double.MAX_VALUE);

            // Kiểm tra điều kiện: Đủ Support VÀ MaxGap nhỏ hơn ngưỡng
            if (expSup >= params.getMinSup() && per <= params.getMaxPer()) {
                UPFPHeaderTable.ItemInfo itemInfo = new UPFPHeaderTable.ItemInfo();
                itemInfo.expSup = expSup;
                itemInfo.periodicity = per; // Đây là Max Gap
                itemInfo.firstNode = null; 
                header.getTable().put(item, itemInfo); 
            }
        }

        // Sắp xếp F-List theo Esup giảm dần (L-order)
        header.sortFlist();

        return header;
    }
}