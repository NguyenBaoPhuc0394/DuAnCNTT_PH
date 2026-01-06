package MTPIU.algorithms.topk;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import MTPIU.config.Parameters;
import MTPIU.core.database.Database;
import MTPIU.core.pattern.Pattern;

/**
 * Quản lý danh sách top-K patterns, xử lý logic khi thêm một pattern vào danh sách
 * Thực hiện tính và cập nhật ngưỡng score, minSup, maxPer
 */
public class TopKHeap {
    private final int K;
    private PriorityQueue<Pattern> heap;
    private double minScore = 0.0;
    private Parameters params;
    private Database db;

    public TopKHeap(int K, Parameters params, Database db) {
        this.K = K;
        this.params = params;
        this.db = db;
        // Heap sắp xếp tăng dần theo Score -> Phần tử có Score thấp nhất nằm ở root (peek). Để khi đầy, ta loại bỏ phần tử này.
        this.heap = new PriorityQueue<>(K, Comparator.comparingDouble(this::calculateScore));
    }

    // Hàm tính điểm score của một pattern
    public double calculateScore(Pattern p){
        double alpha = this.params.getAlpha();
        double beta = this.params.getBeta();
        double N = (double) this.db.getMaxTs() - this.db.getMinTs() + 1;
        if (N <= 1){
            N = 1; // Tránh chia cho 0 
        } 
        double normEsup = p.getEsup() / N;
        double normPer = 1 - ((p.getEper() - 1.0) / (N - 1.0));
        if (normPer < 0) normPer = 0;

        return alpha * normEsup + beta * normPer;
    }

    public double calculateScore(double expSup, double per) {
        double N = (double) this.db.getMaxTs() - this.db.getMinTs() + 1;
        if (N <= 1) N = 1;
        
        double normEsup = expSup / N;
        double normPer = 1 - ((per - 1.0) / (N - 1.0));
        if (normPer < 0) normPer = 0;

        return this.params.getAlpha() * normEsup + this.params.getBeta() * normPer;
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

        // Nếu Heap đã đầy, so sánh với pattern tệ nhất 
        double score = p.getScore();
        Pattern lowest = heap.peek();
        double lowestScore = lowest.getScore();
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
            return;
        }

        // Điểm sàn hiện tại để lọt vào Top-K
        this.minScore = heap.peek().getScore();
        double alpha = this.params.getAlpha();
        double beta = this.params.getBeta();
        double N = (double) this.db.getMaxTs() - db.getMinTs() + 1;

        double currentMinSup = this.params.getMinSup();
        double currentMaxPer = this.params.getMaxPer();

        // 1. Cập nhật Derived minSup
        double newMinSup = (minScore - beta) * N / alpha;
        this.params.setMinSup(Math.max(currentMinSup, newMinSup));

        // 2. Cập nhật Derived maxPer
        double term2 = (minScore - alpha) / beta;
        double newMaxPer = 1.0 + (N - 1.0) * (1.0 - term2);
        
        this.params.setMaxPer(Math.min(currentMaxPer, newMaxPer));
    }

    public List<Pattern> getTopKPatterns() {
        List<Pattern> list = new ArrayList<>(heap);
        list.sort((p1, p2) -> Double.compare(calculateScore(p2), calculateScore(p1)));
        return list; // Trả về list đã sort giảm dần 
    }

    public double getMinScore() {
        return minScore;
    }

    public boolean isFull(){
        return this.heap.size() == this.K;
    }
}
