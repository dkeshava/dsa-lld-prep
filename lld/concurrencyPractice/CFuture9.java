import java.util.concurrent.CompletableFuture;

public class CFuture9 {
    public static CompletableFuture<String> fetchUser(){
        return CompletableFuture.supplyAsync(()->{
            System.out.println("Fetching user...");
            return "DK";
        });
    }
    public static CompletableFuture<Integer> fetchOrders(String user) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("Fetching orders for " + user);
            //if(Math.random()>0.5) throw new RuntimeException();
            return 4;
        });
    }
    public static CompletableFuture<String> fetchAddress(){
        return CompletableFuture.supplyAsync(()->{
            System.out.println("Fetching city...");
            return "Mumbai";
        });
    }
    public static CompletableFuture<String> fetchRecommendations(){
        return CompletableFuture.supplyAsync(()->{
            System.out.println("Fetching recommendations...");
            return "Bhairavgad Fort";
        });
    }
    public static void main(String[] args) {
        CompletableFuture<String> user=fetchUser();
        CompletableFuture<Integer> orders=user.thenCompose((x)->fetchOrders(x));
        CompletableFuture<String> address=fetchAddress();
        CompletableFuture<String> recommendations=fetchRecommendations();

        CompletableFuture<Void> allof=CompletableFuture.allOf(user,orders,address,recommendations);
        allof.join();
        CompletableFuture<String> combined=user.thenCombine(orders, (res1,res2)->{
            return "User: "+res1+" placed "+res2+" orders.";
        })
        .thenCombine(address,(res3,res4)->{
            return res3+" He lives in "+res4+".";
        })
        .thenCombine(recommendations, (res5,res6)->{
            return res5+" His recommendations are "+res6+".";
        });

        System.out.println(combined.join());
    }
}
