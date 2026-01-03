package main.java.MTPIU.algorithms.topk;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import main.java.MTPIU.config.Parameters;
import main.java.MTPIU.core.database.Database;
import main.java.MTPIU.core.pattern.Pattern;

public class TopKHeap {
    private final int K;
    private PriorityQueue<Pattern> heap;
    private double minScore = 0.0;

    public TopKHeap(int K) {
        this.K = K;
        // Heap sắp xếp tăng dần theo Score -> Phần tử có Score thấp nhất nằm ở root (peek)
        // Để khi đầy, ta loại bỏ phần tử này.
        this.heap = new PriorityQueue<>(K, Comparator.comparingDouble(this::calculateScore));
    }

    public double calculateScore(Pattern p){
        Parameters params = Parameters.getInstance();
        Database db = Database.getInstance();
        double alpha = params.getAlpha();
        double beta = params.getBeta();
        double N = (double) db.getMaxTs() - db.getMinTs() + 1;
        if (N <= 1){
            N = 1; // Tránh chia cho 0 
        } 
        double normEsup = p.getEsup() / N;
        double normPer = 1 - ((p.getEper() - 1.0) / (N - 1.0));
        if (normPer < 0) normPer = 0;

        return alpha * normEsup + beta * normPer;
    }

    /**
     * Thêm Pattern vào Heap và cập nhật ngưỡng động
     */
    public boolean add(Pattern p) {
        // Nếu Heap chưa đầy -> Thêm pattern
        if (heap.size() < K) {
            heap.offer(p);
            if (heap.size() == K) {
                updateThresholds();
            }
            return true;
        }

        // Nếu Heap đã đầy, so sánh với pattern tệ nhất (root)
        // double score = calculateScore(p);
        double score = p.getScore();
        Pattern lowest = heap.peek();
        double lowestScore = lowest.getScore();
        // System.out.println("Score:"+ score + ", lowest: "+lowestScore);
        if (score > lowestScore) {
            heap.poll(); // Loại bỏ pattern tệ nhất
            heap.offer(p); // Thêm pattern mới
            updateThresholds(); // Cập nhật lại ngưỡng chặn dưới
            return true;
        }

        return false;
    }

    private void updateThresholds() {
        if (heap.isEmpty()) return;

        if (heap.size() < K) {
            // Parameters.getInstance().setMinSup(0.0);
            // Parameters.getInstance().setMaxPer(Double.MAX_VALUE);
            return;
        }

        // Điểm sàn hiện tại để lọt vào Top-K
        // this.minScore = calculateScore(heap.peek());
        this.minScore = heap.peek().getScore();
        
        Parameters params = Parameters.getInstance();
        Database db = Database.getInstance();
        double alpha = params.getAlpha();
        double beta = params.getBeta();
        double N = (double) db.getMaxTs() - db.getMinTs() + 1;

        double currentMinSup = params.getMinSup();
        double currentMaxPer = params.getMaxPer();

        // 1. Cập nhật Derived MIN_SUP
        // Điều kiện: alpha * (sup/N) + beta * 1 >= minScore (Giả sử Per tốt nhất = 1)
        // => sup >= (minScore - beta) * N / alpha
        double newMinSup = (minScore - beta) * N / alpha;
        // System.out.println(term1);
        // double newMinSup = Math.max(0.0, term1);
        // System.out.println(term1);
        params.setMinSup(Math.max(currentMinSup, newMinSup));

        // 2. Cập nhật Derived MAX_PER
        // Điều kiện: alpha * 1 + beta * (1 - (per-1)/(N-1)) >= minScore (Giả sử Sup tốt nhất = N hay nSup=1)
        // Lưu ý: Sup=N là chặn trên lỏng, thực tế sup chỉ bằng sup của cha, nhưng để tính global threshold ta dùng 1.
        // Biến đổi toán học:
        // (per - 1)/(N-1) <= 1 - (minScore - alpha)/beta
        // per <= 1 + (N-1) * [ 1 - (minScore - alpha)/beta ]
        
        double term2 = (minScore - alpha) / beta;
        double newMaxPer = 1.0 + (N - 1.0) * (1.0 - term2);
        // System.out.println(newMaxPer);
        
        // Chặn trên không được vượt quá độ dài DB
        // if (newMaxPer > N) newMaxPer = N;
        // if (newMaxPer < 1) newMaxPer = 1; // Không thể nhỏ hơn 1
        
        params.setMaxPer(Math.min(currentMaxPer, newMaxPer));
    }

    public List<Pattern> getTopKPatterns() {
        // Trả về list đã sort giảm dần 
        List<Pattern> list = new ArrayList<>(heap);
        list.sort((p1, p2) -> Double.compare(calculateScore(p2), calculateScore(p1)));
        return list;
    }

    public double getMinScore() {
        return minScore;
    }

    public boolean isFull(){
        return this.heap.size() == this.K;
    }
}
