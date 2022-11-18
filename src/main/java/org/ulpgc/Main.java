package org.ulpgc;

import org.ulpgc.crawler.Crawler;
import org.ulpgc.invertedIndex.InvertedIndex;
import static spark.Spark.*;

public class Main {
    public static void main(String[] args) {

        get("/hello", (req, res) -> "Hello World");

//        new Crawler();

//        new InvertedIndex();
    }
}