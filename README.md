# DuAnCNTT_PH

**Các giải pháp hướng đối tượng cho bài toán khai thác Top-K tập mục định kỳ từ các cơ sở dữ liệu không chắc chắn**

(**Object-Oriented Solutions to the Problem of Mining Top-K Periodic Itemsets from Uncertain Databases**).

- Dự án này triển khai thuật toán khai thác các mẫu (pattern) có Độ Hỗ trợ Kỳ vọng (Expected Support - Esup) cao nhất (Top-K) đồng thời thỏa mãn ràng buộc về Tính Định kỳ Kỳ vọng (Expected Periodicity - Eper) trong các cơ sở dữ liệu có tính chất bất định và gắn nhãn thời gian. Thuật toán sử dụng cấu trúc cây nén **UPFP-Tree** và cơ chế **Top-K Min-Heap** để cắt tỉa động, loại bỏ sự cần thiết của ngưỡng hỗ trợ tối thiểu ($\text{minSup}$) đầu vào.

## Hướng dẫn cách chạy chương trình trên command line
- Tải dự án về -> Di chuyển vào ...\source_code\Source\TUPFP_1
- Chạy dòng lệnh sau trên terminal: java -cp bin main/java/mining.Main
- Lúc này sẽ hiện lên yêu cầu nhập 2 tham số là Top-K và maxPer, người dùng chỉ cần nhập giá trị mong muốn là được (Ví dụ Top-K=10, maxPer=1000)

## Cấu trúc dự án (Branch tupfp)

- Dự án được tổ chức theo cấu trúc chuẩn của Java/Maven/Gradle, tập trung vào thư mục `src/main/java/mining/`.

### Thư mục config

- Chứa các lớp cài đặt và quản lý tham số thuật toán.
- `Parameters.java` : Định nghĩa các tham số đầu vào quan trọng như **K** (số lượng mẫu cần tìm), **maxPer** (ngưỡng định kỳ tối đa cho phép), và quản lý **minSup** động (ngưỡng hỗ trợ tối thiểu hiện tại được lấy từ `TopKHeap`).

### Thư mục resources

- Chứa các file data để chạy thuật toán, hiện tại thuật toán đang chạy trên tập retail

### Các Lớp Mô hình Dữ liệu (`model/`)

- `Transaction.java` : Đại diện cho một giao dịch trong cơ sở dữ liệu, bao gồm **ID giao dịch (`tid`)** , **thời điểm xảy ra (`timestamp`)** , và danh sách các `UncertainItem`.
- `UncertainItem.java` : Đại diện cho một mục (item) với thuộc tính **tên mục (`item`)** và **xác suất xuất hiện (`probability`)** tương ứng trong giao dịch.
- `Pattern.java` : Đại diện cho mẫu kết quả được khai thác, lưu trữ danh sách các mục, **Độ Hỗ trợ Kỳ vọng (Esup)** và **Tính Định kỳ Kỳ vọng (Eper)** đã tính toán được.

### Cấu trúc Cây UPFP-Tree (`tree/`)

- `UPFPNode.java` : Đại diện cho một nút trong cây. Lưu trữ **tên mục** , **expSupCap** (giới hạn trên hỗ trợ dùng cho cắt tỉa), **timestamps** (tại nút đuôi), và con trỏ **nodeLink** (liên kết đến nút tiếp theo cùng item).
- `UPFPHeaderTable.java` : Bảng tiêu đề chứa thông tin tổng hợp của các mục tần suất cao (expSup, periodicity) và con trỏ **firstNode** đến nút đầu tiên của mục đó trong cây.
- `UPFPTree.java` : Cấu trúc cây chính, chứa **nút gốc (`root`)** và **bảng tiêu đề (`headerTable`)** .

### Cơ chế Top-K và Thuật toán (`topk/` và `algorithm/`)

- `TopKHeap.java` : Triển khai cấu trúc **Min-Heap** (Hàng đợi Ưu tiên Tối thiểu) để duy trì Top-K mẫu có Esup cao nhất được tìm thấy. Lớp này cung cấp **ngưỡng minSup động** để cắt tỉa hiệu quả.
- `Scanner.java` : Thực hiện **lượt quét đầu tiên** cơ sở dữ liệu. Tính toán Esup và Eper cho các mục đơn, lọc các mục không thỏa mãn và sắp xếp chúng để tạo ra `UPFPHeaderTable` ban đầu.
- `TreeBuilder.java` : **Xây dựng UPFP-Tree** ban đầu. Sắp xếp các item trong mỗi giao dịch theo thứ tự F-List (thứ tự giảm dần của Esup) và chèn vào cây, cập nhật `expSupCap` và `timestamps`.
- `Miner.java` : **Lõi thuật toán Pattern Growth đệ quy** . Thực hiện khai thác, xây dựng Conditional Pattern Base, xây dựng Conditional Tree, và áp dụng cơ chế **cắt tỉa** bằng ESC (Expected Support Cap) dựa trên **minSup động** từ `TopKHeap`.
