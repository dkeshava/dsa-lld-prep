
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
class Foo {

    public Foo() {
        
    }
    public ReentrantLock lock=new ReentrantLock();
    private Condition condition=lock.newCondition();
    private boolean isFirst=true,isSecond=false,isThird=false;
    public void first(Runnable printFirst) throws InterruptedException {
        
        // printFirst.run() outputs "first". Do not change or remove this line.
        lock.lock();
        try{
            while(!isFirst) condition.await();
            printFirst.run();
            isFirst=false;
            isSecond=true;
            condition.signalAll();
        }
        finally{
            lock.unlock();
        }
    }

    public void second(Runnable printSecond) throws InterruptedException {
        
        // printSecond.run() outputs "second". Do not change or remove this line.
        lock.lock();
        try{
            while(!isSecond) condition.await();
            printSecond.run();
            isSecond=false;
            isThird=true;
            condition.signalAll();
        }
        finally{
            lock.unlock();
        }
    }

    public void third(Runnable printThird) throws InterruptedException {
        
        // printThird.run() outputs "third". Do not change or remove this line.
        lock.lock();
        try{
            while(!isThird) condition.await();
            printThird.run();
            isThird=false;
            condition.signalAll();
        }
        finally{
            lock.unlock();
        }
    }
}