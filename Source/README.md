# Các giải pháp hướng đối tượng cho bài toán khai thác Top-K tập mục định kỳ từ các cơ sở dữ liệu không chắc chắn

(Object-Oriented Solutions to the Problem of Mining Top-K Periodic Itemsets from Uncertain Databases)

---

## Giới thiệu

Project thực hiện các giải pháp khai thác Top-K periodic itemsets trong tập dữ liệu không chắc chắn.

Người dùng không cần nhập ngưỡng hỗ trợ tối thiểu (minSup) và ngưỡng định kỳ tối đa (maxPer) mà chỉ nhập:

- K: số patterns mong muốn
- α (alpha): tham số thể hiện tỷ lệ giữa expected support và periodicity

Một hàm scoring tổng hợp được sử dụng để:

- Kết hợp hai tiêu chí
- Sắp xếp Top-K patterns
- Làm giá trị so sánh giữa các patterns trong quá trình khai thác

Báo cáo chi tiết:
https://www.overleaf.com/project/68e00c38a2ed0af01ecbf0ee

---

## Cách thực thi chương trình (For User)

### Chuẩn bị môi trường

Sau khi giải nén project:

1. Di chuyển vào thư mục Source
2. Giải nén data.zip (Extract Here)
3. Mở Command Prompt tại thư mục Source
4. Kiểm tra thư mục bin

Nếu chưa có, thực hiện:

```bash
mkdir bin
javac -d bin src\main\java\TKPIU\**\*.java
```

Nếu lỗi ở câu lệnh trên, thực hiện:

```bash
dir /s /b src\main\java\*.java > sources.txt
javac -d bin @sources.txt
```

---

#### Tham số đầu vào

Chương trình yêu cầu 2 tham số:

- **K**:
  + Số tự nhiên > 0
- **α** (alpha):
  + Giá trị trong khoảng 0 → 9
  + α càng cao → ưu tiên expected support
  + α càng thấp → ưu tiên periodicity

Chương trình sẽ yêu cầu nhập lần lượt hai tham số trên. Nếu nhập sai, người dùng phải nhập lại.

---

### Chạy thực nghiệm theo từng tập dữ liệu

#### Retail Dataset

```bash
Chạy với Baseline TKPIU:
java -cp bin TKPIU.experiment.RetailBaselineMiner

Chạy với Standard TKPIU:
java -cp bin TKPIU.experiment.RetailStandardMiner

Chạy với Optimized TKPIU:
java -cp bin TKPIU.experiment.RetailOptimizedMiner
```

Testcase đề xuất:

- K = {100,200,300,400}
- α = {4,5,6,7}

---

#### T10I4D100K Dataset

```bash
Chạy với Baseline TKPIU:
java -cp bin TKPIU.experiment.T10I4D100KBaselineMiner

Chạy với Standard TKPIU:
java -cp bin TKPIU.experiment.T10I4D100KStandardMiner

Chạy với Optimized TKPIU:
java -cp bin TKPIU.experiment.T10I4D100KOptimizedMiner
```

Testcase đề xuất:

- K = {200,300,400,500}
- α = {4,5, 6, 7}

---

#### Mushrooms Dataset

```bash
Chạy với Optimized TKPIU:
java -cp bin TKPIU.experiment.MushroomsOptimizedMiner

Testcase đề xuất:
- K = {75, 100, 125, 150}
- α = {4,5,6,7}
```

---

#### Chess Dataset

```bash
Chạy với Optimized TKPIU:
java -cp bin TKPIU.experiment.ChessOptimizedMiner
```

Testcase đề xuất:

- K = {20,25,30,35}
- α = {4,5, 6, 7}

---

### Kết quả thực nghiệm

Kết quả được lưu tại:
data\output

---

## Kiến trúc hệ thống

#### Core Data

**Scanner**

- Vai trò: Quét dữ liệu từ cơ sở dữ liệu.
- Phương thức: `scan(db: Database)`
- Ý nghĩa: Tách riêng bước đọc và tiền xử lý dữ liệu.

**Database**

- Vai trò: Đại diện cho toàn bộ cơ sở dữ liệu giao dịch.
- Thuộc tính: `transactions: List<Transaction>`
- Phương thức: `size(): int`

**Transaction**

- Vai trò: Biểu diễn một giao dịch.
- Thuộc tính: `tid: int`, `items: List<UncertainItem>`

**UncertainItem**

- Vai trò: Biểu diễn một mục với xác suất xuất hiện.
- Thuộc tính: `itemId: int`, `probability: double`

#### Tree Structure

**TreeBuilder**

- Vai trò: Xây dựng cây UPFP.
- Phương thức: `buildTree(db: Database, params: Parameters): UPFPTree`

**UPFPTree**

- Vai trò: Cấu trúc cây FP mở rộng cho dữ liệu không chắc chắn.
- Thuộc tính: `root: UPFPNode`, `headerTable: UPFPHeaderTable`

**UPFPNode**

- Vai trò: Nút trong cây UPFP.
- Thuộc tính: `itemId: int`, `count: double`, `parent: UPFPNode`, `children: Map<int, UPFPNode>`, `nodeLink: UPFPNode`

**UPFPHeaderTable**

- Vai trò: Bảng header của cây UPFP.
- Thuộc tính: `table: Map<int, UPFPNode>`

#### Mining Algorithm

**AbstractMiner**

- Vai trò: Định nghĩa khung chung cho các thuật toán khai thác top-K.
- Thuộc tính: `params: Parameters`, `topKHeap: TopKHeap`
- Phương thức: `mine(tree: UPFPTree): List<Pattern>`

#### Top-K Management, Configuration, Pattern

**TopKHeap**

- Vai trò: Quản lý tập top-K mẫu.
- Thuộc tính: `k: int`, `heap: PriorityQueue<Pattern>`
- Phương thức: `add(pattern: Pattern)`, `getTopK(): List<Pattern>`

**Parameters**

- Vai trò: Lưu trữ tham số cấu hình.
- Thuộc tính: `minSup: double`, `k: int`, `threshold: double`

**Pattern**

- Vai trò: Đại diện cho một tập mục phổ biến.
- Thuộc tính: `items: List<int>`, `support: double`
