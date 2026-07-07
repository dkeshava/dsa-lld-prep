import java.util.concurrent.locks.ReentrantLock;

class DiningPhilosophers {
    private ReentrantLock[] forks;
    public DiningPhilosophers() {
        forks=new ReentrantLock[5];
        for (int i = 0; i < 5; i++) {
            forks[i] = new ReentrantLock();
        }
    }

    // call the run() method of any runnable to execute its code
    public void wantsToEat(int philosopher,
                           Runnable pickLeftFork,
                           Runnable pickRightFork,
                           Runnable eat,
                           Runnable putLeftFork,
                           Runnable putRightFork) throws InterruptedException {

                            int left=philosopher;
                            int right=(philosopher+1)%5;
                            int leftFork=Math.min(left,right);
                            int rightFork=Math.max(left,right);

                            forks[leftFork].lock();
                            forks[rightFork].lock();
                            pickLeftFork.run();
                            pickRightFork.run();

                            eat.run();
                            
                            putLeftFork.run();
                            putRightFork.run();
                            forks[leftFork].unlock();
                            forks[rightFork].unlock();
    }
}