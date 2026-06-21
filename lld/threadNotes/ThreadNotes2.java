class MyThread extends Thread{
    @Override
    public void run(){
        for(int i=1;i<=5;i++){
            System.out.println(Thread.currentThread().getName()+" : "+i);
        }
    }
}
public class ThreadNotes2 {
    public static void main(String[] args) {
        Thread t=new MyThread();
        System.out.println("main thread = "+Thread.currentThread().getName());
        t.start();
        // t.start();
        for(int i=1;i<=5;i++){
            System.out.println(
                Thread.currentThread().getName()+" : "+i
            );
        }
    }
}
