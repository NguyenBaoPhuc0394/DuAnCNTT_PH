package TKPIU.core.database;


import java.util.List;
import java.util.Map;

/**
 * Lớp đại diện cho Cơ sở dữ liệu, lưu trữ danh sách giao dịch, lưu trữ thông tin xác suất của item tại thời điểm timestamp
 * 
 */
public class Database {

    private List<Transaction> transactions;
    private Map<Integer, Map<String, Double>> probLookupMap;
    private int minTs;
    private int maxTs;

    public Database(List<Transaction> transactions, Map<Integer, Map<String, Double>> probLookupMap, int minTs, int maxTs) {
        this.transactions = transactions;
        this.probLookupMap = probLookupMap;
        this.minTs = minTs;
        this.maxTs = maxTs;
    }

    //#region getters
    public List<Transaction> getTransactions() {
        return transactions;
    }

    public Map<Integer, Map<String, Double>> getProbLookupMap() {
        return probLookupMap;
    }

    public int getMinTs() {
        return minTs;
    }

    public int getMaxTs() {
        return maxTs;
    }
    //#endregion
}
