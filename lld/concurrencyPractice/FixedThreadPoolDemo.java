import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FixedThreadPoolDemo {

    public static void main(String[] args) {

        ExecutorService executor = Executors.newFixedThreadPool(3);

        for (int i = 0; i < 10; i++) {
            int taskId = i;

            executor.submit(() -> {

                System.out.println(
                        Thread.currentThread().getName()
                                + " started task " + taskId);

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                System.out.println(
                        Thread.currentThread().getName()
                                + " finished task " + taskId);
            });
        }

        executor.shutdown();
    }
}