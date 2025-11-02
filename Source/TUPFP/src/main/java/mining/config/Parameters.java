package main.java.mining.config;

public class Parameters {
    public final int K;
    public final double maxExpPer;
    public double minSup; // động

    public Parameters(int K, double maxExpPer) {
        this.K = K;
        this.maxExpPer = maxExpPer;
        this.minSup = 0.0;
    }

    @Override
    public String toString(){
        return "K = "+ K + ", maxExpPer = " + maxExpPer + " , minSup = " + minSup;
    }
}
