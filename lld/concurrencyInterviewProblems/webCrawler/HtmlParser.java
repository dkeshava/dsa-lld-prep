package lld.concurrencyInterviewProblems.webCrawler;

import java.util.List;

public class HtmlParser {
    public List<String> getUrls(String url) {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return MockWebGraph.getUrls(url);
    }
}
