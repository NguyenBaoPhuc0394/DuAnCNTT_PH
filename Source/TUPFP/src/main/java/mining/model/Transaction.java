package main.java.mining.model;

import java.util.List;

public class Transaction {
    public final int tid;
    public final int timestamp;
    public final List<UncertainItem> items;

    public Transaction(int tid, int timestamp, List<UncertainItem> items) {
        this.tid = tid;
        this.timestamp = timestamp;
        this.items = items;
    }
}
