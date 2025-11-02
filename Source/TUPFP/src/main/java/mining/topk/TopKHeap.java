package main.java.mining.topk;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import main.java.mining.model.Pattern;

public class TopKHeap {
    private PriorityQueue<Pattern> heap;
    private int capacity;

    public TopKHeap(int K) {
        this.capacity = K;
        this.heap = new PriorityQueue<>(K, Comparator.comparingDouble(p -> p.esup));
    }

    // public boolean add(Pattern p);
    // public double getMinSup(); // esup thấp nhất
    // public List<Pattern> getTopK();
}
