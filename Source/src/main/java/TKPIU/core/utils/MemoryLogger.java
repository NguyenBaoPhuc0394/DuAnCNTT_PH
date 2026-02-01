package TKPIU.core.utils;

public class MemoryLogger {
    private static MemoryLogger instance = new MemoryLogger();
    private double maxMemory = 0;

    public static MemoryLogger getInstance() {
        return instance;
    }

    public void reset() {
        this.maxMemory = 0;
    }

    public void checkMemory() {
        Runtime runtime = Runtime.getRuntime();
        
        double currentMemory = (double) (runtime.totalMemory() - runtime.freeMemory()) / 1024d / 1024d;
        if (currentMemory > maxMemory) {
            maxMemory = currentMemory;
        }
    }

    public double getCurrentMemory() {
        Runtime runtime = Runtime.getRuntime();
        return (double) (runtime.totalMemory() - runtime.freeMemory()) / 1024d / 1024d;
    }

    public double getMaxMemory() {
        return maxMemory;
    }
}
