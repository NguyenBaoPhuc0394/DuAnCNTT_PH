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
    public UPFPHeaderTable scanFirstPass(List<Transaction> db, Parameters params) {
        // Tính expSup, per cho từng item
        // Lọc: expSup >= minSup && per <= maxExpPer
        // Trả về header table + F-List
        
        Map<String, Double> expSupMap = new HashMap<>();
        Map<String, List<Integer>> tsMap = new HashMap<>();

        for(Transaction t : db){
            for(UncertainItem itemProb : t.items){
                double prob = itemProb.probability;
                String item = itemProb.item;

                if(prob > 0.0){
                    expSupMap.put(item, expSupMap.getOrDefault(itemProb, 0.0) + prob);
                    tsMap.computeIfAbsent(item, k -> new ArrayList<>()).add(t.timestamp);
                }
            }
        }

        Map<String, Double> expPerMap = new HashMap<>();
        for(String item : expSupMap.keySet()){
            List<Integer> tsList = tsMap.get(item);
            if(tsList.size() < 2){
                expPerMap.put(item, Double.MAX_VALUE);
                continue;
            }

            double sumWeightedDist = 0.0;
            double sumProbPair = 0.0;

            for(int i = 0; i<tsList.size(); i++){
                for(int j = i+1; j<tsList.size(); j++){
                    int dist = tsList.get(j) - tsList.get(i);
                    double Pi = getProbAt(tsList.get(i), item, db);
                    double Pj = getProbAt(tsList.get(j), item, db);
                    double Pij = Pi*Pj;

                    sumWeightedDist += dist*Pij;
                    sumProbPair += Pij;
                }
            }

            double expPer = sumProbPair > 0? sumWeightedDist/sumProbPair : Double.MAX_VALUE;
            expPerMap.put(item, expPer); 
        }

        UPFPHeaderTable header = new UPFPHeaderTable();
        for(String item : expSupMap.keySet()){
            double expSup = expSupMap.get(item);
            double expPer = expPerMap.getOrDefault(item, Double.MAX_VALUE);

            if(expSup >= params.minSup && expPer <= params.maxExpPer){
                UPFPHeaderTable.ItemInfo itemInfo = new UPFPHeaderTable.ItemInfo();
                itemInfo.expSup = expSup;
                itemInfo.periodicity = expPer;
                itemInfo.firstNode = null;
                header.table.put(item, itemInfo);
            }
        }

        header.sortFlist();

        return header;
    }

    private double getProbAt(int timestamp, String item, List<Transaction> database) {
        for (Transaction t : database) {
            if (t.timestamp == timestamp) {
                for (var ip : t.items) {
                    if (ip.item.equals(item)) {
                        return ip.probability;
                    }
                }
            }
        }
        return 0.0;
    }
    
}
