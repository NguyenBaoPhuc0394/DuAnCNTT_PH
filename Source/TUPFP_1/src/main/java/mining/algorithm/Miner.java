package main.java.mining.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import main.java.mining.config.Parameters;
import main.java.mining.model.Pattern;
import main.java.mining.topk.TopKHeap;
import main.java.mining.tree.UPFPHeaderTable;
import main.java.mining.tree.UPFPNode;
import main.java.mining.tree.UPFPTree;

/**
 * Lớp Miner: Thực hiện thuật toán khai thác UPFP-Growth đệ quy để tìm Top-K pattern.
 */
public class Miner {
    private final TopKHeap topK; // Heap lưu trữ K pattern tốt nhất (dùng để lấy minSup động)
    private final Parameters params; // Các tham số cấu hình (K, maxExpPer, minOcc...)

    public Miner(Parameters params){
        this.params = params;   
        this.topK = new TopKHeap(params.getK());
    }

    /**
     * Cấu trúc hỗ trợ lưu trữ thông tin một nhánh (Conditional Path).
     * Được trích xuất từ cây lớn để xây dựng cây con.
     */
    private static class ConditionalPath {
        List<String> path;  // Các item trên đường đi (Prefix Items)
        double cap;         // Trọng số của đường đi (Lấy từ PIC của node Suffix)
        List<Integer> timestamps; // Danh sách thời gian (Lấy từ node Suffix)

        ConditionalPath(List<String> path, double cap, List<Integer> timestamps) {
            this.path = path;
            this.cap = cap;
            this.timestamps = timestamps;
        }
    }

    /**
     * Hàm bắt đầu quá trình khai thác.
     */
    public List<Pattern> mine(UPFPTree tree) {
        params.setMinSup(0); // Khởi tạo minSup động ban đầu là 0
        mineRecursive(tree, new ArrayList<>()); // Bắt đầu hàm đệ quy với prefix rỗng
        return topK.getTopK(); // Trả về danh sách kết quả cuối cùng
    }

    /**
     * Hàm đệ quy chính (Core Recursive Function).
     * @param tree Cây hiện tại (có thể là Cây Gốc hoặc Cây Có Điều Kiện).
     * @param prefix Mẫu tiền tố đã khai thác được (ví dụ: đang xét {A, B} thì prefix là {A, B}).
     */
    private void mineRecursive(UPFPTree tree, List<String> prefix) {
        List<String> fList = tree.headerTable.getFlist();

        System.out.println("Processing prefix: " + prefix + ", FList size: " + fList.size());//

        // Duyệt suffix từ dưới lên (Bottom-up Approach)
        // Duyệt các item trong HeaderTable theo thứ tự ngược (từ ExpSup thấp nhất lên cao nhất).
        // Item đang xét đóng vai trò là suffix (Hậu tố).
        for (int i = fList.size() - 1; i >= 0; i--) {
            String suffix = fList.get(i);

            // 1. Tạo Pattern tiềm năng mới: Prefix cũ + Suffix đang xét
            // Ví dụ: Prefix cũ là {A}, Suffix là B -> Pattern mới là {A, B}
            List<String> newPrefix = new ArrayList<>(prefix);
            newPrefix.add(suffix);

            // 2. Xây dựng CPB (Conditional Pattern Base)
            // Tìm tất cả các đường đi trong cây dẫn đến node 'suffix'.
            // Mỗi đường đi kèm theo một trọng số (PIC của suffix tại đường đi đó).
            List<ConditionalPath> condPaths = buildConditionalPatternBase(tree, suffix);
            if (condPaths.isEmpty()) continue; // Nếu không có đường đi nào, bỏ qua

            // // 3. Cắt tỉa bằng ESC (Expected Support Cap) 
            // double ubEsup = calculateUBEsup(condPaths);
            // // Nếu Cận trên < minSup hiện tại (của Top-K) -> Chắc chắn nhánh này vô vọng -> Cắt tỉa
            // if (ubEsup < params.getMinSup()) {
            //     continue; // bỏ nhánh
            // }

            // 3. Cắt tỉa bằng ESC (Expected Support Cap), không áp dụng cho 1-itemset
            double ubEsup = 0.0;
            if (!prefix.isEmpty()) { 
                ubEsup = calculateUBEsup(condPaths);
                // Nếu Cận trên < minSup hiện tại (của Top-K) -> Chắc chắn nhánh này vô vọng -> Cắt tỉa
                if (ubEsup < params.getMinSup()) {
                    continue; // bỏ nhánh
                }
            }

            // 4. Tính toán Periodicity và cắt tỉa dựa trên MinOcc 
            // Hàm này sẽ trả về giá trị MaxGap thực tế.
            // Nếu vi phạm minOcc, nó trả về Double.MAX_VALUE -> Tự động bị prune ở bước so sánh sau.
            double eper = calculateMaxGapPer(condPaths);

            // 5. Tính Esup cho chính Pattern này
            double esup = calculateEsup(condPaths, newPrefix);
                
            // 6. Cập nhật Top-K Heap
            // Kiểm tra lần cuối xem pattern có đủ điều kiện vào Top-K không.
            if (esup >= params.getMinSup() && eper <= params.getMaxPer()) {
                Pattern p = new Pattern(newPrefix, esup, eper);
                topK.add(p);
                // Nếu Heap đã đầy K phần tử, cập nhật ngưỡng minSup
                // bằng với phần tử nhỏ nhất trong Heap để cắt tỉa mạnh hơn ở các bước sau.
                if (topK.getLength() == params.getK()) {
                    params.setMinSup(topK.getMinSup());
                }
            }
            
            // 7. Xây dựng Cây Có Điều Kiện (Conditional Tree)
            // Hợp nhất các đường đi CPB thành một cây mới.
            // Cây này đại diện cho "Ngữ cảnh của Suffix" (những gì xuất hiện trước Suffix).
            // Node suffix sẽ không nằm trong cây này
            UPFPTree condTree = buildConditionalTree(condPaths);

            System.out.println("Pattern: " + newPrefix + ", UB_Esup: " + ubEsup + ", MinSup: " + params.getMinSup());//
            
            // 8. Gọi Đệ quy 
            // Nếu cây con vẫn còn item (tức là còn Prefix Items để mở rộng), tiếp tục đào sâu.
            if (!condTree.headerTable.getTable().isEmpty()) {
                mineRecursive(condTree, newPrefix);
            }
        }
    }
    
