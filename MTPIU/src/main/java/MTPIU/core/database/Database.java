package main.java.MTPIU.core.database;


import java.util.List;
import java.util.Map;

public class Database {
    private static Database instance;

    private List<Transaction> transactions;
    private Map<Integer, Map<String, Double>> probLookupMap;
    private int minTs;
    private int maxTs;

    // Constructor private để ngăn tạo mới bên ngoài
    private Database(List<Transaction> transactions, Map<Integer, Map<String, Double>> probLookupMap, int minTs, int maxTs) {
        this.transactions = transactions;
        this.probLookupMap = probLookupMap;
        this.minTs = minTs;
        this.maxTs = maxTs;
    }

    // Khởi tạo lần đầu với tham số
    public static synchronized Database init(List<Transaction> transactions, Map<Integer, Map<String, Double>> probLookupMap, int minTs, int maxTs) {
        if (instance == null) {
            instance = new Database(transactions, probLookupMap, minTs, maxTs);
        }
        return instance;
    }

    // Truy cập instance đã khởi tạo
    public static Database getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Database chưa được init!");
        }
        return instance;
    }

    // Getters
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
}
