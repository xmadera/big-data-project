package org.ulpgc;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.ulpgc.crawler.Crawler;
import org.ulpgc.invertedIndex.InvertedIndex;
import static spark.Spark.*;
import org.apache.spark.sql.SparkSession;

public class Main {

    public static void main(String[] args) {

//        SparkSession spark = SparkSession
//                .builder()
//                .appName("Java Spark SQL basic example")
//                .config("spark.master", "local")
//                .getOrCreate();
//
//        Dataset<Row> df = spark.read().json("words.json");
//
//        df.show();
//
//        get("/hello", (req, res) -> "Hello World!");

//        df.show();
//
            new Crawler();
    }
}