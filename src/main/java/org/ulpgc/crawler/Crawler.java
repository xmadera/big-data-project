package org.ulpgc.crawler;

import org.ulpgc.invertedIndex.InvertedIndex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.text.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import java.util.Timer;
import java.util.TimerTask;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

public class Crawler implements CrawlerInterface {
    static InvertedIndex invertedIndex;
    Timer timer;
    static int id;
    static int endId;
    static IntStream range;

    // Number of threads based on individual hardware
    static final ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

    public Crawler() {
        timerCrawler(0, 15);
    }

    public void timerCrawler(int id, int endId) {
        Crawler.invertedIndex = new InvertedIndex();
        Crawler.id = id;
        Crawler.endId = endId;

        timer = new Timer();
        timer.schedule(new crawlerTask(), 0, 60_000); // execute CrawlerTask every 60 seconds
    }

    static class crawlerTask extends TimerTask {
        public void run() {
            setupCrawler();
        }
    }

    private static void setupCrawler() {
        Crawler.range = IntStream.rangeClosed(id, endId);

        String url = "https://www.gutenberg.org/files/";
        String currentDate = getCurrentTime();

        String fileRepo = "src/main/document_repository";
        String fileDir = String.format("src/main/document_repository/%s", currentDate);

        try {
            File theRepo = new File(fileRepo);
            File theDir = new File(fileDir);

            if (!Files.exists(Path.of(theRepo.toURI()))) {
                Files.createDirectory(theRepo.toPath());
            }

            if (!Files.exists(Path.of(fileDir))) {
                Files.createDirectory(theDir.toPath());
            }
        } catch (IOException e) {
            System.err.println("Failed to create directory!: " + e.getMessage());
        }

        crawl(fileDir, url);
    }

    private static void crawl(String fileDir, String url) {

        try {
            forkJoinPool.submit(() -> range.parallel().forEach(x -> {
                String formattedUrl = url + String.format("%s/%s-0.txt", x, x);
                String formattedFileDir = fileDir + String.format("/%s.txt", x);

                downloadUsingNIO(formattedUrl, formattedFileDir);
            })).get();

            Crawler.id += Crawler.endId + 1;
            Crawler.endId += Crawler.endId;
            System.out.println("Crawler finished");

            // start inverted index
            Crawler.invertedIndex.inverted_index_of();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static void downloadUsingNIO(String formattedUrl, String formattedFileDir) {
        try {
            URL url = new URL(formattedUrl);
            ReadableByteChannel rbc = Channels.newChannel(url.openStream());
            FileOutputStream fos = new FileOutputStream(formattedFileDir);
            System.out.println("Downloaded URL: " + url);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            rbc.close();
        } catch (FileNotFoundException e) {
            System.out.println("File " + formattedUrl + " does not exist!");
        } catch (IOException e) {
            System.out.println("File " + formattedUrl + " could not be downloaded!");
        }
    }

    private static String getCurrentTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();
        return dateFormat.format(date);
    }
}