package main.java.mining.config;

/*
    Lớp lưu trữ các giá trị đầu vào do người dùng đặt và các giá trị ngưỡng quan trọng thường xuyên được sử dụng
*/
public class Parameters {
    private final int K; // Số lượng pattern đủ điều kiện
    private final double maxPer; // Ngưỡng định kỳ tối đa
    private double minSup; // động
    private int dbMinTimestamp; // thời điểm đầu tiên trong CSDL, là timestamp ở giao dịch đầu tiên
    private int dbMaxTimestamp;  // thời điểm cuối cùng trong CSDL, là timestamp ở giao dịch cuối cùng.
    private final int minOcc = 3; // ngưỡng xuất hiện tối thiểu của một pattern.

    public Parameters(int K, double maxExpPer) {
        this.K = K;
        this.maxPer = maxExpPer;
        this.minSup = 0.0;
    }

    public int getK() {
        return K;
    }

    public double getMaxPer() {
        return maxPer;
    }

    public double getMinSup() {
        return minSup;
    }

    public void setMinSup(double minSup) {
        this.minSup = minSup;
    }
    
    
    public int getDbMinTimestamp() {
        return dbMinTimestamp;
    }

    public void setDbMinTimestamp(int dbMinTimestamp) {
        this.dbMinTimestamp = dbMinTimestamp;
    }

    public int getDbMaxTimestamp() {
        return dbMaxTimestamp;
    }

    public void setDbMaxTimestamp(int dbMaxTimestamp) {
        this.dbMaxTimestamp = dbMaxTimestamp;
    }

    public int getMinOcc(){
        return this.minOcc;
    }

    @Override
    public String toString(){
        return "K = "+ K + ", maxExpPer = " + maxPer + " , minSup = " + minSup;
    }
}
