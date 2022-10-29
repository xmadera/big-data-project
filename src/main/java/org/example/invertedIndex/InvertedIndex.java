package org.example.invertedIndex;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.example.tools.StringTools.isStringNumeric;

public class InvertedIndex implements InvertedIndexInterface {


    @Override
    public void inverted_index_of(List<String> documentList) {
        for (String document : documentList) {
            ArrayList<String> documentWords = new ArrayList<String>();

            try {
                URL url = new URL(document);
                Scanner s = new Scanner(url.openStream()).useDelimiter("\\W+");

                while (s.hasNext()) {
                    String nextWord = s.next().toLowerCase();

                    if (!isStringNumeric(nextWord)) documentWords.add(nextWord);
                }

                System.out.println(documentWords);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
