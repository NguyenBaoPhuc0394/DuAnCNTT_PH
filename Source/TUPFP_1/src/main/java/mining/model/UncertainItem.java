package main.java.mining.model;

/*
    Lớp đại diện cho các item, lưu giữ các thông tin tên của item và xác suất đi kèm với nó.
*/
public class UncertainItem {
    private final String item;
    private final double probability;

    public UncertainItem(String item, double probability) {
        this.item = item;
        this.probability = probability;
    }

    public String getItem() {
        return item;
    }

    public double getProbability() {
        return probability;
    }
    
}
