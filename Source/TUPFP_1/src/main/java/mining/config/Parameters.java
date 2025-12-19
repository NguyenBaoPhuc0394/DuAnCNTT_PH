package main.java.mining.config;

/*
    Lớp lưu trữ các giá trị đầu vào do người dùng đặt và các giá trị ngưỡng quan trọng thường xuyên được sử dụng
*/
public class Parameters {
    private final int K; // Số lượng pattern đủ điều kiện
    private final double maxPer; // Ngưỡng định kỳ tối đa
    private double minSup; // Giá trị động được cập nhật trong quá trình khai thác mẫu
    private int dbMinTimestamp; // thời điểm đầu tiên trong CSDL, là timestamp ở giao dịch đầu tiên
    private int dbMaxTimestamp;  // thời điểm cuối cùng trong CSDL, là timestamp ở giao dịch cuối cùng.
    private final int minOcc = 3; // ngưỡng xuất hiện tối thiểu của một pattern (điều kiện để loại bỏ triệt để những mẫu pattern chỉ xuất hiện 2 lần nhưng may mắn lọt vô top K)

    public Parameters(int K, double maxExpPer) {
        this.K = K;
        this.maxPer = maxExpPer;
        this.minSup = 0.0;
    }

    //#region getter

    public int getK() {
        return K;
    }

    public double getMaxPer() {
        return maxPer;
    }

    public double getMinSup() {
        return minSup;
    }

    public int getMinOcc(){
        return this.minOcc;
    }

    public int getDbMaxTimestamp() {
        return dbMaxTimestamp;
    }

    public int getDbMinTimestamp() {
        return dbMinTimestamp;
    }

    //#endregion

    //#region setter

    public void setMinSup(double minSup) {
        this.minSup = minSup;
    }

    public void setDbMinTimestamp(int dbMinTimestamp) {
        this.dbMinTimestamp = dbMinTimestamp;
    }

    public void setDbMaxTimestamp(int dbMaxTimestamp) {
        this.dbMaxTimestamp = dbMaxTimestamp;
    }

    //#endregion setter

    @Override
    public String toString(){
        return "K = "+ K + ", maxExpPer = " + maxPer + " , minSup = " + minSup;
    }
}
