package main.java.mining.model;

public class UncertainItem {
    public final String item;
    public final double probability;

    public UncertainItem(String item, double probability) {
        this.item = item;
        this.probability = probability;
    }
}
