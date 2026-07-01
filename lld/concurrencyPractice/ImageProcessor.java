
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ImageProcessor {
    private ExecutorService executor=Executors.newFixedThreadPool(3);
    public void processImage(String imageName){
        executor.submit(()-> {
            System.out.println(Thread.currentThread().getName()+" processing Image: "+imageName);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // restore interrupt status
                System.out.println(Thread.currentThread().getName()
                        + " interrupted while processing " + imageName);
                return;
            }
            
            System.out.println(Thread.currentThread().getName()
                    + " processed Image: " + imageName);
        });
    }
    public void shutdown(){
        executor.shutdown(); // stop accepting new tasks
        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    public static void main(String[] args) {
        ImageProcessor processor=new ImageProcessor();
        for (int i = 1; i <= 10; i++) {
            processor.processImage("image" + i + ".jpg");
        }
        processor.shutdown();
        //System.out.println("All images processed.");
    }
}
