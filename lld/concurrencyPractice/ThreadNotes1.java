class MyThread extends Thread{
    public void run(){
        System.out.println("Inside run()");
    }
}

public class ThreadNotes1 {
    public static void main(String[] args) {
        Thread t=new MyThread();
        System.out.println("Before start");
        t.start();
        System.out.println("After start");
        // t.run();
        // t.getName();
        //t.getId();
    }
}
