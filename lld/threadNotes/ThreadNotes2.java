class MyThread extends Thread{
    @Override
    public void run(){
        System.out.println("run thread = "+Thread.currentThread().getName());
    }
}
public class ThreadNotes2 {
    public static void main(String[] args) {
        Thread t=new MyThread();
        System.out.println("main thread = "+Thread.currentThread().getName());
        t.start();
        // t.start();
        System.out.println("main thread = "+Thread.currentThread().getName());
    }
}
