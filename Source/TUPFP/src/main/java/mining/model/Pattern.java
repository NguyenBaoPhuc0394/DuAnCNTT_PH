package main.java.mining.model;

import java.util.List;

public class Pattern {
    public final List<String> items;
    private final double esup;
    private final double eper;

    public Pattern(List<String> items, double esup, double eper) {
        this.items = List.copyOf(items);
        this.esup = esup;
        this.eper = eper;
    }
    

    public List<String> getItems() {
        return items;
    }


    public double getEsup() {
        return esup;
    }


    public double getEper() {
        return eper;
    }


    @Override
    public String toString() {
        return items + " (sup=" + String.format("%.3f", esup) + ", per=" + String.format("%.3f", eper) + ")";
    }
}
