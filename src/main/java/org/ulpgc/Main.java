package org.ulpgc;

import org.apache.spark.sql.*;

// Edit configuration -> Add the JVM option "--add-exports java.base/sun.nio.ch=ALL-UNNAMED"

public class Main {

    public static void main(String[] args) {
//        TODO: create table document -> based on documents array attribute in table words

        SparkSession spark = SparkSession
                .builder()
                .appName("Java Spark SQL basic example")
                .config("spark.master", "local")
                .getOrCreate();

        Dataset<Row> words = spark.read().json("wordsFormatted.json");
        Dataset<Row> meta = spark.read().json("metadata.json");

        System.out.println("Schema\n=======================");
        words.printSchema();
        meta.printSchema();

        words.createOrReplaceTempView("words");
        meta.createOrReplaceTempView("meta");

//        Dataset<Row> language = spark.sql("SELECT language FROM words");
        System.out.println("\n\nSQL Result\n=======================");

        words.show();
        meta.show();

        System.out.println("\n\nSQL Result JOIN tables\n=======================");

        spark.sql("SELECT * FROM meta m JOIN words w ON m.id_doc = w.id").show();

        spark.stop();
//
//        get("/hello", (req, res) -> "Hello World!");
//        new Crawler();
//
//        new InvertedIndex();
    }
}