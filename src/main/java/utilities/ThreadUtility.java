package utilities;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import com.sun.management.ThreadMXBean;

public class ThreadUtility {

    private final static ThreadMXBean thbean = (ThreadMXBean) ManagementFactory.getThreadMXBean();
    private final static RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
    private final static long cpuSamplingInterval = 1000; // 1,000 ms = 1s
    private final static ConcurrentHashMap<Long, Double> threadCPUUsages = new ConcurrentHashMap<>();
    private final static boolean runThreadCpuUsageCollector = true;
    private final static int default_thread_stack_depth = 15;

    private final static ExecutorService executorService = Executors.newFixedThreadPool(1);
    private final static Future cpuUsageCollectorFuture = executorService.submit(() -> {
        try {
            ThreadUtility.runCpuUsageCollector();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    });

    public static long[] getAllThreadsId() {
        return thbean.getAllThreadIds();
    }

    public static ThreadInfo[] getAllThreadsInfo() {
        return thbean.getThreadInfo(thbean.getAllThreadIds(), default_thread_stack_depth);
    }

    public static long getAllocatedMemoryInBytes(long threadId) {
        return thbean.getThreadAllocatedBytes(threadId);
    }

    public static long getPercentageOfHeapMemoryUsed(long threadId) {
        return 0;
    }

    public static double getCPUUsage(long threadId) throws InterruptedException {
        double threadCPUUsage = threadCPUUsages.getOrDefault(threadId, -1.0);
        if(threadCPUUsage == -1) {
            System.out.println("Thread " + threadId + " no longer exists or is no longer accessible! => Unable to retrieve CPU usage");
        }
        return threadCPUUsage;
    }

    private static void runCpuUsageCollector() throws InterruptedException {
        System.out.println("CPU usage collection has started...");
        while(runThreadCpuUsageCollector) {
            long[] threadIds = getAllThreadsId();
            long initialUpTime = runtimeMxBean.getUptime();
            long finalUpTimeInMilliSeconds = 0;


            Map<Long, Long> initialThreadCPUTimes = Arrays.stream(threadIds)
                    .boxed()
                    .collect(
                            Collectors.toMap(
                                    threadId -> threadId,
                                    threadId -> thbean.getThreadCpuTime(threadId)
                            )
                    );

            Thread.sleep(cpuSamplingInterval);

            Map<Long, Long> currentThreadCPUTimes = Arrays.stream(threadIds)
                    .boxed()
                    .collect(
                            Collectors.toMap(
                                    threadId -> threadId,
                                    threadId -> thbean.getThreadCpuTime(threadId)
                            )
                    );

            finalUpTimeInMilliSeconds = runtimeMxBean.getUptime();

            for (long threadId : currentThreadCPUTimes.keySet()) {

                if (initialThreadCPUTimes.containsKey(threadId)) {
//                    System.out.println("Thread ID : " + threadId);
//                    System.out.println("initialThreadCPUTimes : " + initialThreadCPUTimes);
//                    System.out.println("currentThreadCPUTimes : " + currentThreadCPUTimes);
//                    System.out.println("initialUptTime : " + initialUpTime);
//                    System.out.println("finalUptTime : " + finalUpTime);
//                    System.out.println();
                    double cpuThreadTimeInMilliSeconds = (currentThreadCPUTimes.get(threadId) - initialThreadCPUTimes.get(threadId)) / (1000.0 * 1000.0);
                    double samplingDurationInMilliSeconds = finalUpTimeInMilliSeconds - initialUpTime;
                    double threadCPUUsage = cpuThreadTimeInMilliSeconds * 100.0 / samplingDurationInMilliSeconds;
                    threadCPUUsages.put(threadId, threadCPUUsage);
                }
            }
        }

        // System.out.println("Thread CPU usage collector has been stopped.");
    }

    @Override
    public void finalize() {
        try {
            cpuUsageCollectorFuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

}
