package main.java.mining.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.java.mining.config.Parameters;
import main.java.mining.model.Transaction;
import main.java.mining.model.UncertainItem;
import main.java.mining.tree.UPFPHeaderTable;

public class Scanner {
    // Tính expSup, per cho từng item
    // Lọc: expSup >= minSup && per <= maxExpPer
    // Trả về header table + F-List
    public UPFPHeaderTable scanFirstPass(List<Transaction> db, Parameters params) {
        
        // Map luu Expected Support (Esup)
        Map<String, Double> expSupMap = new HashMap<>();
        // Map luu danh sach Timestamps (ts)
        Map<String, List<Integer>> tsMap = new HashMap<>();
        
        // Map tra cứu nhanh xác suất P(item|timestamp)
        // Map<Integer, Map<String, Double>> probLookupMap = new HashMap<>(); 
        Map<String, Map<Integer, Double>> itemProbAtTs = new HashMap<>();

        /*
            Bước 1: Quét DB lần 1 (O(|DB|)):
            - Tính Esup cho từng Item.
            - Thu thập Timestamps cho từng Item.
            - Xây dựng ProbLookupMap.
        */
        for (Transaction t : db) {
            int timestamp = t.getTimestamp();

            for (UncertainItem itemProb : t.items) {
                double prob = itemProb.getProbability();
                String item = itemProb.getItem();

                if (prob > 0.0) {
                    // Tính expSup: Sử dụng Map.merge để cộng dồn, hiệu quả hơn getOrDefault + put
                    expSupMap.merge(item, prob, Double::sum); 
                    
                    // Thu thập timestamps
                    tsMap.computeIfAbsent(item, k -> new ArrayList<>()).add(timestamp);
                    
                    // Lưu xác suất vào Map tra cứu nhanh
                    itemProbAtTs.computeIfAbsent(item, k -> new HashMap<>()).put(timestamp, prob);
                }
            }
        }

        /*
            Bước 2: Tính expPer (Expected Periodicity) cho từng item.
            (Độ phức tạp: O(FList * |tsList|^2), đã tối ưu do không còn O(|DB|) bên trong)
        */
        Map<String, Double> expPerMap = new HashMap<>();
        
        for (String item : expSupMap.keySet()) {
            List<Integer> tsList = tsMap.get(item);
            
            // Cần ít nhất 2 lần xuất hiện để tính Periodicity
            if (tsList.size() < 2) {
                expPerMap.put(item, Double.MAX_VALUE);
                continue;
            }

            double sumWeightedDist = 0.0;
            double sumProbPair = 0.0;

            for (int i = 0; i < tsList.size(); i++) {
                for (int j = i + 1; j < tsList.size(); j++) {
                    int dist = tsList.get(j) - tsList.get(i);
                    int ts_i = tsList.get(i);
                    int ts_j = tsList.get(j);
                    
                    double Pi = itemProbAtTs.get(item).get(ts_i);  
                    double Pj = itemProbAtTs.get(item).get(ts_j);
                    
                    double Pij = Pi * Pj;
                    
                    sumWeightedDist += dist * Pij;
                    sumProbPair += Pij;
                }
            }

            double expPer = sumProbPair > 0 ? sumWeightedDist / sumProbPair : Double.MAX_VALUE;
            expPerMap.put(item, expPer);
        }

        /*
            Bước 3: Lọc và Xây dựng Header Table (F-List)
        */
        UPFPHeaderTable header = new UPFPHeaderTable();
        for (String item : expSupMap.keySet()) {
            double expSup = expSupMap.get(item);
            double expPer = expPerMap.getOrDefault(item, Double.MAX_VALUE);

            if (expSup >= params.getMinSup() && expPer <= params.getMaxExpPer()) {
                UPFPHeaderTable.ItemInfo itemInfo = new UPFPHeaderTable.ItemInfo();
                itemInfo.expSup = expSup;
                itemInfo.periodicity = expPer;
                itemInfo.firstNode = null; 
                header.table.put(item, itemInfo);
            }
        }

        // Sắp xếp F-List theo Esup giảm dần (L-order)
        header.sortFlist();

        return header;
    }
}