    /**
     * Hàm lấy các đường đi tiền tố (Prefix Paths) của Suffix từ cây hiện tại.
     */
    private List<ConditionalPath> buildConditionalPatternBase(UPFPTree tree, String suffix) {
        List<ConditionalPath> paths = new ArrayList<>();

        // Lấy node đầu tiên của Suffix từ HeaderTable
        UPFPHeaderTable.ItemInfo info = tree.headerTable.getItemInfo(suffix);
        if (info == null || info.firstNode == null) {
            return paths; 
        }
        
        UPFPNode node = info.firstNode;

        // Duyệt ngang qua danh sách liên kết các node 'suffix' trong cây (qua nodeLink)
        while (node != null) {
            List<String> path = new ArrayList<>();

            // Lấy trọng số của nhánh này (chính là PIC của node Suffix tại vị trí này)
            double cap = node.getExpSupCap();  

            // Lấy timestamp (được lưu tại node Suffix)
            List<Integer> ts = new ArrayList<>(node.getTimestamps());
            UPFPNode parent = node.getParent();

            // Đi ngược từ node Suffix lên Root để lấy đường đi tiền tố
            while (parent != null && parent != tree.root) {
                path.add(parent.getItem());
                parent = parent.getParent();
            }

            // Đảo ngược danh sách để có thứ tự đúng: Root -> ... -> Parent of Suffix
            Collections.reverse(path);

            if (!path.isEmpty()) {
                paths.add(new ConditionalPath(path, cap, ts));
            }

            // Nhảy sang node 'suffix' tiếp theo trong cây
            node = node.getNodeLink();
        }
        return paths;
    }

