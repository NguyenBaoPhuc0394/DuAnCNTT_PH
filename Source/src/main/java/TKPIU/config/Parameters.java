package TKPIU.config;

/**
 * Lớp lưu trữ các giá trị đầu vào do người dùng đặt và các giá trị ngưỡng quan trọng thường xuyên được sử dụng
 */
public class Parameters {
    private int K; // Số lượng pattern mong muốn
    private double minSup; // Giá trị động được cập nhật trong quá trình khai thác mẫu
    private double maxPer; // Ngưỡng định kỳ tối đa (dynamic)
    private double alpha;
    private double beta;

    public Parameters(int k, double alpha, double beta){
        this.K = k;
        this.alpha = alpha;
        this.beta = beta;
        minSup = 0;
        maxPer = Double.MAX_VALUE;
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

    public void setK(int K){
        this.K = K;
    }

    
    //#endregion

    @Override
    public String toString() {
        return "Parameters [K=" + K + ", minSup=" + minSup + ", maxPer=" + maxPer + "]";
    }
    
}
