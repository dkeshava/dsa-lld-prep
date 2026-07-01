import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.IntConsumer;

class FizzBuzz {
    private int n;

    public FizzBuzz(int n) {
        this.n = n;
    }
    private ReentrantLock lock =new ReentrantLock();
    private Condition condition=lock.newCondition();
    private int current=1;
    //private boolean isNumber=true;

    // printFizz.run() outputs "fizz".
    public void fizz(Runnable printFizz) throws InterruptedException {
        lock.lock();
        try{
            while(current<=n){
                while(!(current%3==0 && current%5!=0) && current<=n){
                    condition.await();
                }
                if (current > n) {
                    condition.signalAll();
                    return;
                }
                printFizz.run();
                current++;
                condition.signalAll();
            }
        }
        finally{
            lock.unlock();
        }
    }

    // printBuzz.run() outputs "buzz".
    public void buzz(Runnable printBuzz) throws InterruptedException {
        lock.lock();
        try{
            while(current<=n){
                while(!(current%3!=0 && current%5==0) && current<=n){
                    condition.await();
                }
                if (current > n) {
                    condition.signalAll();
                    return;
                }
                printBuzz.run();
                current++;
                condition.signalAll();
            }
        }
        finally{
            lock.unlock();
        }
    }

    // printFizzBuzz.run() outputs "fizzbuzz".
    public void fizzbuzz(Runnable printFizzBuzz) throws InterruptedException {
        lock.lock();
        try{
            while(current<=n){
                while(!(current%15==0) && current<=n){
                    condition.await();
                }
                if (current > n) {
                    condition.signalAll();
                    return;
                }
                printFizzBuzz.run();
                current++;
                condition.signalAll();
            }
        }
        finally{
            lock.unlock();
        }
    }

    // printNumber.accept(x) outputs "x", where x is an integer.
    public void number(IntConsumer printNumber) throws InterruptedException {
        lock.lock();
        try{
            while(current<=n){
                while((current % 3 == 0 || current % 5 == 0) && current<=n){
                    condition.await();
                }
                if (current > n) {
                    condition.signalAll();
                    return;
                }
                printNumber.accept(current);
                current++;
                condition.signalAll();
            }
        }
        finally{
            lock.unlock();
        }
    }
}