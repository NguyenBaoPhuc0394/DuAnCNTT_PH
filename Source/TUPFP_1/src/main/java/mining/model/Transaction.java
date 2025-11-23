package main.java.mining.model;

import java.util.List;

/*
    Lớp đại diện cho các Transaction, lưu trữ các thông tin liên quan như mã giao dịch, timestamp, danh sách item xuất hiện.
    Được sử dụng để lưu từng transaction từ file dữ liệu.
*/
public class Transaction {
    private final int tid;
    private final int timestamp;
    private final List<UncertainItem> items;

    public Transaction(int tid, int timestamp, List<UncertainItem> items) {
        this.tid = tid;
        this.timestamp = timestamp;
        this.items = items;
    }

    public int getTid() {
        return tid;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public List<UncertainItem> getItems() {
        return items;
    }
    
}
