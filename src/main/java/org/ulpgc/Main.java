package org.ulpgc;

import org.ulpgc.crawler.Crawler;
import org.ulpgc.invertedIndex.InvertedIndex;

public class Main {
    public static void main(String[] args) {
        new Crawler();

        new InvertedIndex();
    }
}