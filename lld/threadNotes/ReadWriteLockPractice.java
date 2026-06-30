import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class Cache{
    private Map<String,String> map=new HashMap<>();
    private ReadWriteLock lock=new ReentrantReadWriteLock();
    public String get(String key){
        System.out.println(Thread.currentThread().getName()+" waiting for read lock");
        lock.readLock().lock();
        try{
            System.out.println(Thread.currentThread().getName()+" acquired read lock");
            String value = map.get(key);
            System.out.println(Thread.currentThread().getName()+ " read value = " + value);
            return value;
        }
        finally{
            lock.readLock().unlock();
            System.out.println(Thread.currentThread().getName()+" released read lock");
        }
    }

    public void put(String key, String value){
        System.out.println(Thread.currentThread().getName()+" waiting for write lock");
        lock.writeLock().lock();
        try {
            System.out.println(Thread.currentThread().getName()+" acquired write lock");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            map.put(key, value);
        } finally {
            System.out.println(Thread.currentThread().getName()+" releasing write lock");
            lock.writeLock().unlock();
        }
    }
}

public class ReadWriteLockPractice {
    public static void main(String[] args) throws InterruptedException {
        Cache cache=new Cache();
        Thread reader1 = new Thread(() -> {
            cache.get("1");
        }, "Reader-1");
        
        Thread reader2 = new Thread(() -> {
            cache.get("2");
        }, "Reader-2");
        
        Thread writer1 = new Thread(() -> {
            cache.put("1", "1");
        }, "Writer-1");
        
        Thread writer2 = new Thread(() -> {
            cache.put("2", "2");
        }, "Writer-2");

        Thread writer3 = new Thread(() -> {
            cache.put("3", "3");
        }, "Writer-3");
        
        Thread writer4 = new Thread(() -> {
            cache.put("4", "4");
        }, "Writer-4");
        Thread reader3=new Thread(()->{
            cache.get("3");
        }, "Reader-3");
        Thread reader4=new Thread(()->{
            cache.get("4");
        }, "Reader-4");
        reader1.start();  
        reader2.start();
        Thread.sleep(500); 
        writer3.start();
        Thread.sleep(500); 
        reader3.start();
        Thread.sleep(500); 
        writer4.start();
        reader4.start();
    }
}