    /**
     * Hàm xây dựng Cây Có Điều Kiện từ danh sách CPB.
     * Vừa xây cây, vừa tạo HeaderTable mới.
     */
    private UPFPTree buildConditionalTree(List<ConditionalPath> paths) {
        UPFPTree condTree = new UPFPTree(); // Khởi tạo conditional tree, ban đầu chỉ có nút root

        // Khởi tạo headerTable cho conditional tree, nó sẽ được xây dựng song song việc xây cây conditional
        // Sau khi xây xong nó sẽ được gán vào cho cây
        UPFPHeaderTable condHeader = new UPFPHeaderTable(); 

        // Duyệt qua từng đường đi trong CPB
        for (ConditionalPath cp : paths) {
            UPFPNode current = condTree.root;

            // Chèn lần lượt các item của đường đi vào cây mới
            for (String item : cp.path) {
                UPFPNode child = current.getChildren().get(item);
                if (child == null) {
                    // Tạo node mới nếu chưa có
                    child = new UPFPNode(item);
                    current.getChildren().put(item, child);
                    child.setParent(current);

                    // Đồng thời cập nhật HeaderTable cho cây mới ngay tại đây (On-the-fly)
                    UPFPHeaderTable.ItemInfo info = condHeader.getItemInfo(item);
                    if (info == null) { 
                        info = condHeader.addItem(item, 0.0); 
                    }

                    // Nối nodeLink
                    child.setNodeLink(info.firstNode);
                    info.firstNode = child;
                }
                
                // Cộng dồn trọng số
                // 'cp.cap' là trọng số của Suffix trong ngữ cảnh nhánh này.
                // Ta cộng nó vào expSupCap của tất cả các node tiền tố (A, B...) trong cây mới.
                // Để node A trong cây mới đại diện cho "Tổng hỗ trợ của A khi đi cùng Suffix".
                child.setExpSupCap(child.getExpSupCap() + cp.cap);

                // Xuống cấp tiếp theo
                current = child; 
            }

            // Sau khi chèn xong đường đi, gán timestamps vào node cuối cùng của đường đi tiền tố.
            // (Node này đóng vai trò là đuôi trong cây conditional)
            if (current != condTree.root) {
                current.getTimestamps().addAll(cp.timestamps);
                // condTree.suffixNode = current;
            }
        }

        // Sau khi xây xong cây, sắp xếp HeaderTable để chuẩn bị cho đệ quy
        condHeader.sortFlist(); 
        // Gán HeaderTable cho cây
        condTree.attachConditionalHeader(condHeader);

        return condTree;
    }

    /**
     * Tính ESC (Expected Support Cap) - Cận trên hỗ trợ
     */
    private double calculateUBEsup(List<ConditionalPath> paths) {
        double total = 0.0;
        for (ConditionalPath cp : paths) {
            total += cp.cap; // cp.cap chính là PIcap của suffix tại path này
        }
        return total;
    }

    // Hàm tính Esup bằng cách tra cứu Scanner.probLookupMap
    private double calculateEsup(List<ConditionalPath> paths, List<String> currentPattern) {
        double totalExactEsup = 0.0;
        
        // Thu thập tất cả các timestamp duy nhất mà pattern xuất hiện
        // (Do cấu trúc nén path, mỗi path có thể chứa nhiều ts, ta gộp lại hết)
        TreeSet<Integer> uniqueTimestamps = new TreeSet<>();
        for (ConditionalPath cp : paths) {
            uniqueTimestamps.addAll(cp.timestamps);
        }

        // Duyệt qua từng thời điểm xuất hiện
        for (Integer ts : uniqueTimestamps) {
            // Lấy Map xác suất tại thời điểm ts từ Scanner
            Map<String, Double> itemProbsAtTs = Scanner.probLookupMap.get(ts);
            
            if (itemProbsAtTs == null) continue;

            double productProb = 1.0;
            boolean allItemsFound = true;

            // Tính tích xác suất của tất cả item trong pattern tại thời điểm ts
            // P(Pattern) = P(item1) * P(item2) * ...
            for (String item : currentPattern) {
                Double prob = itemProbsAtTs.get(item);
                if (prob == null) {
                    // Nếu một item trong pattern không có mặt tại ts này thì xác suất = 0.
                    allItemsFound = false;
                    break;
                }
                productProb *= prob;
            }

            if (allItemsFound) {
                totalExactEsup += productProb;
            }
        }
        
        return totalExactEsup;
    }

    /**
     * Hàm tính MaxGap thực tế.
     * Tích hợp kiểm tra MinOcc: Trả về MAX_VALUE nếu không đủ số lần xuất hiện.
     */
    private double calculateMaxGapPer(List<ConditionalPath> paths) {
        TreeSet<Integer> sortedTimestamps = new TreeSet<>();
        for (ConditionalPath cp : paths) {
            sortedTimestamps.addAll(cp.timestamps);
        }

        // Kiểm tra MinOcc ngay tại đây
        // Nếu không đủ số lần xuất hiện -> Coi như MaxGap vô cùng lớn -> Sẽ bị cắt tỉa.
        if (sortedTimestamps.size() < params.getMinOcc()) {
            return Double.MAX_VALUE;
        }

        List<Integer> tsList = new ArrayList<>(sortedTimestamps);
        int maxGap = 0;
        
        // Tính Gap nội bộ
        for (int i = 0; i < tsList.size() - 1; i++) {
            int gap = tsList.get(i+1) - tsList.get(i);
            if (gap > maxGap) {
                maxGap = gap;
            }
        }
        
        // Tính Gap đầu và Gap cuối
        int startGap = tsList.get(0) - params.getDbMinTimestamp();
        int endGap = params.getDbMaxTimestamp() - tsList.get(tsList.size() - 1);
        maxGap = Math.max(maxGap, Math.max(startGap, endGap));

        return (double) maxGap;
    }
}