package lld.concurrencyInterviewProblems.rateLimiter.version2;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

enum RateLimiterType{
    TOKEN_BUCKET,
    LEAKY_BUCKET,
    FIXED_WINDOW,
    SLIDING_WINDOW
}

interface IRateLimiter{
    boolean giveAccess(String rateLimitKey);
}

class TokenBucketStrategy implements IRateLimiter{
    private int bucketCapacity;
    private int refreshRate;
    private long refillIntervalMillis;
    private Bucket globalBucket;
    private ConcurrentHashMap<String, Bucket> userBuckets;
    public TokenBucketStrategy(int bucketCapacity, int refreshRate, long refillIntervalMillis){
        this.userBuckets=new ConcurrentHashMap<>();
        this.bucketCapacity=bucketCapacity;
        this.refillIntervalMillis=refillIntervalMillis;
        this.refreshRate=refreshRate;
        this.globalBucket=new Bucket(bucketCapacity, refreshRate, refillIntervalMillis);
        //startRefillTask();
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
    public void updateConfig(int bucketCapacity, int refreshRate){
        this.bucketCapacity=bucketCapacity;
        this.refreshRate=refreshRate;
        for(Bucket userBucket :userBuckets.values()){
            userBucket.updateConfig(bucketCapacity, refreshRate);
        }
    }
}
class Bucket{
    private int bucketCapacity;
    private int refreshRate;
    private long refillIntervalMillis;
    private volatile int tokensLeft;
    private long lastRefilled;
    public Bucket(int bucketCapacity, int refreshRate, long refillIntervalMillis){
        this.bucketCapacity=bucketCapacity;
        this.refreshRate=refreshRate;
        this.refillIntervalMillis=refillIntervalMillis;
        this.tokensLeft=bucketCapacity;
        this.lastRefilled=System.currentTimeMillis();
    }
    public int getCapacity(){return bucketCapacity;}
    public int getRefreshRate(){return refreshRate;}
    public long getRefillInterval(){return refillIntervalMillis;}
    public synchronized boolean tryConsume(){
        long now=System.currentTimeMillis();
        long elapsed=now-lastRefilled;
        if(elapsed>=refillIntervalMillis){
            long intervals=elapsed/refillIntervalMillis;
            tokensLeft=Math.min(bucketCapacity,tokensLeft+(int)(intervals*refreshRate));
            lastRefilled+=intervals*refillIntervalMillis;
        }
        if(tokensLeft>0){
            tokensLeft--;
            return true;
        }
        else return false;
    }
    public synchronized void updateConfig(int bucketCapacity, int refreshRate){
        this.bucketCapacity=bucketCapacity;
        this.refreshRate=refreshRate;
        tokensLeft = Math.min(tokensLeft, bucketCapacity);
    }
}

class FixedWindowStrategy implements IRateLimiter{
    private long windowSize;
    private int requestCap;
    private ConcurrentHashMap<String, Window> userWindows;
    public FixedWindowStrategy(long windowSize, int requestCap){
        this.windowSize=windowSize;
        this.requestCap=requestCap;
        userWindows = new ConcurrentHashMap<>();
    }
    @Override
    public boolean giveAccess(String rateLimitKey){
        if(rateLimitKey!=null && !rateLimitKey.isEmpty()){
            Window userWindow=userWindows.computeIfAbsent(rateLimitKey, k->new Window(windowSize,requestCap));
            return userWindow.tryConsume();
        }
        else{
            System.out.println("Please try with a valid rate limit key!");
            return false;
        } 
    }
}
class Window{
    private long windowStart;
    private final long windowSize;
    private int requestsLeft;
    private final int requestCap;
    public Window(long windowSize,int requestCap){
        this.requestCap=requestCap;
        this.requestsLeft=requestCap;
        this.windowSize=windowSize;
        this.windowStart=System.currentTimeMillis();
    }
    public synchronized boolean tryConsume(){
        long now=System.currentTimeMillis();
        long elapsed=now-windowStart;
        if(elapsed>=windowSize){
            long intervals=elapsed/windowSize;
            windowStart+=intervals*windowSize;
            requestsLeft=requestCap;
        }
        if(requestsLeft>0){
            requestsLeft--;
            return true;
        }
        else return false;
    }
}
class SlidingWindowStrategy implements IRateLimiter{
    private long windowSize;
    private int requestCap;
    private ConcurrentHashMap<String, SlidingWindow> userWindows;
    public SlidingWindowStrategy(long windowSize, int requestCap){
        this.windowSize=windowSize;
        this.requestCap=requestCap;
        userWindows = new ConcurrentHashMap<>();
    }
    @Override
    public boolean giveAccess(String rateLimitKey){
        if(rateLimitKey!=null && !rateLimitKey.isEmpty()){
            SlidingWindow userWindow=userWindows.computeIfAbsent(rateLimitKey, k->new SlidingWindow(windowSize,requestCap));
            return userWindow.tryConsume();
        }
        else{
            System.out.println("Please try with a valid rate limit key!");
            return false;
        } 
    }
}
class SlidingWindow{
    private final long windowSize;
    private final int requestCap;
    private final Deque<Long> timeStamps;
    public SlidingWindow(long windowSize, int requestCap){
        this.windowSize=windowSize;
        this.requestCap=requestCap;
        this.timeStamps=new ArrayDeque<>();
    }
    public synchronized boolean tryConsume(){
        long now=System.currentTimeMillis();
        while(!timeStamps.isEmpty() && now-timeStamps.peekFirst()>=windowSize) timeStamps.pollFirst();
        if(timeStamps.size()<requestCap){
            timeStamps.offerFirst(now);
            return true;
        }
        else return false;
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
        executor.shutdown();
    }
}
class RateLimiterFactory{
    public IRateLimiter createRateLimiter(RateLimiterType type, Config config){
        switch (type) {
            case RateLimiterType.TOKEN_BUCKET:
                return new TokenBucketStrategy(config.getBucketCapacity(), config.getRefreshRate(), config.getRefillInterval());
            case RateLimiterType.FIXED_WINDOW:
                return new FixedWindowStrategy(config.getWindowSize(), config.getRequestCap());
            case RateLimiterType.LEAKY_BUCKET:
                throw new UnsupportedOperationException();
            case RateLimiterType.SLIDING_WINDOW:
                return new SlidingWindowStrategy(config.getSlidingWindowSize(), config.getSlidingRequestCap());
            default:
                throw new AssertionError();
        }
    }
}
class Config{
    private int bucketCapacity;
    private int refreshRate;
    private long refillIntervalMillis;
    private long windowSize;
    private int requestCap;
    private long slidingWindowSize;
    private int slidingRequestCap;
    public Config(int bucketCapacity, int refreshRate, long refillIntervalMillis){
        this.bucketCapacity=bucketCapacity;
        this.refreshRate=refreshRate;
        this.refillIntervalMillis=refillIntervalMillis;
    }
    public Config(long windowSize,int requestCap){
        this.windowSize=windowSize;
        this.requestCap=requestCap;
    }
    public Config(int slidingRequestCap,long slidingWindowSize){
        this.slidingRequestCap=slidingRequestCap;
        this.slidingWindowSize=slidingWindowSize;
    }
    public int getBucketCapacity(){return bucketCapacity;}
    public int getRefreshRate(){return refreshRate;}
    public long getRefillInterval(){return refillIntervalMillis;}
    public long getWindowSize(){return windowSize;}
    public int getRequestCap(){return requestCap;}
    public long getSlidingWindowSize(){return slidingWindowSize;}
    public int getSlidingRequestCap(){return slidingRequestCap;}
}
public class RateLimiterV2 {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor=Executors.newFixedThreadPool(10);
        RateLimiterFactory factory=new RateLimiterFactory();
        Config config1=new Config(5, 1, 1000);
        IRateLimiter rateLimiter1=factory.createRateLimiter(RateLimiterType.TOKEN_BUCKET, config1);
        RateLimiterController rateLimiterController=new RateLimiterController(rateLimiter1, executor);

        System.out.println("Toke bucket: Global Rate Limiting demo: Sending burst of requests");
        for(int i=0;i<15;i++){
            rateLimiterController.processRequest(null);
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        }

        // for(int i=0;i<15;i++){
        //     rateLimiterController.processRequest("user"+i);
        // }
        ExecutorService executor2=Executors.newFixedThreadPool(10);
        System.out.println();
        Config config2=new Config((long)(1000), 3);
        IRateLimiter rateLimiter2=factory.createRateLimiter(RateLimiterType.FIXED_WINDOW, config2);
        RateLimiterController controller2=new RateLimiterController(rateLimiter2, executor2);
        System.out.println("Fixed Window demo: Sending burst of requests");
        for(int i=0;i<15;i++){
            controller2.processRequest("user");
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        }
        rateLimiterController.shutDown();
        controller2.shutDown();
    }
}
