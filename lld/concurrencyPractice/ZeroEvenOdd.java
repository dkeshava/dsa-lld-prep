import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.IntConsumer;

class ZeroEvenOdd {
    private int n;
    
    public ZeroEvenOdd(int n) {
        this.n = n;
    }
    private int current=1;
    private ReentrantLock lock=new ReentrantLock();
    private Condition condition=lock.newCondition();
    private boolean printZero=true;
    // printNumber.accept(x) outputs "x", where x is an integer.
    public void zero(IntConsumer printNumber) throws InterruptedException {
        lock.lock();
        try{
            while(current<=n){
                while(!printZero){
                    condition.await();
                }
                if (current > n) {
                    condition.signalAll();
                    return;
                }
                printNumber.accept(0);
                //current++;
                printZero=false;
                condition.signalAll();
            }
            //condition.signalAll();
        }
        finally{
            lock.unlock();
        }
    }

    public void even(IntConsumer printNumber) throws InterruptedException {
        lock.lock();
        try{
            while(current<=n){
                while((printZero || current%2!=0) && current<=n){
                    condition.await();
                }
                if (current > n) {
                    condition.signalAll();
                    return;
                }
                printNumber.accept(current);
                current++;
                printZero=true;
                condition.signalAll();
            }
        }
        finally{
            lock.unlock();
        }
    }

    public void odd(IntConsumer printNumber) throws InterruptedException {
        lock.lock();
        try{
            while(current<=n){
                while((printZero || current%2==0) && current<=n){
                    condition.await();
                }
                if (current > n) {
                    condition.signalAll();
                    return;
                }
                printNumber.accept(current);
                current++;
                printZero=true;
                condition.signalAll();
            }
        }
        finally{
            lock.unlock();
        }
    }
}