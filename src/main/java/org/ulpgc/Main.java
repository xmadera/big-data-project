package org.ulpgc;

import org.ulpgc.crawler.Crawler;
import org.ulpgc.invertedIndex.InvertedIndex;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Crawler crawler = new Crawler();
        crawler.timerCrawler(5, 15);

        Thread.sleep(6000);

        InvertedIndex invertedIndex = new InvertedIndex();
        invertedIndex.inverted_index_of();
    }
}