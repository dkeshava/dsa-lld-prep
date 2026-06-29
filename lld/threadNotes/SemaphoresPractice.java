
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.IntConsumer;

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
class EvenOddPrinter3{
    private int current=1;
    private Semaphore evenSemaphore=new Semaphore(0);
    private Semaphore oddSemaphore=new Semaphore(1);
    public void printOdd() throws InterruptedException{
        try {
            while(true){
                oddSemaphore.acquire();
                if(current>10){
                    evenSemaphore.release();
                    break;
                }
                System.out.println("Odd: "+current);
                current++;
                evenSemaphore.release();
            }
        } finally {
            
        }
    }
    public void printEven() throws InterruptedException{
        try {
            while(true){
                evenSemaphore.acquire();
                if(current>10){
                    oddSemaphore.release();
                    break;
                }
                System.out.println("Even: "+current);
                current++;
                oddSemaphore.release();
            }   
        } finally {
            
        }
    }
}

class FizzBuzz {
    private int n;

    public FizzBuzz(int n) {
        this.n = n;
    }
    private Semaphore numSem=new Semaphore(1);
    private Semaphore fizzSem=new Semaphore(0);
    private Semaphore buzzSem=new Semaphore(0);
    private Semaphore fizzbuzzSem=new Semaphore(0);

    private int curr=1;
    // printFizz.run() outputs "fizz".
    public void fizz(Runnable printFizz) throws InterruptedException {
        try{
            while(true){
                fizzSem.acquire();
                if(curr>n){
                    buzzSem.release();
                    numSem.release();
                    fizzbuzzSem.release();
                    break;
                }
                printFizz.run();
                curr++;
                if(curr%15==0) fizzbuzzSem.release();
                else if(curr%5==0) buzzSem.release();
                else numSem.release();   
            }
        }
        finally{

        }
    }

    // printBuzz.run() outputs "buzz".
    public void buzz(Runnable printBuzz) throws InterruptedException {
        try{
            while(true){
                buzzSem.acquire();
                if(curr>n){
                    fizzSem.release();
                    numSem.release();
                    fizzbuzzSem.release();
                    break;
                }
                printBuzz.run();
                curr++;
                if(curr%15==0) fizzbuzzSem.release();
                else if(curr%3==0) fizzSem.release();
                else numSem.release();   
            }
        }
        finally{
            
        }
    }

    // printFizzBuzz.run() outputs "fizzbuzz".
    public void fizzbuzz(Runnable printFizzBuzz) throws InterruptedException {
        try{
            while(true){
                fizzbuzzSem.acquire();
                if(curr>n){
                    buzzSem.release();
                    numSem.release();
                    fizzSem.release();
                    break;
                }
                printFizzBuzz.run();
                curr++;
                //if(curr%15==0) fizzbuzzSem.release();
                if(curr%3==0) fizzSem.release();
                else if(curr%5==0) buzzSem.release();
                else numSem.release();   
            }
        }
        finally{
            
        }
    }

    // printNumber.accept(x) outputs "x", where x is an integer.
    public void number(IntConsumer printNumber) throws InterruptedException {
        try{
            while(true){
                numSem.acquire();
                if(curr>n){
                    buzzSem.release();
                    fizzSem.release();
                    fizzbuzzSem.release();
                    break;
                }
                printNumber.accept(curr);
                curr++;
                if(curr%15==0) fizzbuzzSem.release();
                else if(curr%5==0) buzzSem.release();
                else if(curr%3==0) fizzSem.release();
                else numSem.release();   
            }
        }
        finally{
            
        }
    }
}

class FooBar {
    private int n;

    public FooBar(int n) {
        this.n = n;
    }
    private Semaphore foo=new Semaphore(1);
    private Semaphore bar=new Semaphore(0);
    public void foo(Runnable printFoo) throws InterruptedException {
        
        for (int i = 0; i < n; i++) {
            
        	// printFoo.run() outputs "foo". Do not change or remove this line.
            try{
                foo.acquire();
                printFoo.run();
            }
        	finally{
                bar.release();
            }
        }
    }

    public void bar(Runnable printBar) throws InterruptedException {
        
        for (int i = 0; i < n; i++) {
            
            // printBar.run() outputs "bar". Do not change or remove this line.
        	try{
                bar.acquire();
                printBar.run();
            }
        	finally{
                foo.release();
            }
        }
    }
}

class Foo {

    public Foo() {
        
    }
    private Semaphore firstSemaphore=new Semaphore(1);
    private Semaphore secondSemaphore=new Semaphore(0);
    private Semaphore thirdSemaphore=new Semaphore(0);

    public void first(Runnable printFirst) throws InterruptedException {
        
        // printFirst.run() outputs "first". Do not change or remove this line.
        try{
            firstSemaphore.acquire();
            printFirst.run();
            secondSemaphore.release();
        }
        finally{
            firstSemaphore.release();
        }
    }

    public void second(Runnable printSecond) throws InterruptedException {
        
        // printSecond.run() outputs "second". Do not change or remove this line.
        try{
            secondSemaphore.acquire();
            printSecond.run();
            thirdSemaphore.release();
        }
        finally{
            secondSemaphore.release();
        }
    }

    public void third(Runnable printThird) throws InterruptedException {
        
        // printThird.run() outputs "third". Do not change or remove this line.
        try{
            thirdSemaphore.acquire();
            printThird.run();
        }
        finally{
            thirdSemaphore.release();
        }
    }
}

class ZeroEvenOdd {
    private int n;
    
    public ZeroEvenOdd(int n) {
        this.n = n;
    }
    private Semaphore zSemaphore=new Semaphore(1);
    private Semaphore oSemaphore=new Semaphore(0);
    private Semaphore eSemaphore=new Semaphore(0);
    private int curr=1;
    // printNumber.accept(x) outputs "x", where x is an integer.
    public void zero(IntConsumer printNumber) throws InterruptedException {
        try{
            while(true){
                zSemaphore.acquire();
                if(curr>n){
                    oSemaphore.release();
                    eSemaphore.release();
                    break;
                }
                printNumber.accept(0);
                if(curr%2==0) eSemaphore.release();
                else oSemaphore.release();
            }
        }
        finally{
            //zSemaphore.release();
        }
    }

    public void even(IntConsumer printNumber) throws InterruptedException {
        try{
            while(true){
                eSemaphore.acquire();
                if(curr>n){
                    oSemaphore.release();
                    zSemaphore.release();
                    break;
                }
                printNumber.accept(curr);
                curr++;
                zSemaphore.release();
            }
        }
        finally{
            //eSemaphore.release();
        }
    }

    public void odd(IntConsumer printNumber) throws InterruptedException {
        try{
            while(true){
                oSemaphore.acquire();
                if(curr>n){
                    eSemaphore.release();
                    zSemaphore.release();
                    break;
                }
                printNumber.accept(curr);
                curr++;
                zSemaphore.release();
            }
        }
        finally{
            //oSemaphore.release();
        }
    }
}

public class SemaphoresPractice {
    public static void main(String[] args) throws InterruptedException {
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
        evenThread2.join();
        oddThread2.join();

        System.out.println("Using semaphore");
        EvenOddPrinter3 printer3=new EvenOddPrinter3();
        Thread oddThread3=new Thread(()->{
            try {
                printer3.printOdd();
            } catch (InterruptedException e) {
            }
        });
        Thread evenThread3=new Thread(()->{
            try {
                printer3.printEven();
            } catch (InterruptedException e) {
            }
        });
        oddThread3.start();
        evenThread3.start();
    }
}

