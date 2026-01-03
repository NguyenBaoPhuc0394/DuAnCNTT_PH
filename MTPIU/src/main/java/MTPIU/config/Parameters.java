package main.java.MTPIU.config;

public class Parameters {
    private static Parameters instance;
    private final int K;
    private double minSup;
    private double maxPer;
    private int minOcc;
    private double alpha;
    private double beta;

    public Parameters(int k, double alpha, double beta){
        this.K = k;
        this.alpha = alpha;
        this.beta = beta;
        minOcc = 3;
        minSup = 0;
        maxPer = Double.MAX_VALUE;
    }

    // Khởi tạo lần đầu
    public static synchronized Parameters init(int k, double alpha, double beta){
        if(instance == null){
            instance = new Parameters(k, alpha, beta);
        }
        return instance;
    }

    public static Parameters getInstance(){
        if(instance == null){
            throw new IllegalStateException("Parameters chưa được init!");
        }
        return instance;
    }


    //#region getters & setters
    public int getK() {
        return K;
    }

    public double getMinSup() {
        return minSup;
    }

    public void setMinSup(double minSup) {
        this.minSup = minSup;
    }

    public double getMaxPer() {
        return maxPer;
    }

    public void setMaxPer(double maxPer) {
        this.maxPer = maxPer;
    }

    public int getMinOcc() {
        return minOcc;
    }

    public void setMinOcc(int minOcc) {
        this.minOcc = minOcc;
    }

    
    //#endregion

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getBeta() {
        return beta;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }

    @Override
    public String toString() {
        return "Parameters [K=" + K + ", minSup=" + minSup + ", maxPer=" + maxPer + ", minOcc=" + minOcc + "]";
    }
    
}
