
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


class FooBar {
    private int n;

    public FooBar(int n) {
        this.n = n;
    }
    private ReentrantLock lock=new ReentrantLock();
    private Condition condition= lock.newCondition();
    private boolean foo=true;
    public void foo(Runnable printFoo) throws InterruptedException {
        lock.lock();
        try{
            for (int i = 0; i < n; i++) {
                while(!foo){
                    condition.await();
                }
                // printFoo.run() outputs "foo". Do not change or remove this line.
                printFoo.run();
                foo=false;
                condition.signalAll();
            }
        }
        finally{
            lock.unlock();
        }
    }

    public void bar(Runnable printBar) throws InterruptedException {
        lock.lock();
        try{
            for (int i = 0; i < n; i++) {
                // printBar.run() outputs "bar". Do not change or remove this line.
                while(foo){
                    condition.await();
                }
                printBar.run();
                foo=true;
                condition.signalAll();
            }
        }
        finally{
            lock.unlock();
        }
    }
}