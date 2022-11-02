package org.ulpgc;

import org.ulpgc.crawler.Crawler;
import org.ulpgc.invertedIndex.InvertedIndex;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        InvertedIndex invertedIndex = new InvertedIndex();

        invertedIndex.inverted_index_of(List.of("https://www.gutenberg.org/cache/epub/69042/pg69042.txt",
                "https://www.gutenberg.org/cache/epub/69035/pg69035.txt",
                "https://www.gutenberg.org/cache/epub/69040/pg69040.txt"));

        Crawler crawler = new Crawler();
    }
}