package main.java.mining.model;

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
