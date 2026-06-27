
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class Counter{
    private int count=0;
    private ReentrantLock lock=new ReentrantLock();
    public void increment(){
        lock.lock();
        try {
            count++;    
        } finally {
            lock.unlock();
        }
    }
    public int getCount(){
        return count;
    }
}

class ReentrancyDemo{
    ReentrantLock lock=new ReentrantLock();
    public void methodA(){
        lock.lock();
        try {
            methodB();
        } finally {
            lock.unlock();
        }
    }
    private void methodB(){
        lock.lock();
        try {
            System.out.println("Inside Method B");
        } finally {
            lock.unlock();
        }
    }
}

class SharedResource{
    private boolean available=false;
    private final ReentrantLock lock=new ReentrantLock();
    private final Condition condition=lock.newCondition();

    public void waitForResource() throws InterruptedException{
        lock.lock();
        try {
            while(!available){
                System.out.println("Waiting...");
                condition.await();
            }
            System.out.println("Resource available!");
        } finally {
            lock.unlock();
        }
    }
    public void makeAvailable() throws InterruptedException{
        lock.lock();
        try {
            System.out.println("Making resource available");
            available=true;
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }
}

class Buffer{
    private Integer item=null;
    private final ReentrantLock lock=new ReentrantLock();
    private final Condition condition=lock.newCondition();
    public void produce(int value) throws InterruptedException{
        lock.lock();
        try {
            while(item!=null){
                System.out.println("Producer waiting to produce "+value);
                condition.await();
            }
            item=value;
            condition.signalAll();
            System.out.println("Producer produced: "+value);
        } finally {
            lock.unlock();
        }
    }
    public int consume() throws InterruptedException{
        lock.lock();
        try {
            while(item==null){
                System.out.println("Consumer waiting to consume");
                condition.await();
            }
            System.out.println("Consumer consumed: "+item);
            int value=item;
            item=null;
            condition.signalAll();
            return value;
        } finally {
            lock.unlock();
        }
    }
}

class EvenOddPrinter{
    private int current=1;
    public synchronized void printOdd() throws InterruptedException{
        while(current<=10){
            while(current%2==0){
                wait();
            }
            if(current<=10){
                System.out.println("Odd: "+current);
                current++;
                notifyAll();
            }
        }
        notifyAll();
    }
    public synchronized void printEven() throws InterruptedException{
        while(current<=10){
            while(current%2!=0) wait();
            if(current<=10){
                System.out.println("Even: "+current);
                current++;
                notifyAll();
            }
        }
        notifyAll();
    }
}
class EvenOddPrinter2{
    private int current=1;
    private ReentrantLock lock=new ReentrantLock();
    private Condition condition=lock.newCondition();
    public void printOdd() throws InterruptedException{
        lock.lock();
        try {
            while(current<=10){
                while(current%2==0){
                    condition.await();
                }
                if(current<=10){
                    System.out.println("Odd: "+current);
                    current++;
                    condition.signalAll();
                }
            }   
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }
    public void printEven() throws InterruptedException{
        lock.lock();
        try {
            while(current<=10){
                while(current%2!=0) condition.await();
                if(current<=10){
                    System.out.println("Even: "+current);
                    current++;
                    condition.signalAll();
                }
            }   
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }
}

public class ThreadNotes5 {
    public static void main(String[] args) throws InterruptedException {
        // Counter counter=new Counter();
        // Thread thread1=new Thread(()->{
        //     for(int i=0;i<1000;i++){
        //         counter.increment();
        //     }
        // });
        // Thread thread2=new Thread(()->{
        //     for(int i=0;i<1000;i++){
        //         counter.increment();
        //     }
        // });
        // thread1.start();
        // thread2.start();
        // thread1.join();
        // thread2.join();
        // System.out.println(counter.getCount());
        
        // ReentrancyDemo demo=new ReentrancyDemo();
        // Thread thread3 = new Thread(() -> {
        //     demo.methodA();
        // });
        
        // thread3.start();
        // thread3.join();

        // SharedResource resource=new SharedResource();
        // Thread t1=new Thread(()->{
        //     try {
        //         resource.waitForResource();
        //     } catch (InterruptedException e) {
        //     }
        // });
        // Thread t2=new Thread(()->{
        //     try {
        //         Thread.sleep(2000);
        //         resource.makeAvailable();
        //     } catch (InterruptedException e) {
        //     }
        // });

        // t1.start();
        // t2.start();
        // t1.join();
        // t2.join();

        Buffer buffer=new Buffer();
        Thread producer=new Thread(()->{
            for (int i = 0; i < 5; i++) {
                try {
                    Thread.sleep(1000);
                    buffer.produce(i);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        Thread consumer=new Thread(()->{
            for (int i = 0; i < 5; i++) {
                try {
                    //Thread.sleep(1000);
                    buffer.consume();
                } catch (InterruptedException e) {
                }
            }
        });
        // consumer.start();
        // producer.start();
        EvenOddPrinter printer=new EvenOddPrinter();
        Thread oddThread=new Thread(()->{
            try {
                printer.printOdd();
            } catch (InterruptedException e) {
            }
        });
        Thread evenThread=new Thread(()->{
            try {
                printer.printEven();
            } catch (InterruptedException e) {
            }
        });
        evenThread.start();
        oddThread.start();
        evenThread.join();
        oddThread.join();

        System.out.println("With Reentrant Lock: ");
        EvenOddPrinter2 printer2=new EvenOddPrinter2();
        Thread oddThread2=new Thread(()->{
            try {
                printer2.printOdd();
            } catch (InterruptedException e) {
            }
        });
        Thread evenThread2=new Thread(()->{
            try {
                printer2.printEven();
            } catch (InterruptedException e) {
            }
        });
        evenThread2.start();
        oddThread2.start();
    }
}

