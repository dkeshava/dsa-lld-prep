package lld.concurrencyInterviewProblems.pubSub;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class Message{
    private static final AtomicInteger counter=new AtomicInteger(0);
    private final int id;
    private String message;
    private long timeStamp;
    public Message(String message){
        this.id=counter.incrementAndGet();
        this.message=message;
        this.timeStamp=System.currentTimeMillis();
    }
    public int getId(){return id;}
    public String getMessage(){return message;}
}
class Topic{
    private static final AtomicInteger counter=new AtomicInteger(0);
    private int topicId;
    private String topicName;
    private List<Message> messages;
    private List<TopicSubscriber> subscribers;
    public Topic(String topicName){
        topicId=counter.incrementAndGet();
        this.topicName=topicName;
        messages=new ArrayList<>();
        this.subscribers=new ArrayList<>();
    }
    public synchronized void addMessage(Message message){
        messages.add(message);
    }
    public synchronized Message getMessage(int offset){
        return messages.get(offset);
    }
    public synchronized List<TopicSubscriber> getSubscribers(){return Collections.unmodifiableList(subscribers);}
    public synchronized int getSize(){
        return messages.size();
    }
    public synchronized void addSubscriber(TopicSubscriber subscriber){
        subscribers.add(subscriber);
    }
}

interface ISubscriber{
    int getId();
    void consume(Message message);
}
class SimpleSubscriber implements ISubscriber{
    private int id;
    public SimpleSubscriber(int id){
        this.id=id;
    }
    @Override
    public void consume(Message message){
        System.out.println("Subscriber " + id + " received: " + message.getMessage());
    }
    public int getId(){return id;}
}
class TopicSubscriber{
    private final Topic topic;
    private final ISubscriber subscriber;
    private int offset;
    public TopicSubscriber(Topic topic, ISubscriber subscriber, int offset){
        this.topic=topic;
        this.subscriber=subscriber;
        this.offset=offset;
    }
    public Topic getTopic(){return topic;}
    public ISubscriber getSubscriber(){return subscriber;}
    public int getOffset(){return offset;}
    public void incrementOffset(){
        offset++;
    }
    public void setOffset(int offset){
        this.offset=offset;
    }
}

class TopicSubscriberWorker implements Runnable{
    private final TopicSubscriber topicSubscriber;
    public TopicSubscriberWorker(TopicSubscriber subscriber){
        this.topicSubscriber=subscriber;
    }
    @Override
    public void run(){
        Topic topic=topicSubscriber.getTopic();
        ISubscriber subscriber=topicSubscriber.getSubscriber();
        Message msgToProcess=null;
        while (true) { 
            synchronized (topicSubscriber) {
                while(topicSubscriber.getOffset()>=topic.getSize()){
                    try {
                        topicSubscriber.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                msgToProcess=topic.getMessage(topicSubscriber.getOffset());
                topicSubscriber.incrementOffset();
            }
            try {
                subscriber.consume(msgToProcess);  
            }catch (Exception e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
interface IPublisher{
    void publish(Topic topic, Message message);
}
class SimplePublisher implements IPublisher{
    private int id;
    public SimplePublisher(int id){
        this.id=id;
    }
    @Override
    public void publish(Topic topic, Message message){
        topic.addMessage(message);
        List<TopicSubscriber> subscribers=topic.getSubscribers();
        for(TopicSubscriber subscriber: subscribers){
            synchronized (subscriber) {
                subscriber.notifyAll();
            }
        }
    }
}

class PubSubService{
    private Map<String, Topic> topicMap;
    private ExecutorService subscriberExecutor;
    public PubSubService(){
        this.topicMap=new HashMap<>();
        subscriberExecutor=Executors.newCachedThreadPool();
    }

    public Topic createTopic(String name){
        Topic topic=new Topic(name);
        topicMap.put(name, topic);
        return topic;
    }
    public void subscribe(ISubscriber subscriber, String topicName, int offset){
        Topic topic=topicMap.get(topicName);
        if(topic==null){
            System.out.println("No such topic exists to subscribe!");
            return;
        }
        TopicSubscriber topicSubscriber=new TopicSubscriber(topic, subscriber, offset);
        topic.addSubscriber(topicSubscriber);
        subscriberExecutor.submit(new TopicSubscriberWorker(topicSubscriber));
        System.out.println("Subscriber "+subscriber.getId()+" subscribed to "+topicName);
    }
    public void publish(IPublisher publisher,String topicName, Message message){
        Topic topic=topicMap.get(topicName);
        if(topic==null){
            System.out.println("No such topic exists to publish");
            return;
        }
        publisher.publish(topic, message);
    }
    public void resetOffset(String topicName, ISubscriber subscriber, int offset){
        Topic topic=topicMap.get(topicName);
        if(topic==null){
            System.out.println("Error, no such topic exist to reset offset!");
            return;
        }
        TopicSubscriber topicSubscriber=null;
        for(TopicSubscriber ts: topic.getSubscribers()){
            if(ts.getSubscriber()==subscriber){
                topicSubscriber=ts;
            }
        }
        if(topicSubscriber==null){
            System.out.println("Invalid subscriber!");
            return;
        }
        topicSubscriber.setOffset(offset);
        synchronized (topicSubscriber) {
            topicSubscriber.notifyAll();
        }
    }
    public void shutDown(){
        subscriberExecutor.shutdown();
        try {
            if(!subscriberExecutor.awaitTermination(1, TimeUnit.SECONDS)){
                subscriberExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            subscriberExecutor.shutdownNow();
        }
    }
}
public class SimpleKafka {
    public static void main(String[] args) {
        PubSubService pubSub=new PubSubService();
        IPublisher publisher1=new SimplePublisher(1);
        IPublisher publisher2=new SimplePublisher(2);
        
        ISubscriber subscriber1=new SimpleSubscriber(1);
        ISubscriber subscriber2=new SimpleSubscriber(2);
        ISubscriber subscriber3=new SimpleSubscriber(3);

        Topic topic1=pubSub.createTopic("orders");
        Topic topic2=pubSub.createTopic("trades");
        Topic topic3=pubSub.createTopic("futures");

        pubSub.subscribe(subscriber1, "orders", 0);
        pubSub.subscribe(subscriber2, "orders", 0);
        pubSub.subscribe(subscriber1, "trades", 0);
        pubSub.subscribe(subscriber3, "futures", 0);
        System.out.println();

        pubSub.publish(publisher1, "orders", new Message("order1"));
        pubSub.publish(publisher1, "orders", new Message("order2"));
        pubSub.publish(publisher1, "orders", new Message("order3"));
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }
        pubSub.publish(publisher2, "futures", new Message("future 1"));

        pubSub.resetOffset("order", subscriber3, 1);
        pubSub.resetOffset("orders", subscriber2, 1);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        pubSub.shutDown();
    }
}
