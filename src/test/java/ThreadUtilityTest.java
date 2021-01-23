import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.Arrays;

import com.sun.management.ThreadMXBean;
import utilities.LoadUtility;
import utilities.ThreadUtility;

public class ThreadUtilityTest {

    @Test
    public void testThreadList() throws InterruptedException {
//        for(Thread thread:ThreadUtility.getAllThreads()) {
//            System.out.println(thread.getName());
//            System.out.println(thread.getStackTrace());
//        }

        LoadUtility.CPULoadGenerator cpuLoadGenerator = LoadUtility.getCPULoadGenerator();
        System.out.println("LOL1");
        cpuLoadGenerator.start(0.8, 100000);
        System.out.println("LOL2");

        ThreadUtility.getAllThreadsId();
        Thread.sleep(3000);

        while(true) {
            for (ThreadInfo thread : ThreadUtility.getAllThreadsInfo()) {

                System.out.println("Thread Name: " + thread.getThreadName());
                System.out.println("Thread ID: " + thread.getThreadId());
                System.out.println(Arrays.toString(thread.getStackTrace()));
                System.out.println("Memory usage: " + ThreadUtility.getAllocatedMemoryInBytes(thread.getThreadId()) + " bytes");
                System.out.println("CPU usage: " + ThreadUtility.getCPUUsage(thread.getThreadId()) + "%");

                System.out.println();
            }
            Thread.sleep(2000);
        }

    }

}