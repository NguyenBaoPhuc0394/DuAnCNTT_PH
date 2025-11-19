package main.java.mining.topk;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import main.java.mining.model.Pattern;

public class TopKHeap {
    private final PriorityQueue<Pattern> heap;
    private final int K;

    public TopKHeap(int K) {
        this.K = K;
        this.heap = new PriorityQueue<>(K, Comparator.comparingDouble(p -> p.getEsup()));
    }

    public void add(Pattern p) {
        heap.offer(p);
        if (heap.size() > K) {
            heap.poll();
        }
    }

    public double getMinSup() {
        return heap.isEmpty() ? 0.0 : heap.peek().getEsup();
    }

    public int getLength(){
        return heap.size();
    }

    public List<Pattern> getTopK() {
        List<Pattern> result = new ArrayList<>(heap);
        result.sort((a, b) -> Double.compare(b.getEsup(), a.getEsup()));
        return result;
    }

    // public boolean add(Pattern p);
    // public double getMinSup(); // esup thấp nhất
    // public List<Pattern> getTopK();
}
