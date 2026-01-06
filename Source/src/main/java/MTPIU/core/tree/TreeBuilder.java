package MTPIU.core.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import MTPIU.core.database.Database;
import MTPIU.core.database.Transaction;
import MTPIU.core.database.UncertainItem;

/**
 * Lớp TreeBuilder: Chịu trách nhiệm xây dựng cây UPFP-Tree gốc từ cơ sở dữ liệu.
 */
public class TreeBuilder {

    /**
     * Hàm chính để xây dựng cây UPFP-Tree từ Database.
     * @param db Danh sách các giao dịch.
     * @param header HeaderTable đã được tạo từ bước Scanner (chứa thông tin ExpSup toàn cục).
     * @return Cây {@link UPFPTree} đã hoàn thiện.
     */
    public UPFPTree buildTree(Database db, UPFPHeaderTable header) {
        
        UPFPTree tree = new UPFPTree(); // Khởi tạo cây rỗng, ban đầu chỉ có node root = null

        // Sao chép cấu trúc HeaderTable từ Scanner sang cây. Để cây biết thứ tự ưu tiên (L-Order) của các item.
        tree.headerTable.setTable(header.getTable()); // gán headerTable vừa tạo được ở Scanner cho headerTable của cây
        tree.headerTable.setfList(new ArrayList<>(header.getFlist())); // Gán Flist cho cây
        List<String> fList = header.getFlist();

        Map<String, Integer> rankMap = new HashMap<>();
        for (int i = 0; i < fList.size(); i++) {
            rankMap.put(fList.get(i), i);
        }
        // Duyệt qua từng giao dịch trong DB để đưa vào cây
        for(Transaction t : db.getTransactions()){ 
            //#region Bước 1: Lọc và sắp xếp item trong mỗi giao dịch
            List<UncertainItem> sortedItems = new ArrayList<>();

            // a. Lọc item: Chỉ giữ lại các item nằm trong F-List
            for(UncertainItem ui : t.getItems()){
                if(rankMap.containsKey(ui.getItem())){
                    sortedItems.add(ui);
                }
            }

            // b. Sắp xếp (L-Order):
            // Sắp xếp các item trong giao dịch theo thứ tự xuất hiện trong F-List, theo thứ tự ExpSup giảm dần
            // Item phổ biến nhất sẽ nằm gần gốc cây nhất. Tối đa hóa việc chia sẻ đường đi (nén cây).
            sortedItems.sort((a, b) -> {
                int rankA = rankMap.get(a.getItem());
                int rankB = rankMap.get(b.getItem());
                return Integer.compare(rankA, rankB);
            });
            //#endregion

            //#region Bước 2: Thực hiện chèn giao dịch vào cây
            if (!sortedItems.isEmpty()) {
                insertTransaction(tree, sortedItems, t.getTimestamp());
            }
            //#endregion
        }

        return tree;
    }

    /**
     * Hàm chèn một giao dịch (đã lọc và sắp xếp) vào cây.
     * Đây là nơi tính toán giá trị PIC (Prefixed Item Cap).
     */
    public void insertTransaction(UPFPTree tree, List<UncertainItem> items, int timestamp) {
       
        UPFPNode current = tree.root; // Bắt đầu từ nút gốc (root)

        // Biến này lưu xác suất lớn nhất của các item tiền tố (Prefix) trong giao dịch này.
        double prefixMaxProb = 1.0; // Dùng để tính PIC. Khởi tạo là 1.0 (coi như Root luôn có xác suất 1.0).

        // Duyệt qua từng item trong giao dịch
        for (int i = 0; i < items.size(); i++) {
            UncertainItem ui = items.get(i);
            String item = ui.getItem();
            double prob = ui.getProbability();

            // 1. Tính toán PIC (Prefixed Item Cap)
            // Công thức: PIC(item) = Prob(item) * Max(Prob của các item đứng trước nó trong path)
            // Đây là giới hạn trên xác suất xuất hiện của pattern kết thúc tại item này trong ngữ cảnh của giao dịch hiện tại.
            double currentPicap = prob * prefixMaxProb;

            // 2. Tìm hoặc tạo node trên cây
            // Kiểm tra xem từ node hiện tại (current) đã có nhánh con nào tên là 'item' chưa.
            UPFPNode child = current.getChildren().get(item);
            if (child == null) { // Nếu chưa có, tạo node mới
                child = new UPFPNode(item);
                current.getChildren().put(item, child); // thêm node con cho node cha (current)
                child.setParent(current); // Liên kết với node cha

                // Cập nhật nodeLink trong HeaderTable:
                // Nối node mới này vào đầu danh sách liên kết của item đó.  
                UPFPHeaderTable.ItemInfo info = tree.headerTable.getItemInfo(item);
                if (info != null) {
                    child.setNodeLink(info.firstNode);
                    info.firstNode = child;
                }
            }

            // 3. Cộng dồn trọng số (ExpSupCap)
            // Node này có thể được chia sẻ bởi nhiều giao dịch khác nhau 
            // Ta cộng thêm PIC của giao dịch hiện tại vào giá trị ExpSupCap đang có của node.
            // Tổng này sau này dùng làm ESC (Upper Bound Support) cho nhánh cây.
            child.setExpSupCap(child.getExpSupCap() + currentPicap);
            
            // 4. Lưu TIMESTAMP (Chỉ tại Node Đuôi) 
            // Chỉ lưu timestamp nếu đây là item cuối cùng của giao dịch (Tail Node).
            // Các node trung gian không cần lưu để tiết kiệm bộ nhớ.
            if (i == items.size() - 1 && prob > 0.0) {
                child.getTimestamps().add(timestamp);
            }

            // 5. Cập nhật prefixMaxProb
            // Cập nhật max xác suất cho vòng lặp tiếp theo (cho item con của item này).
            // Max mới = Max(Max cũ, Prob của item hiện tại).
            if(i == 0){
                prefixMaxProb = prob;
            }else
                prefixMaxProb = Math.max(prefixMaxProb, prob);

            // Di chuyển con trỏ current xuống node con vừa xử lý để tiếp tục
            current = child;
        }
    }
}
