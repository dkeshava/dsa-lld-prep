class Buffer{
    private Integer item=null;
    public synchronized void produce(int value) throws InterruptedException{
        while(item!=null){
            System.out.println("Producer waiting to produce "+value);
            wait();
        }
        item=value;
        notifyAll();
        System.out.println("Producer produced: "+value);
    }
    public synchronized int consume() throws InterruptedException{
        while(item==null){
            System.out.println("Consumer waiting to consume");
            wait();
        }
        System.out.println("Consumer consumed: "+item);
        int value=item;
        item=null;
        notifyAll();
        return value;
    }
}

public class ThreadNotes4 {
    public static void main(String[] args) {
        Buffer buffer=new Buffer();
        Thread producer=new Thread(()->{
            for (int i = 0; i < 5; i++) {
                try {
                    Thread.sleep(1000);
                    buffer.produce(i);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        Thread consumer=new Thread(()->{
            for (int i = 0; i < 5; i++) {
                try {
                    //Thread.sleep(1000);
                    buffer.consume();
                } catch (InterruptedException e) {
                }
            }
        });
        consumer.start();
        producer.start();
    }
}
