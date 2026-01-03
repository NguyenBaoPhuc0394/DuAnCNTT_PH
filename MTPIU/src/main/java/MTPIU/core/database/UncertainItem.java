package main.java.MTPIU.core.database;

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
