package com.promptcraft.client.util;

import com.promptcraft.PromptCraft;
import com.promptcraft.config.ConfigManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Performance monitoring utility for PromptCraft
 */
public class PerformanceMonitor {
    private static final Map<String, Long> startTimes = new HashMap<>();
    private static final Map<String, Long> totalTimes = new HashMap<>();
    private static final Map<String, Integer> callCounts = new HashMap<>();

    /**
     * Starts timing an operation
     */
    public static void startTiming(String operation) {
        if (shouldMonitor()) {
            startTimes.put(operation, System.currentTimeMillis());
        }
    }

    /**
     * Ends timing an operation and logs the result
     */
    public static void endTiming(String operation) {
        if (shouldMonitor() && startTimes.containsKey(operation)) {
            long startTime = startTimes.remove(operation);
            long duration = System.currentTimeMillis() - startTime;

            // Update statistics
            totalTimes.put(operation, totalTimes.getOrDefault(operation, 0L) + duration);
            callCounts.put(operation, callCounts.getOrDefault(operation, 0) + 1);

            // Log if duration is significant
            if (duration > 100) { // More than 100ms
                PromptCraft.LOGGER.debug("Operation '{}' took {}ms", operation, duration);
            }
        }
    }

    /**
     * Times a runnable operation
     */
    public static void timeOperation(String operation, Runnable runnable) {
        startTiming(operation);
        try {
            runnable.run();
        } finally {
            endTiming(operation);
        }
    }

    /**
     * Gets the average time for an operation
     */
    public static double getAverageTime(String operation) {
        if (!totalTimes.containsKey(operation) || !callCounts.containsKey(operation)) {
            return 0.0;
        }

        long totalTime = totalTimes.get(operation);
        int count = callCounts.get(operation);

        return (double) totalTime / count;
    }

    /**
     * Gets the total time for an operation
     */
    public static long getTotalTime(String operation) {
        return totalTimes.getOrDefault(operation, 0L);
    }

    /**
     * Gets the call count for an operation
     */
    public static int getCallCount(String operation) {
        return callCounts.getOrDefault(operation, 0);
    }

    /**
     * Logs performance statistics
     */
    public static void logStatistics() {
        if (!shouldMonitor()) {
            return;
        }

        PromptCraft.LOGGER.info("=== PromptCraft Performance Statistics ===");

        for (String operation : totalTimes.keySet()) {
            long totalTime = getTotalTime(operation);
            int count = getCallCount(operation);
            double avgTime = getAverageTime(operation);

            PromptCraft.LOGGER.info("{}: {} calls, {}ms total, {}ms average",
                    operation, count, totalTime, String.format("%.2f", avgTime));
        }

        PromptCraft.LOGGER.info("==========================================");
    }

    /**
     * Clears all performance statistics
     */
    public static void clearStatistics() {
        startTimes.clear();
        totalTimes.clear();
        callCounts.clear();
    }

    /**
     * Checks if performance monitoring should be enabled
     */
    private static boolean shouldMonitor() {
        try {
            ConfigManager.PromptCraftConfig config = ConfigManager.getConfig();
            return config != null && config.enableLogging;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Monitors memory usage
     */
    public static void logMemoryUsage(String context) {
        if (!shouldMonitor()) {
            return;
        }

        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();

        PromptCraft.LOGGER.debug("Memory usage [{}]: Used: {}MB, Free: {}MB, Total: {}MB, Max: {}MB",
                context,
                usedMemory / 1024 / 1024,
                freeMemory / 1024 / 1024,
                totalMemory / 1024 / 1024,
                maxMemory / 1024 / 1024);
    }

    /**
     * Checks if memory usage is high
     */
    public static boolean isMemoryUsageHigh() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();

        double usagePercentage = (double) usedMemory / maxMemory;
        return usagePercentage > 0.8; // 80% threshold
    }

    /**
     * Suggests garbage collection if memory usage is high
     */
    public static void suggestGarbageCollection() {
        if (isMemoryUsageHigh()) {
            PromptCraft.LOGGER.debug("High memory usage detected, suggesting garbage collection");
            System.gc();
        }
    }
}
