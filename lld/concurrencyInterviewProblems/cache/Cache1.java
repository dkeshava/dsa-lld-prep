package lld.concurrencyInterviewProblems.cache;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

class DoublyLinkedListNode<V>{
    private final V value;
    DoublyLinkedListNode<V> prev;
    DoublyLinkedListNode<V> next;

    public DoublyLinkedListNode(V value){
        this.value=value;
        this.prev=null;
        this.next=null;
    }
    public V getValue(){
        return value;
    }
}
class DoublyLinkedList<K>{
    DoublyLinkedListNode<K> head;
    DoublyLinkedListNode<K> tail;
    public DoublyLinkedList(){
        this.head=null;
        this.tail=null;
    }
    public synchronized void addNodeAtTail(DoublyLinkedListNode<K> node){
        if(tail==null){
            head=tail=node; 
        }
        else{
            tail.next=node;
            node.prev=tail;
            tail=tail.next;
        }
    }
    public synchronized void deleteNode(DoublyLinkedListNode<K> node){
        if(head==node && tail==node){
            head=tail=null;
        }
        else if(head==node){
            head=head.next;
            head.prev=null;
        }
        else if(tail==node){
            tail=tail.prev;
            tail.next=null;
        }
        else{
            DoublyLinkedListNode left=node.prev;
            DoublyLinkedListNode right=node.next;
            left.next=right;
            right.prev=left;
        }
        node.prev = null;
        node.next = null;
    }
    public synchronized DoublyLinkedListNode<K> getHead(){
        return head;
    }
    public synchronized DoublyLinkedListNode<K> getTail(){
        return tail;
    }
    public synchronized void removeHead(){
        deleteNode(head);
    }
    public synchronized void removeTail(){
        deleteNode(tail);
    }
}

interface CacheStorage<K,V> {
    void put(K key,V value);
    V get(K key);
    void remove(K key);
    boolean containsKey(K key);
    int getCapacity() ;
    int size();
}
interface DBStorage<K,V>{
    void write(K key, V value);
    V read(K key);
    void delete(K key);
}

