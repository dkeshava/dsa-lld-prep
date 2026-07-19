package lld.concurrencyInterviewProblems.rateLimiter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

interface IRateLimiter{
    boolean giveAccess(String rateLimitKey);
    void shutDown();
}

class TokenBucketStrategy implements IRateLimiter{
    private final int bucketCapacity;
    private final int refreshRate;
    private final long refillIntervalMillis;
    private Bucket globalBucket;
    private ConcurrentHashMap<String, Bucket> userBuckets;
    private ExecutorService scheduler=Executors.newSingleThreadScheduledExecutor();
    public TokenBucketStrategy(int bucketCapacity, int refreshRate, long refillIntervalMillis){
        this.userBuckets=new ConcurrentHashMap<>();
        this.bucketCapacity=bucketCapacity;
        this.refillIntervalMillis=refillIntervalMillis;
        this.refreshRate=refreshRate;
        this.globalBucket=new Bucket(bucketCapacity, refreshRate, refillIntervalMillis);
        startRefillTask();
    }
    @Override
    public boolean giveAccess(String rateLimitKey){
        if(rateLimitKey!=null && !rateLimitKey.isEmpty()){
            Bucket userBucket=userBuckets.computeIfAbsent(rateLimitKey, k->new Bucket(bucketCapacity, refreshRate, refillIntervalMillis));
            return userBucket.tryConsume();
        }
        else{
            return globalBucket.tryConsume();
        }
    }
    public void startRefillTask(){
        ((ScheduledExecutorService) scheduler).scheduleAtFixedRate(()->{
            globalBucket.refill();
            for(Bucket userBucket: userBuckets.values()){
                userBucket.refill();
            }
        }, 0,refillIntervalMillis,TimeUnit.MILLISECONDS);
    }
    public void shutDown(){
        scheduler.shutdown();
    }
}
class Bucket{
    private final int bucketCapacity;
    private final int refreshRate;
    private final long refillIntervalMillis;
    private volatile int tokensLeft;
    public Bucket(int bucketCapacity, int refreshRate, long refillIntervalMillis){
        this.bucketCapacity=bucketCapacity;
        this.refreshRate=refreshRate;
        this.refillIntervalMillis=refillIntervalMillis;
        this.tokensLeft=bucketCapacity;
    }
    public int getCapacity(){return bucketCapacity;}
    public int getRefreshRate(){return refreshRate;}
    public long getRefillInterval(){return refillIntervalMillis;}
    public synchronized boolean tryConsume(){
        if(tokensLeft>0){
            tokensLeft--;
            return true;
        }
        else return false;
    }
    public synchronized void refill(){
        tokensLeft=Math.min(bucketCapacity,tokensLeft+refreshRate);
    }
}

class RateLimiterController{
    private IRateLimiter rateLimiter;
    private ExecutorService executor;
    public RateLimiterController(IRateLimiter rateLimiter, ExecutorService executor){
        this.rateLimiter=rateLimiter;
        this.executor=executor;
    }
    public CompletableFuture<Boolean> processRequest(String rateLimitKey){
        return CompletableFuture.supplyAsync(()->{
            boolean allowed=rateLimiter.giveAccess(rateLimitKey);
            if (allowed) {
                System.out.printf("Request with key [%s]: ✅ Allowed%n", rateLimitKey);
            } else {
                System.out.printf("Request with key [%s]: ❌ Blocked%n", rateLimitKey);
            }
            return allowed;
        }, executor);
    }
    public void shutDown(){
        rateLimiter.shutDown();
        executor.shutdown();
    }
}

public class RateLimiterV1 {
    public static void main(String[] args) {
        ExecutorService executor=Executors.newFixedThreadPool(10);
        IRateLimiter rateLimiter=new TokenBucketStrategy(5, 1, 1000);
        RateLimiterController rateLimiterController=new RateLimiterController(rateLimiter, executor);

        // System.out.println("Global Rate Limiting demo: Sending burst of requests");
        // for(int i=0;i<30;i++){
        //     rateLimiterController.processRequest(null);
        //     try {
        //         Thread.sleep(100);
        //     } catch (Exception e) {
        //     }
        // }

        for(int i=0;i<15;i++){
            rateLimiterController.processRequest("user"+i);
        }
        rateLimiterController.shutDown();
    }
}
