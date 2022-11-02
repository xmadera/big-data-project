package org.ulpgc;

import org.ulpgc.crawler.Crawler;
import org.ulpgc.invertedIndex.InvertedIndex;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Crawler crawler = new Crawler();
        crawler.timerCrawler(5, 15);

        // TODO: execute after first crawling
        InvertedIndex invertedIndex = new InvertedIndex();

        // Get list of txt files from folder based on current time
        String currentDate = getCurrentTime();
        File folder = new File("src/main/document_repository/%s".formatted(currentDate));
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;
        String[] documentsList = new String[listOfFiles.length];
        for (int i = 0; i < listOfFiles.length; i++) {
            documentsList[i] = listOfFiles[i].getPath();
        }

        invertedIndex.inverted_index_of(List.of(documentsList));
    }

    private static String getCurrentTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();
        return dateFormat.format(date);
    }
}