# Object-Oriented Solutions to the Problem of Mining Top-K Periodic Itemsets from Uncertain Databases

## Giới thiệu

Project này tập trung vào việc xây dựng các giải pháp hướng đối tượng cho bài toán
khai thác Top-K tập mục định kỳ (Top-K Periodic Itemsets) trong các cơ sở dữ liệu
không chắc chắn (Uncertain Databases).

Người dùng không cần chỉ định:

- Ngưỡng hỗ trợ tối thiểu (minSup)
- Ngưỡng định kỳ tối đa (maxPer)

Thay vào đó, người dùng chỉ cần cung cấp:

- K: số lượng patterns mong muốn
- α (alpha): tham số điều chỉnh tỷ lệ giữa hai tiêu chí
  - Expected Support
  - Periodicity

Một hàm scoring tổng hợp được sử dụng để:

- Kết hợp hai tiêu chí
- Sắp xếp Top-K patterns
- Làm giá trị so sánh giữa các patterns

## Cách thực thi chương trình

### Chuẩn bị

````bash
cd Source

```md
### Dataset: Chess

- Tham số:
  - k ∈ {50, 60, 70}
  - α ∈ {5, 6, 7, 8}

```bash
java -cp bin MTPIU.experiment.ChessBaselineMiner
java -cp bin MTPIU.experiment.ChessStandardMiner
java -cp bin MTPIU.experiment.ChessOptimizedMiner

```md
### Dataset: Retail

- Tham số:
  - k ∈ {500, 1000, 1500}
  - α ∈ {5, 6, 7, 8}

```bash
java -cp bin MTPIU.experiment.RetailBaselineMiner
java -cp bin MTPIU.experiment.RetailStandardMiner
java -cp bin MTPIU.experiment.RetailOptimizedMiner

```md
### Dataset: Mushrooms

- Tham số:
  - k ∈ {100, 150, 200}
  - α ∈ {5, 6, 7, 8}

```bash
java -cp bin MTPIU.experiment.MushroomBaselineMiner
java -cp bin MTPIU.experiment.MushroomStandardMiner
java -cp bin MTPIU.experiment.MushroomOptimizedMiner

## Cách thêm tập dữ liệu mới

## Kiến trúc dự án

## Các thuật toán
````
