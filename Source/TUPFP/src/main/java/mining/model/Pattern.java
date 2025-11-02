package main.java.mining.model;

import java.util.List;

public class Pattern {
    public final List<String> items;
    public final double esup;
    public final double eper;

    public Pattern(List<String> items, double esup, double eper) {
        this.items = items;
        this.esup = esup;
        this.eper = eper;
    }
}
