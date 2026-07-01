class MyTask implements Runnable{
    public void run(){
        for(int i=0;i<5;i++){
            System.out.println("Runnable "+Thread.currentThread().getName()+" is running "+i);
        }
    }
}

class Counter{
    int count=0;
    public synchronized void increment(){
        count++;
    }
}

class SharedResource{
    private boolean available=false;
    public synchronized void waitForResource() throws InterruptedException{
        while(!available){
            System.out.println("Waiting...");
            wait();
            //Thread.sleep(1000);
        }
        Thread.sleep(200);
        System.out.println("Resource available now!");
    }   
    public synchronized void makeAvailable(){
        available=true;
        System.out.println("Making resource available");
        notifyAll();
    }
}

public class ThreadNotes3 {
    public static void main(String[] args) throws InterruptedException {
        //MyTask task=new MyTask();
        // Counter counter=new Counter();
        // Runnable task=()->{
        //     for(int i=0;i<10000;i++){
        //         counter.increment();
        //     }
        // };

        // Thread thread1=new Thread(task);
        // Thread thread2=new Thread(task);

        // thread1.start();
        // thread2.start();

        // thread1.join();
        // thread2.join();
        // System.out.println(counter.count);
        SharedResource resource=new SharedResource();
        Thread consumer=new Thread(()->{
            try {
                resource.waitForResource();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Thread producer=new Thread(()->{
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            resource.makeAvailable();
        });
        consumer.start();
        //Thread.sleep(1000);
        producer.start();
    }
}
