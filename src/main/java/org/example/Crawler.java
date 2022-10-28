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

public class Crawler {

    public static void main(String[] args) {
        String url = "https://www.gutenberg.org/cache/epub/";
        String currentDate = getCurrentTime();
        String fileDir = "src/main/document_repository/%s".formatted(currentDate);

        try {
            File theDir = new File(fileDir);
    //        file.exists() && !file.isDirectory();
            if (!Files.exists(Path.of(fileDir))){
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


//public class Crawler {
//    public static void main(String[] args) {
//        String url = "https://www.gutenberg.org/";    // https://www.gutenberg.org/cache/epub/69035/pg69035.txt
//        crawl(1, url, new ArrayList<String>());
//    }
//
//    private static void crawl(int level, String url, ArrayList<String> visited) {
//        if (level <= 5) {
//            System.out.println("Current level: " + level);
//            Document doc = request(url, visited);
//
//            if (doc != null) {
//                for (Element link : doc.select("a[href]")) {
//                    String next_link = link.absUrl("href");
//                    if (!visited.contains(next_link)) {
//                        crawl(level++, next_link, visited);
//                    }
//                }
//            }
//        }
//    }
//
//    private static Document request(String url, ArrayList<String> visited) {
//        try {
//            Connection con = Jsoup.connect(url);
//            Document doc = con.get();
//
//            if (con.response().statusCode() == 200) {
//                System.out.println("Link " + url);
//                System.out.println(doc.title());
//                visited.add(url); // Already visited urls array
//
//                return doc;
//            }
//            return null;
//        }
//        catch(IOException e) {
//            return null;
//        }
//    }
//}