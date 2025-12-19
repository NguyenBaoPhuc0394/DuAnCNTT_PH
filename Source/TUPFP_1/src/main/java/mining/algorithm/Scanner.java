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


/**
 * Lớp Scanner: Thực hiện lượt quét đầu tiên (First Pass) qua cơ sở dữ liệu.
 * Nhiệm vụ: 
 * 1. Tính toán Expected Support (ExpSup) và Periodicity (Per) cho từng item.
 * 2. Lọc bỏ các item không đạt yêu cầu (rác).
 * 3. Tạo HeaderTable và danh sách F-List (Frequent List) để chuẩn bị cho việc xây cây.
 */
public class Scanner {

    /**
     * Hàm chính thực hiện quét DB.
     * @param db Danh sách các giao dịch.
     * @param params Các tham số cấu hình (minOcc, maxExpPer, minSup...).
     * @return Bảng HeaderTable chứa các item hợp lệ đã được sắp xếp.
     */

    // Map tra cứu xác suất gốc P(item, timestamp).
    // Dùng để tính Esup của trong Miner.
    // Key: Timestamp -> Value: Map<ItemName, Probability>
    public static Map<Integer, Map<String, Double>> probLookupMap = new HashMap<>();

    public UPFPHeaderTable scanFirstPass(List<Transaction> db, Parameters params) {
        
        probLookupMap.clear(); // Reset map
        // Map lưu trữ Expected Support (Tổng xác suất) của từng item
        Map<String, Double> expSupMap = new HashMap<>();
        // Map lưu trữ danh sách các Timestamps mà item xuất hiện (có xác suất > 0)
        Map<String, List<Integer>> tsMap = new HashMap<>();
        
        /* * 1. Xác định thời gian của Database 
         * Để tính Gap đầu và Gap cuối chính xác, ta cần biết thời điểm bắt đầu và kết thúc của toàn bộ dữ liệu.
         */
        int minTs = db.isEmpty() ? 0 : db.get(0).getTimestamp(); // Thời điểm giao dịch đầu tiên
        int maxTs = db.isEmpty() ? 0 : db.get(db.size() - 1).getTimestamp(); // Thời điểm giao dịch cuối cùng
        // Lưu lại vào params để Miner sử dụng sau này (cho việc tính Gap đầu/cuối của Pattern)
        params.setDbMinTimestamp(minTs); 
        params.setDbMaxTimestamp(maxTs); 

        /*
         * 2. QUÉT DB LẦN 1 
         * Duyệt qua từng giao dịch để thu thập thông tin cơ bản: ExpSup và Timestamps.
         */
        for (Transaction t : db) { // Duyệt qua từng transaction
            int timestamp = t.getTimestamp(); // Lấy giá trị timestamp của transaction hiện tại

            // Map con lưu xác suất của các item trong transaction này
            Map<String, Double> itemProbs = new HashMap<>();

            for (UncertainItem itemProb : t.getItems()) { // Duyệt qua từng item trong transaction
                double prob = itemProb.getProbability(); // Lấy xác suất tồn tại của item tại transaction hiện tại
                String item = itemProb.getItem(); // Lấy tên item

                if (prob > 0.0) { 
                    // Tính expSup
                    // Cộng tất cả xác suất tồn tại của item trong tất cả transaction mà nó xuất hiện
                    expSupMap.merge(item, prob, Double::sum); 
                    
                    // Thu thập timestamps: Thêm thời điểm hiện tại vào danh sách của item đó.
                    tsMap.computeIfAbsent(item, k -> new ArrayList<>()).add(timestamp);

                    // Lưu xác suất vào map con
                    itemProbs.put(item, prob);
                }
            }

            // Lưu map con vào probLookupMap toàn cục
            if (!itemProbs.isEmpty()) {
                probLookupMap.put(timestamp, itemProbs);
            }
        }

        /*
         * 3. Tính toán Periodicity (MaxGap) cho từng item
         * Duyệt qua danh sách các item đã tìm thấy để tính chu kỳ và lọc rác.
         */
        Map<String, Double> perMap = new HashMap<>(); // Lưu giá trị maxPer cho từng item
        
        for (String item : expSupMap.keySet()) { // Duyệt qua từng item

            List<Integer> tsList = tsMap.get(item); // Lấy danh sách timestamp của từng item
            
            // Kiểm tra số lượng xuất hiện thực tế (MinOcc).
            // Nếu item xuất hiện quá ít lần (< 3), coi như là nhiễu -> Loại bỏ ngay.
            // (Không cần tính Periodicity cho nó nữa để tiết kiệm thời gian).
            if (tsList == null || tsList.size() < params.getMinOcc())
            {
                // Nếu không xuất hiện đủ minOcc lần thì bỏ qua
                continue;
            }

            // Đảm bảo timestamps được sắp xếp tăng dần để tính Gap
            Collections.sort(tsList);

            int maxGap = 0;

            // a. Tính Gap Nội bộ (Internal Gaps)
            // Khoảng cách giữa các lần xuất hiện liên tiếp.
            for (int i = 0; i < tsList.size() - 1; i++) {
                int gap = tsList.get(i+1) - tsList.get(i);
                if (gap > maxGap) {
                    maxGap = gap;
                }
            }
            
            // b. Tính Gap Đầu và Gap Cuối (Start/End Gaps)
            // Gap từ đầu DB đến lần xuất hiện đầu tiên của item
            int startGap = tsList.get(0) - minTs;
            // Gap từ lần xuất hiện cuối cùng đến hết DB
            int endGap = maxTs - tsList.get(tsList.size() - 1);

            // Periodicity cuối cùng là MAX của tất cả các loại Gap
            maxGap = Math.max(maxGap, Math.max(startGap, endGap));

            // Lưu maxPer cho item
            perMap.put(item, (double) maxGap);
        }

        /*
         * 4. Xây dựng HeaderTable và Flist
         * Lọc lại lần cuối và tạo cấu trúc dữ liệu cho cây.
         */
        UPFPHeaderTable header = new UPFPHeaderTable();
        
        for (String item : expSupMap.keySet()) { // Duyệt qua từng item
            double expSup = expSupMap.get(item); // Lấy expected support của item

            // Lấy maxGap vừa tính được. Nếu item bị loại ở bước MinOcc thì per mặc định là MAX_VALUE.
            double per = perMap.getOrDefault(item, Double.MAX_VALUE); 

            // Điều kiện lọc
            // Nếu thỏa mãn cả hai -> Item này là HỢP LỆ.
            if (expSup >= params.getMinSup() && per <= params.getMaxPer()) {
                UPFPHeaderTable.ItemInfo itemInfo = new UPFPHeaderTable.ItemInfo(); // itemInfo là thông tin của một item sẽ được lưu trong headerTable
                itemInfo.expSup = expSup; 
                itemInfo.periodicity = per;
                itemInfo.firstNode = null; // firstNode của một item là lần đầu tiên xuất hiện node của nó trong cây, khởi tạo ban đầu là null, sẽ được gán khi xây cây (TreeBuilder)
                header.getTable().put(item, itemInfo); // Lưu tên item và thông tin của nó vào headerTable
            }
        }

        // Sắp xếp Flist:
        // Sắp xếp các item hợp lệ theo thứ tự ExpSup giảm dần (L-Order).
        // Đây là bước bắt buộc để xây dựng cây FP-Tree nén hiệu quả.
        header.sortFlist();

        // Trả về headerTable và Flist thông qua header
        return header;
    }
}