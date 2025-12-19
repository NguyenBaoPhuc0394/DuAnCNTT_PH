package main.java.mining.model;

/*
    Lớp đại diện cho các item, lưu giữ các thông tin gồm tên của item và xác suất đi kèm với nó.
*/
public class UncertainItem {
    private final String item;
    private final double probability;

    public UncertainItem(String item, double probability) {
        this.item = item;
        this.probability = probability;
    }

    //#region getter

    public String getItem() {
        return item;
    }

    public double getProbability() {
        return probability;
    }

    //#endregion getter

    @Override
    public String toString(){
        return "item: " + this.item + ", probability: " + this.probability;
    }
    
}
