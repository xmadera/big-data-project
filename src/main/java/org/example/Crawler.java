package org.example;

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

//Timer and threads
//https://www.digitalocean.com/community/tutorials/java-timer-timertask-example

public class Crawler {
    Timer timer;

    public Crawler() {
        timer = new Timer();
        timer.schedule(new CrawlerTask(), 1000, 60000); // execute CrawlerTask every 60 seconds
    }

    public static void main(String[] args) {
        new Crawler();
    }

    static class CrawlerTask extends TimerTask {
        public void run() {
            setupCrawler();
//          timer.cancel(); //Terminate the timer thread
        }
    }

    private static void setupCrawler() {
        String url = "https://www.gutenberg.org/cache/epub/";
        String currentDate = getCurrentTime();
        String fileDir = "src/main/document_repository/%s".formatted(currentDate);

        try {
            File theDir = new File(fileDir);
            //        file.exists() && !file.isDirectory();
            if (!Files.exists(Path.of(fileDir))) {
                Files.createDirectory(theDir.toPath());
            }
        } catch (IOException e) {
            System.err.println("Failed to create directory!: " + e.getMessage());
        }

        try {
            crawl(1, 10, fileDir, url);
        } catch (IOException e) {
            System.err.println("Failed crawl website!: " + e.getMessage());
//            e.printStackTrace();
        }
    }

    private static void crawl(int id, int maxId, String fileDir, String url) throws IOException {
        if (id <= maxId) {
            String formattedUrl = url + "%s/pg%s.txt".formatted(id, id);
            String formattedFileDir = fileDir + "/%s.txt".formatted(id);

            downloadUsingNIO(formattedUrl, formattedFileDir);
            crawl(id + 1, maxId, fileDir, url);
        }
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