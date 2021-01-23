package utilities;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.sun.management.OperatingSystemMXBean;

public class LoadUtility {

    private static CPULoadGenerator cpuLoadGeneratorObj;

    public static CPULoadGenerator getCPULoadGenerator() {
        if(cpuLoadGeneratorObj == null) {
            cpuLoadGeneratorObj = new CPULoadGenerator();
        }
        return cpuLoadGeneratorObj;
    }

    /**
     * Generates Load on the CPU by keeping it busy for the given load percentage
     * Taken from https://gist.github.com/SriramKeerthi/0f1513a62b3b09fecaeb and modified it as needed.
     */
    public final static class CPULoadGenerator extends Thread {

        final private OperatingSystemMXBean osMxBean;
        private boolean cpuLoadInProgress;
        private double load;
        private long duration;

        protected CPULoadGenerator() {
            osMxBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            cpuLoadInProgress = false;
        }

        public synchronized void start(double load, long duration) throws InterruptedException {
            this.load = load;
            this.duration = duration;
            start();
        }

        @Override
        public synchronized void run() {

            long threadsNotAlive = 0;
            int logicalCoresCount = osMxBean.getAvailableProcessors();

            System.out.println(osMxBean.getAvailableProcessors());

            // Initialize all the threads (One per logical core)
            List<CPULoadThread> cpuLoadThreads = IntStream.range(0, logicalCoresCount)
                    .mapToObj(logicalCore -> new CPULoadThread("Thread" + logicalCore, load, duration))
                    .collect(Collectors.toList());

            // Invoke all the initialized threads
            for (CPULoadThread cpuLoadThread:cpuLoadThreads) {
                cpuLoadThread.start();
            }

            System.out.println("Waiting for threads to complete.");

            // Wait till all threads complete execution
            try {
                Thread.sleep(duration);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            while(threadsNotAlive == logicalCoresCount) {
                threadsNotAlive = cpuLoadThreads.stream()
                        .map(CPULoadThread::isAlive)
                        .filter(isAlive -> !isAlive)
                        .count();
            }
        }

        /**
         * Thread that actually generates the given load
         */
        private class CPULoadThread extends Thread {
            private double load;
            private long duration;

            /**
             * Constructor which creates the thread
             * @param name Name of this thread
             * @param load Load % that this thread should generate
             * @param duration Duration that this thread should generate the load for
             */
            public CPULoadThread(String name, double load, long duration) {
                super(name);
                this.load = load;
                this.duration = duration;
            }

            /**
             * Generates the load when run
             */
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                try {
                    // Loop for the given duration
                    while (System.currentTimeMillis() - startTime < duration) {
                        // Every 100ms, sleep for the percentage of unladen time
                        if (System.currentTimeMillis() % 100 == 0) {
                            Thread.sleep((long) Math.floor((1 - load) * 100));
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
