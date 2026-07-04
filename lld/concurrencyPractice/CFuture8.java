import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CFuture8 {
    public static CompletableFuture<String> getUser(){
        return CompletableFuture.supplyAsync(()->{
            System.out.println("Fetching user...");
            return "DK";
        });
    }
    public static CompletableFuture<Integer> getAge(String user) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("Fetching age for " + user);
            if(Math.random()>0.5) throw new RuntimeException();
            return 23;
        });
    }
    public static CompletableFuture<String> getCity(){
        return CompletableFuture.supplyAsync(()->{
            System.out.println("Fetching city...");
            return "Mumbai";
        });
    }
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        CompletableFuture<Integer> future=getUser().thenCompose(x->getAge(x))
        .handle((result,ex)->{
            if(ex!=null) return 30;
            return result;
        });
        //Thread.sleep(2000);
        System.out.println(future.join());

        CompletableFuture<String> user=getUser();
        CompletableFuture<String> city=getCity();

        CompletableFuture<String> combined=user.thenCombine(city, (result1, result2)->result1+" lives in "+result2).whenComplete((result,ex)->{
            System.out.println("Pipeline Finished");
        });
        System.out.println(combined.join());

        System.out.println();
        CompletableFuture<Integer> future1=CompletableFuture.supplyAsync(()->10).thenApply(x->x*2).thenApply(x -> {
            if(Math.random()>0.5) throw new RuntimeException();
            return x+5;
        }).whenComplete((res,ex)->{
            if(ex!=null) System.out.println("Exception occured");
            if(res!=null) System.out.println("Pipeline finished");
        })
        .exceptionally(ex->20);

        System.out.println(future1.join());

        System.out.println();
        CompletableFuture<String> t1=getUser();
        CompletableFuture<Integer> t2=t1.thenCompose(u -> getAge(u));
        CompletableFuture<String> t3=getCity();

        CompletableFuture<Void> allof=CompletableFuture.allOf(t1,t2,t3);
        allof.thenRun(()->{
            System.out.println("User: "+t1.join());
            System.out.println("Age: "+t2.join());
            System.out.println("City: "+t3.join());
        });

        System.out.println();
        System.out.println("Any of practice");
        
        CompletableFuture<String> first=CompletableFuture.supplyAsync(()->{
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return "first";
        });
        CompletableFuture<String> second=CompletableFuture.supplyAsync(()->{
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return "second";
        });
        CompletableFuture<String> third=CompletableFuture.supplyAsync(()->{
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return "third";
        });
        CompletableFuture<Object> anyof=CompletableFuture.anyOf(first,second,third);
        System.out.println("First completed: " + anyof.join());
    }
}
