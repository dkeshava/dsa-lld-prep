import java.util.concurrent.Semaphore;

class DiningPhilosophers {
    private Semaphore dining;
    private Semaphore[] forkSemaphore;
    public DiningPhilosophers() {
        dining=new Semaphore(4);
        forkSemaphore=new Semaphore[5];
        for(int i=0;i<5;i++) forkSemaphore[i]=new Semaphore(1);
    }

    // call the run() method of any runnable to execute its code
    public void wantsToEat(int philosopher,
                           Runnable pickLeftFork,
                           Runnable pickRightFork,
                           Runnable eat,
                           Runnable putLeftFork,
                           Runnable putRightFork) throws InterruptedException {
                            dining.acquire();

                            int left=philosopher;
                            int right=(philosopher+1)%5;

                            Semaphore leftFork=forkSemaphore[left];
                            Semaphore rightFork=forkSemaphore[right];

                            leftFork.acquire();
                            rightFork.acquire();
                            pickLeftFork.run();
                            pickRightFork.run();

                            eat.run();
                            
                            putLeftFork.run();
                            putRightFork.run();
                            leftFork.release();
                            rightFork.release();
                            dining.release();
    }
}