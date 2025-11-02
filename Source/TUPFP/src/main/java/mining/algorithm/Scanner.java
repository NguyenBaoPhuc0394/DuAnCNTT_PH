package main.java.mining.algorithm;

import java.util.HashMap;
import java.util.List;

import main.java.mining.config.Parameters;
import main.java.mining.model.Transaction;
import main.java.mining.model.UncertainItem;
import main.java.mining.tree.UPFPHeaderTable;
import main.java.mining.tree.UPFPNode;
import main.java.mining.tree.UPFPHeaderTable.ItemInfo;

public class Scanner {
    public UPFPHeaderTable scanFirstPass(List<Transaction> db, Parameters params) {
        // Tính expSup, per cho từng item
        // Lọc: expSup >= minSup && per <= maxExpPer
        // Trả về header table + F-List
        
        return null;
    }

    
}