interface EvictionPolicy<K>{
    void keyAccessed(K key);
    K evictKey();
    void removeKey(K key);
}
class InMemoryCacheStorage<K,V> implements CacheStorage<K,V>{
    private final Map<K, V> cache;
    private final int capacity;
    public InMemoryCacheStorage(int capacity){
        this.capacity=capacity;
        this.cache=new ConcurrentHashMap<>();
    }
    @Override
    public void put(K key, V value){
        cache.put(key, value);
    }
    @Override
    public V get(K key){
        return cache.get(key);
    }
    @Override
    public void remove(K key){
        cache.remove(key);
    }
    @Override
    public boolean containsKey(K key){
        return cache.containsKey(key);
    }
    @Override
    public int getCapacity(){return capacity;}
    @Override
    public int size(){return cache.size();}
}
class simpleDBStorage<K,V> implements DBStorage<K, V>{
    private final Map<K,V> db;
    public simpleDBStorage(){
        this.db=new ConcurrentHashMap<>();
    }
    @Override
    public V read(K key){return db.get(key);}
    @Override
    public void write(K key, V value){db.put(key, value);}
    @Override
    public void delete(K key){db.remove(key);}
    public boolean containsKey(K key){
        return db.containsKey(key);
    }
}
class LruEviction<K> implements EvictionPolicy<K>{
    private DoublyLinkedList<K> dll;
    private Map<K, DoublyLinkedListNode<K>> keyToNodeMap;
    public LruEviction(){
        this.dll=new DoublyLinkedList<>();
        this.keyToNodeMap=new ConcurrentHashMap<>();
    }
    @Override
    public synchronized void keyAccessed(K key){
        if(keyToNodeMap.containsKey(key)){
            DoublyLinkedListNode<K> nodeToDetach=keyToNodeMap.get(key);
            dll.deleteNode(nodeToDetach);
            dll.addNodeAtTail(nodeToDetach);
        }
        else{
            DoublyLinkedListNode<K> newNode=new DoublyLinkedListNode<>(key);
            dll.addNodeAtTail(newNode);
            keyToNodeMap.put(key, newNode);
        }
    }
    public synchronized K evictKey(){
        DoublyLinkedListNode<K> head=dll.getHead();
        if(head==null) return null;
        dll.removeHead();
        keyToNodeMap.remove(head.getValue());
        return head.getValue();
    }
    public synchronized void removeKey(K key){
        DoublyLinkedListNode nodeToDelete=keyToNodeMap.get(key);
        dll.deleteNode(nodeToDelete);
        keyToNodeMap.remove(key);
    }
}
interface WritePolicy<K,V>{
    void write(K key, V value, CacheStorage<K,V> cacheStorage, DBStorage<K,V> dbStorage);
}
class WriteThrough<K,V> implements WritePolicy<K, V>{
    @Override
    public void write(K key, V value, CacheStorage<K,V> cacheStorage, DBStorage<K,V> dbStorage){
        CompletableFuture<Void> f1=CompletableFuture.runAsync(()->{
            cacheStorage.put(key, value);
        });
        CompletableFuture<Void> f2=CompletableFuture.runAsync(()->{
            dbStorage.write(key, value);
        });
        CompletableFuture.allOf(f1,f2).join();
    }
}
class CacheManager<K,V>{
    private final CacheStorage<K,V> cacheStorage;
    private final DBStorage<K,V> dbStorage;
    private final WritePolicy<K,V> writePolicy;
    private final EvictionPolicy<K> evictionAlgo;
    private ReentrantLock lock=new ReentrantLock();
    public CacheManager(CacheStorage<K,V> cacheStorage,DBStorage<K,V> dbStorage,WritePolicy<K,V> writePolicy,EvictionPolicy<K> evictionPolicy){
        this.cacheStorage=cacheStorage;
        this.dbStorage=dbStorage;
        this.writePolicy=writePolicy;
        this.evictionAlgo=evictionPolicy;
    }
    public V accessData(K key){
        if(cacheStorage.containsKey(key)){
            evictionAlgo.keyAccessed(key);
            return cacheStorage.get(key);
        }
        else{
            V value = dbStorage.read(key);
            try {
                lock.lock();
                if(value!=null){
                    if(cacheStorage.size()==cacheStorage.getCapacity()){
                        K evictedKey = evictionAlgo.evictKey();
                        cacheStorage.remove(evictedKey);
                    }
                    evictionAlgo.keyAccessed(key);
                    cacheStorage.put(key, value);
                    return value;
                }  
            } catch (Exception e) {
            } finally {
                lock.unlock();
            }
        }
        return null;
    }
    public void updateData(K key, V value){
        if(cacheStorage.containsKey(key)){
            writePolicy.write(key, value, cacheStorage, dbStorage);
            evictionAlgo.keyAccessed(key);
        }
        else{
            try {
                if(cacheStorage.size()==cacheStorage.getCapacity()){
                    K evictedKey = evictionAlgo.evictKey();
                    cacheStorage.remove(evictedKey);
                }
                writePolicy.write(key, value, cacheStorage, dbStorage);
                evictionAlgo.keyAccessed(key);
            } catch (Exception e) {
            }
            finally{
                lock.unlock();
            }
        }
    }
    public void deleteData(K key){
        V value=cacheStorage.get(key);
        if(value!=null){
            cacheStorage.remove(key);
            evictionAlgo.removeKey(key);
        }
        dbStorage.delete(key);
    }
}
public class Cache1 {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        CacheStorage<String, String> cacheStorage=new InMemoryCacheStorage<>(2);
        DBStorage<String, String> dbStorage=new simpleDBStorage();
        WritePolicy<String,String> writePolicy=new WriteThrough<>();
        EvictionPolicy<String> evictionPolicy=new LruEviction<>();
        CacheManager<String,String> cacheManager=new CacheManager<>(cacheStorage,dbStorage,writePolicy,evictionPolicy);
        // ExecutorService executor=Executors.newFixedThreadPool(5);
        // executor.submit(()->{
        //     cacheManager.updateData("A", "1");
        // });
        // executor.submit(()->{
        //     cacheManager.updateData("B", "2");
        // });
        // Future<String> f1=executor.submit(()->{
        //     String a=cacheManager.accessData("A");
        //     return a;
        // });
        // System.out.println(f1.get());
        // cacheManager.updateData("C", "3");
        // String b=cacheManager.accessData("B");
        // System.out.println(b);
        // executor.shutdown();
        ExecutorService pool = Executors.newFixedThreadPool(10);
        for(int i=0;i<100;i++){
            int x=i;
            pool.submit(()->
                cacheManager.updateData("K"+x,"V"+x)
            );
        }
        Thread.sleep(2000);
        for(int i=0;i<100;i++){
            int x=i;
        
            pool.submit(()->
                cacheManager.updateData("A",""+x)
            );
        }
        Thread.sleep(2000);
        pool.shutdown();
        CompletableFuture<String> c1=CompletableFuture.supplyAsync(()->{
            String s=cacheManager.accessData("A");
            return s;
        });
        System.out.println(c1.get());

        System.out.println();
    }
}
