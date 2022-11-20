package org.ulpgc.crawler;

import org.ulpgc.invertedIndex.InvertedIndex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
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
    static final String DATA_LAKE_PATH = "src/main/data_lake/";
    static final String GUTENBERG_FILES_URL = "https://www.gutenberg.org/files/";

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
        timer.schedule(new crawlerTask(), 0, 10_000); // execute CrawlerTask every 60 seconds TODO
    }

    static class crawlerTask extends TimerTask {
        public void run() {
            setupCrawler();
        }
    }

    private static void setupCrawler() {
        Crawler.range = IntStream.rangeClosed(id, endId);

        try {
            File theRepo = new File(DATA_LAKE_PATH);

            if (!Files.exists(Path.of(theRepo.toURI()))) {
                Files.createDirectory(theRepo.toPath());
            }
        } catch (IOException e) {
            System.err.println("Failed to create directory!: " + e.getMessage());
        }

        crawl();
    }

    private static void crawl() {

        try {
            forkJoinPool.submit(() -> range.parallel().forEach(x -> {
                String formattedUrl = GUTENBERG_FILES_URL + String.format("%s/%s-0.txt", x, x);
                String formattedFileDir = DATA_LAKE_PATH + String.format("/%s.txt", x);

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
}