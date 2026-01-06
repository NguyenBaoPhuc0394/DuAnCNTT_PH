package MTPIU.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import MTPIU.algorithms.topk.TopKHeap;
import MTPIU.config.Parameters;
import MTPIU.core.pattern.Pattern;
import MTPIU.core.tree.UPFPHeaderTable;

/**
     * Lớp Scanner chịu trách nhiệm quét cơ sở dữ liệu ban đầu để xác định các 1-itemsets.
     * Nhiệm vụ chính:
        * 1. Tính toán Expected Support và Periodicity cho tất cả các item.
        * 2. Tính điểm (Score) cho từng item dựa trên công thức Top-K.
        * 3. Khởi tạo Heap Top-K ban đầu với các item tốt nhất.
        * 4. Xây dựng Header Table (F-List) chỉ chứa các item tốt nhất (đã được cắt tỉa sơ bộ).
 */
public class Scanner {

    /**
         * Quét cơ sở dữ liệu để xây dựng Header Table cho cây UPFP.
         *
         * @param db Cơ sở dữ liệu giao dịch (Transaction Database).
         * @param params Các tham số khai phá (minOcc, K...).
         * @param topKHeap Heap quản lý Top-K pattern (dùng để tính score và lưu trữ ban đầu).
         * @return {@link UPFPHeaderTable} chứa danh sách các item đã được sắp xếp (F-List) và thông tin thống kê của chúng.
    */
    public UPFPHeaderTable scan(Database db, Parameters params, TopKHeap topKHeap){
        // Map lưu trữ tạm thời support và danh sách timestamps
        Map<String, Double> expSupMap = new HashMap<>();
        Map<String, List<Integer>> tsMap = new HashMap<>();

        // Bước 1: Duyệt qua toàn bộ database để tính tổng xác suất và thu thập timestamps
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

        // Bước 2: Lọc bỏ item không đạt minOcc và tính điểm cho các item hợp lệ
        List<Pattern> itemsList = new ArrayList<>();
        for (String item : expSupMap.keySet()) {
            List<Integer> tsList = tsMap.get(item);

            // Cắt tỉa sớm dựa trên số lần xuất hiện tối thiểu (minOcc)
            if (tsList == null || tsList.size() < params.getMinOcc()) {
                continue;
            }

            double expSup = expSupMap.get(item);

            // Tính Periodicity (MaxGap) dựa trên timestamps đã thu thập
            double periodicity = calculateMaxGap(tsList, db.getMinTs(), db.getMaxTs());

            // Tạo đối tượng Pattern cho 1-itemset
            Pattern pattern = new Pattern(Collections.singletonList(item), expSup, periodicity, 0);

            // Sử dụng công thức Score từ TopKHeap
            double score = topKHeap.calculateScore(pattern);
            pattern.setScore(score);

            itemsList.add(pattern);
        }

        // Bước 3: Sắp xếp item theo score giảm dần
        itemsList.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        // Đưa K item tốt nhất vào Heap để thiết lập ngưỡng minScore ban đầu
        int limit = Math.min(itemsList.size(), params.getK());
        for (int i = 0; i < limit; i++) {
            topKHeap.add(itemsList.get(i)); 
        }

        // Bước 4: Xây dựng Header Table
        // Chỉ giữ lại những item có Score >= minScore hiện tại của Heap
        double scoreThreshold = topKHeap.getMinScore();
        List<Pattern> validPatterns = new ArrayList<>();
        UPFPHeaderTable headerTable = new UPFPHeaderTable();
        for (Pattern p : itemsList) {
            // Chỉ lưu những item có điểm score lớn hơn ngưỡng minScore hiện tại.
            if (p.getScore() >= scoreThreshold) {
                validPatterns.add(p); // 1-Itemsét này đủ điều kiện -> Thêm vào danh sách
                UPFPHeaderTable.ItemInfo itemInfo = new UPFPHeaderTable.ItemInfo();
                itemInfo.expSup = p.getEsup();
                itemInfo.periodicity = p.getEper();
                itemInfo.firstNode = null;
                
                headerTable.getTable().put(p.getItems().get(0), itemInfo);
            }
            else{
                break;
            }
        }
        validPatterns.sort((a, b) -> Double.compare(b.getEsup(), a.getEsup())); // Sắp xếp lại theo độ phổ biến (expSup)
        List<String> fList = new ArrayList<>();
        for (Pattern p : validPatterns) {
            fList.add(p.getItems().get(0));
        }
        // Lưu danh sách F-List vào Header Table
        headerTable.setfList(fList);

        return headerTable;
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
}
