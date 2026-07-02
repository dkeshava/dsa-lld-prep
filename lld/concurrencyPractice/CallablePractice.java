
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CallablePractice {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ExecutorService executor=Executors.newFixedThreadPool(3);
        Callable<Integer> task=()->{
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return 41;
        };
        Future<Integer> future= executor.submit(task);

        //isDone example 
        if(future.isDone()){
            System.out.println("completed task");
        }
        else System.out.println("not yet");
        System.out.println();

        //get example
        Integer result=future.get();
        System.out.println("Result : "+result);
        System.out.println();

        //cancel example
        System.out.println("Cancel example");
        Future<?> future1=executor.submit(()->{
            while(true){
                if(Thread.interrupted()){
                    System.out.println("Task was interrupted, stopping...");
                    break;
                }
                System.out.println("working");
            }
        });
        Thread.sleep(1);
        future1.cancel(true);
        System.out.println();

        //isCancelled Example
        Future<?> future2=executor.submit(()->{
            try {
                System.out.println("Task started");
                Thread.sleep(500);
                System.out.println("Task completed");
            } catch (InterruptedException e) {
                System.out.println("Task interrupted");
                Thread.currentThread().interrupt();
            }
        });
        Thread.sleep(1000);
        //boolean cancelled=future2.cancel(true);
        //System.out.println("cancel() returned : " + cancelled);
        System.out.println("isCancelled() : " + future2.isCancelled());
        System.out.println("isDone() : " + future2.isDone());
        System.out.println();


        Future<Integer> future3 = executor.submit(() -> 42);

        Thread.sleep(1000); // Task already completed

        boolean cancelled = future3.cancel(true);
        System.out.println("cancelling after task completion");
        System.out.println(cancelled);          // false
        System.out.println(future3.isCancelled()); // false
        System.out.println(future3.isDone()); 
        System.out.println();
        
        System.out.println("Example 4");
        List<Future<String>> futures = new ArrayList<>();
        for(int i=0;i<5;i++){
            final int id = i;
            Future<String> future4=executor.submit(()->{
                Thread.sleep(3000);
                final String str="Image "+id+" processed successfully by "+Thread.currentThread().getName();
                return str;
            });
            futures.add(future4);
        }

        for(Future<String> future5: futures){
            try {
                String res=future5.get();
                System.out.println(res);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        executor.shutdown();
    }
}
