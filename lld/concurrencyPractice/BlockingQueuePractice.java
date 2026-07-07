
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BlockingQueuePractice {
    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Integer> queue=new ArrayBlockingQueue<>(3);
        Thread producer1=new Thread(()->{
            try {
                for (int i = 1; i <=10; i++) {
                    queue.put(i);
                    System.out.println(Thread.currentThread().getName() + " produced " + i);
                }
                queue.put(-1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        },"Producer-1");
        Thread producer2=new Thread(()->{
            try {
                for (int i = 11; i <=15; i++) {
                    queue.put(i);
                    System.out.println(Thread.currentThread().getName() + " produced " + i);
                }
                queue.put(-1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        },"Producer-2");
        Thread consumer1=new Thread(()->{
            try {
                while(true) {
                    Thread.sleep(1000);
                    int x=queue.take();
                    if(x==-1) break;
                    System.out.println(Thread.currentThread().getName() + " consumed " + x);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        },"Consumer-1");
        Thread consumer2=new Thread(()->{
            try {
                while(true) {
                    Thread.sleep(1000);
                    int x=queue.take();
                    if(x==-1) break;
                    System.out.println(Thread.currentThread().getName() + " consumed " + x);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        },"Consumer-2");
        producer1.start();
        producer2.start();
        consumer1.start();
        consumer2.start();
        producer1.join();
        producer2.join();
        consumer1.join();
        consumer2.join();
    }   
}
