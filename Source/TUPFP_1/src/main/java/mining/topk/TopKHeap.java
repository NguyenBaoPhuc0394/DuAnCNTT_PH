package main.java.mining.topk;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import main.java.mining.model.Pattern;

/*
    Lưu trữ và quản lý danh sách top K heap
*/
public class TopKHeap {
    private final PriorityQueue<Pattern> heap;
    private final int K;

    public TopKHeap(int K) {
        /// Heap mặc định sắp xếp tăng dần, giá trị có Esup nhỏ nhất sẽ được xếp ở đầu (head)
        this.K = K; 
        this.heap = new PriorityQueue<>(K, Comparator.comparingDouble(p -> p.getEsup()));
    }

    public void add(Pattern p) {
        /// Thêm một pattern vào heap
        /// Nếu số lượng vượt quá K thì loại bỏ pattern ở đầu heap (pattern có Esup nhỏ nhất)
        heap.offer(p);
        if (heap.size() > K) {
            heap.poll();
        }
    }

    public double getMinSup() {
        /// Lấy giá trị Esup nhỏ nhất của pattern trong heap
        return heap.isEmpty() ? 0.0 : heap.peek().getEsup();
    }

    public int getLength(){
        return heap.size();
    }

    public List<Pattern> getTopK() {
        /// Trả về danh sách Top K 
        List<Pattern> result = new ArrayList<>(heap);
        result.sort((a, b) -> Double.compare(b.getEsup(), a.getEsup()));
        return result;
    }
}
