package main.java.mining.config;

public class Parameters {
    private final int K;
    private final double maxExpPer;
    private double minSup; // động

    public Parameters(int K, double maxExpPer) {
        this.K = K;
        this.maxExpPer = maxExpPer;
        this.minSup = 0.0;
    }

    public int getK() {
        return K;
    }

    public double getMaxExpPer() {
        return maxExpPer;
    }

    public double getMinSup() {
        return minSup;
    }

    public void setMinSup(double minSup) {
        this.minSup = minSup;
    }
    
    @Override
    public String toString(){
        return "K = "+ K + ", maxExpPer = " + maxExpPer + " , minSup = " + minSup;
    }
}
