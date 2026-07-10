package lld.concurrencyInterviewProblems.webCrawler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class WebCrawler {
    private ExecutorService executor;
    private HtmlParser parser;
    private Set<String> visited;
    private String hostname;
    private AtomicInteger urlsPending;

    public WebCrawler() {
        executor = Executors.newFixedThreadPool(5);
        parser = new HtmlParser();
        visited = ConcurrentHashMap.newKeySet();
        urlsPending = new AtomicInteger(0);
    }
    public List<String> crawl(String startUrl){
        hostname=startUrl.split("/")[2];
        urlsPending.addAndGet(1);
        visited.add(startUrl);
        
        executor.submit(()->crawlPage(startUrl));

        while(urlsPending.get()>0){
            try {
                // Sleep to avoid busy waiting
                Thread.sleep(10);
              } catch (Exception e) {
                // Handle exceptions
                System.out.println(e);
              }
        }

        executor.shutdown();
        return new ArrayList<>(visited);
    }

    private void crawlPage(String url){
        System.out.println(
            Thread.currentThread().getName() +
            " crawling " +
            url +
            " | pending=" +
            urlsPending.get()
        );
        for(String curUrl: parser.getUrls(url)){
            String curHostname=curUrl.split("/")[2];
            if(visited.add(curUrl) && curHostname.equals(hostname)){
                urlsPending.addAndGet(1);
                executor.submit(()->crawlPage(curUrl));
                //crawlPage(curUrl);
            }
        }
        urlsPending.addAndGet(-1);
    }
    public static void main(String[] args) {
        
        //System.out.println(parser.getUrls("https://site.com"));

        String testUrl="https://site.com/careers";
        WebCrawler crawler=new WebCrawler();
        List<String> crawledList=crawler.crawl(testUrl);

        System.out.println();
        for(String a: crawledList){
            System.out.println(a);
        }
    }
}
