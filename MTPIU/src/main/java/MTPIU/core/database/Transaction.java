package main.java.MTPIU.core.database;

import java.util.List;

public class Transaction {
    private final int tid;
    private final int timestamp;
    private final List<UncertainItem> items;

    public Transaction(int tid, int timestamp, List<UncertainItem> items) {
        this.tid = tid;
        this.timestamp = timestamp;
        this.items = items;
    }

    //#region getter

    public int getTid() {
        return tid;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public List<UncertainItem> getItems() {
        return items;
    }
    
    //#endregion getter

    @Override
    public String toString(){
        return "id: "+ this.tid + ", timestamp: " + this.timestamp + ", items: " + this.items;
    }
}
