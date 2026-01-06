package MTPIU.core.pattern;

import java.util.List;

/**
 * Lớp đại diện cho các pattern, gồm danh sách các item, expected support và per của nó.
 */
public class Pattern {
    private final List<String> items;
    private final double esup;
    private final double eper;
    private double score;

    public Pattern(List<String> items, double esup, double eper, double score) {
        this.items = List.copyOf(items);
        this.esup = esup;
        this.eper = eper;
        this.score = score;
    }

    //#region getter

    public List<String> getItems() {
        return items;
    }

    public double getEsup() {
        return esup;
    }

    public double getEper() {
        return eper;
    }

    public double getScore() {
        return score;
    }
    //#endregion getter

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return items + " (sup=" + String.format("%.3f", esup) + ", per=" + String.format("%.3f", eper) + ", score=" + String.format("%.3f", score) + ")";
    }

}
