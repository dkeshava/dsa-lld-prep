import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SingleThreadExecutorDemo {

    public static void main(String[] args) {

        ExecutorService executor = Executors.newSingleThreadExecutor();

        for (int i = 0; i < 5; i++) {
            int taskId = i;

            executor.submit(() -> {

                System.out.println(
                        Thread.currentThread().getName()
                                + " started task " + taskId);

                System.out.println(
                        Thread.currentThread().getName()
                                + " finished task " + taskId);
            });
        }

        executor.shutdown();
    }
}