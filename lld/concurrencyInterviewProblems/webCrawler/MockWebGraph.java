package lld.concurrencyInterviewProblems.webCrawler;

import java.util.List;
import java.util.Map;

public class MockWebGraph {
    private static final Map<String, List<String>> webGraph = Map.of(
        "https://site.com",
        List.of(
            "https://site.com/about",
            "https://site.com/contact",
            "https://site.com/blog",
            "https://site.com/products",
            "https://site.com/services",
            "https://site.com/careers",
            "https://site.com/support",
            "https://site.com/pricing",
            "https://site.com/faq"
        ),

        "https://site.com/blog",
        List.of(
            "https://site.com/blog/post-1",
            "https://site.com/blog/post-2",
            "https://site.com/blog/post-3",
            "https://site.com/blog/post-4",
            "https://site.com/blog/post-5",
            "https://site.com/about",
            "https://site.com/contact",
            "https://site.com/products",
            "https://site.com/support"
        ),

        "https://site.com/products",
        List.of(
            "https://site.com/products/laptop",
            "https://site.com/products/phone",
            "https://site.com/products/tablet",
            "https://site.com/products/watch",
            "https://site.com/products/headphones",
            "https://site.com/pricing",
            "https://site.com/support",
            "https://site.com/blog",
            "https://site.com/contact"
        ),

        "https://site.com/services",
        List.of(
            "https://site.com/services/cloud",
            "https://site.com/services/security",
            "https://site.com/services/consulting",
            "https://site.com/services/training",
            "https://site.com/services/support",
            "https://site.com/about",
            "https://site.com/contact",
            "https://site.com/careers",
            "https://site.com/blog"
        ),

        "https://site.com/about",
        List.of(
            "https://site.com/team",
            "https://site.com/history",
            "https://site.com/mission",
            "https://site.com/careers",
            "https://site.com/contact",
            "https://site.com/blog",
            "https://site.com/services",
            "https://site.com/products",
            "https://site.com/support"
        ),

        "https://site.com/support",
        List.of(
            "https://site.com/support/docs",
            "https://site.com/support/tickets",
            "https://site.com/support/forum",
            "https://site.com/support/downloads",
            "https://site.com/faq",
            "https://site.com/contact",
            "https://site.com/blog",
            "https://site.com/products",
            "https://site.com/pricing"
        ),

        "https://site.com/careers",
        List.of(
            "https://site.com/careers/backend",
            "https://site.com/careers/frontend",
            "https://site.com/careers/devops",
            "https://site.com/careers/mobile",
            "https://site.com/careers/design",
            "https://site.com/about",
            "https://site.com/contact",
            "https://site.com/blog",
            "https://site.com/services"
        ),

        "https://site.com/contact",
        List.of(
            "https://site.com/support",
            "https://site.com/about",
            "https://site.com/blog",
            "https://site.com/products",
            "https://site.com/services",
            "https://site.com/pricing",
            "https://site.com/faq",
            "https://site.com/team",
            "https://site.com/history"
        ),

        "https://site.com/pricing",
        List.of(
            "https://site.com/products",
            "https://site.com/services",
            "https://site.com/contact",
            "https://site.com/support",
            "https://site.com/blog",
            "https://site.com/faq",
            "https://site.com/about",
            "https://site.com/careers",
            "https://site.com/products/laptop"
        ),

        "https://site.com/faq",
        List.of(
            "https://site.com/support",
            "https://site.com/contact",
            "https://site.com/products",
            "https://site.com/services",
            "https://site.com/blog",
            "https://site.com/about",
            "https://site.com/pricing",
            "https://site.com/team",
            "https://site.com/history"
        )
    );
    public static List<String> getUrls(String url){
        return webGraph.getOrDefault(url, List.of());
    }
}
