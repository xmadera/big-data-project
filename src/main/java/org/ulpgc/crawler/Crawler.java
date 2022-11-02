package org.ulpgc.crawler;

import java.io.File;
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

import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

public class Crawler implements CrawlerInterface {
    Timer timer;
    static int id;
    static int maxId;


    public void timerCrawler(int id, int maxId) {
        Crawler.id = id;
        Crawler.maxId = maxId;

        timer = new Timer();
        timer.schedule(new crawlerTask(), 1000, 60000); // execute CrawlerTask every 60 seconds
    }

    static class crawlerTask extends TimerTask {
        public void run() {
            setupCrawler();
//          timer.cancel(); //Terminate the timer thread
        }
    }

    private static void setupCrawler() {
        String url = "https://www.gutenberg.org/cache/epub/";
        String currentDate = getCurrentTime();

        String fileRepo = "src/main/document_repository";
        String fileDir = "src/main/document_repository/%s".formatted(currentDate);

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

        try {
            crawl(fileDir, url);
        } catch (IOException e) {
            System.err.println("Failed crawl website!: " + e.getMessage());
//            e.printStackTrace();
        }
    }

    private static void crawl(String fileDir, String url) throws IOException {

        // Number of threads based on individual hardware
        System.out.println("Number of available threads " + Runtime.getRuntime().availableProcessors());
        final ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

        IntStream range = IntStream.rangeClosed(id, maxId);

        forkJoinPool.submit(() -> {
            range.parallel().forEach(x -> {
                String formattedUrl = url + "%s/pg%s.txt".formatted(x, x);
                String formattedFileDir = fileDir + "/%s.txt".formatted(x);

                try {
                    downloadUsingNIO(formattedUrl, formattedFileDir);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    private static void downloadUsingNIO(String formattedUrl, String formattedFileDir) throws IOException {
        URL url = new URL(formattedUrl);
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(formattedFileDir);
        System.out.println("Downloaded URL: " + url);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
    }

    private static String getCurrentTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();
        return dateFormat.format(date);
    }
}