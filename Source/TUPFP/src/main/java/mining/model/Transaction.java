package main.java.mining.model;

import java.util.List;

public class Transaction {
    private final int tid;
    private final int timestamp;
    public final List<UncertainItem> items;

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
