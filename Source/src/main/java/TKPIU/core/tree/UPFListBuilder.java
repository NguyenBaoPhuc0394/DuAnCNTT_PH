package TKPIU.core.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import TKPIU.algorithms.topk.TopKHeap;
import TKPIU.config.Parameters;
import TKPIU.core.database.Database;
import TKPIU.core.database.Transaction;
import TKPIU.core.database.UncertainItem;
import TKPIU.core.pattern.Pattern;

/**
     * Lớp Scanner chịu trách nhiệm quét cơ sở dữ liệu ban đầu để xác định các 1-itemsets.
     * Nhiệm vụ chính:
        * 1. Tính toán Expected Support và Periodicity cho tất cả các item.
        * 2. Tính điểm (Score) cho từng item dựa trên công thức Top-K.
        * 3. Khởi tạo Heap Top-K ban đầu với các item tốt nhất.
        * 4. Xây dựng UPF-list chỉ chứa các item tốt nhất (đã được cắt tỉa sơ bộ).
 */
public class UPFListBuilder implements IUPFListBuilder{

    /**
         * Quét cơ sở dữ liệu để xây dựng UPF-list cho UPF-tree.
         *
         * @param db Cơ sở dữ liệu giao dịch (Transaction Database).
         * @param params Chứa các tham số cấu hình (K...).
         * @param topKHeap Heap quản lý Top-K pattern (dùng để tính score và lưu trữ ban đầu).
         * @return {@link UPFList} chứa danh sách các item đã được sắp xếp (F-List) và thông tin thống kê của chúng.
    */

    public UPFList buildUPFList(Database db, Parameters params, TopKHeap topKHeap){
        // 1. Thu thập thống kê cơ bản cho các item
        ItemStatistics stats = collectItemStatistics(db);

        // 2. Đánh giá các 1-itemsets (expSup, periodicity, score)
        List<Pattern> singleItemPatterns = evaluateSingleItemsets(stats, db, topKHeap);

        // 3. Khởi tạo Top-K heap bằng các 1-itemsets tốt nhất
        initializeTopKHeap(singleItemPatterns, params, topKHeap);

        // 4. Xây dựng UPF-list dựa trên ngưỡng minScore hiện tại
        return constructUPFList(singleItemPatterns, topKHeap.getMinScore());
    }

    private ItemStatistics collectItemStatistics(Database db){
        Map<String, Double> expSupMap = new HashMap<>();
        Map<String, List<Integer>> tsMap = new HashMap<>();
        for(Transaction tran : db.getTransactions()){
            int ts = tran.getTimestamp();

            for(UncertainItem ui : tran.getItems()){
                String itemName = ui.getItem();
                double prob = ui.getProbability();
                // Cộng dồn xác suất (Expected Support)
                expSupMap.put(itemName, expSupMap.getOrDefault(itemName, 0.0)+prob);
                // Thu thập timestamp
                tsMap.computeIfAbsent(itemName, k -> new ArrayList<>()).add(ts);
            }
        }

        return new ItemStatistics(expSupMap, tsMap);
    }

    private List<Pattern> evaluateSingleItemsets(ItemStatistics stats, Database db, TopKHeap topKHeap){
        List<Pattern> patterns = new ArrayList<>();
        for (String item : stats.expSupMap.keySet()) {

            double expSup = stats.expSupMap.get(item);
            List<Integer> tsList = stats.timestampMap.get(item);

            if (tsList == null || tsList.isEmpty()) {
                continue;
            }

            double periodicity = calculateMaxGap(tsList, db.getMinTs(), db.getMaxTs());

            Pattern pattern = new Pattern( Collections.singletonList(item), expSup, periodicity, 0);

            double score = topKHeap.calculateScore(pattern);
            pattern.setScore(score);

            patterns.add(pattern);
        }

        return patterns;
    }

    private void initializeTopKHeap(List<Pattern> patterns, Parameters params, TopKHeap topKHeap) {
        patterns.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        int limit = Math.min(patterns.size(), params.getK());
        for (int i = 0; i < limit; i++) {
            topKHeap.add(patterns.get(i));
        }
    }

    private UPFList constructUPFList(List<Pattern> patterns, double scoreThreshold) {

        UPFList upfList = new UPFList();
        List<Pattern> validPatterns = new ArrayList<>();

        for (Pattern p : patterns) {
            if (p.getScore() >= scoreThreshold) {

                validPatterns.add(p);

                UPFList.ItemInfo info = new UPFList.ItemInfo();
                info.expSup = p.getEsup();
                info.periodicity = p.getEper();
                info.firstNode = null;

                upfList.getHeaderTable().put(p.getItems().get(0), info);
            } else {
                break;
            }
        }

        // F-list được sắp xếp theo expSup (phục vụ UPF-tree)
        validPatterns.sort((a, b) -> Double.compare(b.getEsup(), a.getEsup()));

        List<String> fList = new ArrayList<>();
        for (Pattern p : validPatterns) {
            fList.add(p.getItems().get(0));
        }

        upfList.setfList(fList);
        return upfList;
    }

    /**
         * Tính toán Periodicity (MaxGap) của một item dựa trên danh sách timestamps.
         * Periodicity = max(StartGap, InternalGaps, EndGap).
         * @param tsList   Danh sách các timestamps xuất hiện của item 
         * @param minDbTs  Timestamp bắt đầu của toàn bộ Database (T_start).
         * @param maxDbTs  Timestamp kết thúc của toàn bộ Database (T_end).
         * @return         Giá trị khoảng cách lớn nhất (Max Gap).
     */
    private double calculateMaxGap(List<Integer> tsList, int minDbTs, int maxDbTs) {
        // Sắp xếp timestamps để tính khoảng cách liên tiếp
        Collections.sort(tsList); 
        int maxGap = 0;
        
        // 1. Start Gap: Khoảng cách từ đầu DB đến lần xuất hiện đầu tiên
        int startGap = tsList.get(0) - minDbTs;
        maxGap = Math.max(maxGap, startGap);

        // 2. Internal Gaps: Khoảng cách giữa các lần xuất hiện liên tiếp
        for (int i = 0; i < tsList.size() - 1; i++) {
            int gap = tsList.get(i + 1) - tsList.get(i);
            maxGap = Math.max(maxGap, gap);
        }

        // 3. End Gap: Khoảng cách từ lần xuất hiện cuối cùng đến cuối DB
        int endGap = maxDbTs - tsList.get(tsList.size() - 1);
        maxGap = Math.max(maxGap, endGap);

        return (double) maxGap;
    }

    class ItemStatistics {
        Map<String, Double> expSupMap;
        Map<String, List<Integer>> timestampMap;

        ItemStatistics(Map<String, Double> expSupMap, Map<String, List<Integer>> timestampMap) {
            this.expSupMap = expSupMap;
            this.timestampMap = timestampMap;
        }
    }

}
