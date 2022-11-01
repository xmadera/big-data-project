package org.example.invertedIndex;

import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import com.google.gson.Gson;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import com.github.pemistahl.lingua.api.LanguageDetector;

import java.io.*;
import java.net.URL;
import java.util.*;

import static org.example.tools.StringTools.isStringNumeric;

public class InvertedIndex implements InvertedIndexInterface {

    static final String DOCUMENTS_MAP_KEY = "documents";
    static final String LANGUAGE_MAP_KEY = "language";

    @Override
    @SuppressWarnings(value = "unchecked")
    public void inverted_index_of(List<String> documentList) {
        Gson gson = new Gson();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        InputStream posModelInEN = classloader.getResourceAsStream("opennlp-en-ud-ewt-pos-1.0-1.9.3.bin");
        InputStream posModelInDE = classloader.getResourceAsStream("opennlp-de-ud-gsd-pos-1.0-1.9.3.bin");
        InputStream posModelInFR = classloader.getResourceAsStream("opennlp-fr-ud-ftb-pos-1.0-1.9.3.bin");

        POSTaggerME posTaggerEN = null;
        POSTaggerME posTaggerDE = null;
        POSTaggerME posTaggerFR = null;

        try {
            assert posModelInEN != null;
            POSModel posModelEN = new POSModel(posModelInEN);
            assert posModelInDE != null;
            POSModel posModelDE = new POSModel(posModelInDE);
            assert posModelInFR != null;
            POSModel posModelFR = new POSModel(posModelInFR);

            posTaggerEN = new POSTaggerME(posModelEN);
            posTaggerDE = new POSTaggerME(posModelDE);
            posTaggerFR = new POSTaggerME(posModelFR);

        } catch (IOException e) {
            e.printStackTrace();
        }

        String documentId = null;

        Scanner scanner = null;

        Map<Object, Map<Object, Object>> multiMap = new HashMap<>();

        for (String document : documentList) {
            ArrayList<String> documentWords = new ArrayList<>();

            try {
                URL url = new URL(document);

                String[] urlPath = url.getPath().split("/");
                documentId = urlPath[urlPath.length - 2];

                scanner = new Scanner(url.openStream()).useDelimiter("\\W+");

            } catch (IOException e) {
                e.printStackTrace();
            }

            final LanguageDetector detector = LanguageDetectorBuilder.fromLanguages(
                    Language.ENGLISH,
                    Language.GERMAN,
                    Language.FRENCH
            ).build();

            assert scanner != null;

            while (scanner.hasNext()) {
                String nextWord = scanner.next().toLowerCase();
                if (!isStringNumeric(nextWord) && nextWord.length() > 1 && !documentWords.contains(nextWord)) {
                    documentWords.add(nextWord);
                }
            }

            Iterator<String> documentWordsIterator = documentWords.iterator();
            Map<Object, Object> documentWordsLanguageMap = new HashMap<>();

            while (documentWordsIterator.hasNext()) {
                String[] token = new String[1];
                String[] tag = new String[1];
                token[0] = documentWordsIterator.next();

                Language detectedLanguage = detector.detectLanguageOf(token[0]);

                documentWordsLanguageMap.put(token[0], detectedLanguage.getIsoCode639_1());

                assert posTaggerEN != null;

                if (detectedLanguage == Language.ENGLISH) {
                    tag = posTaggerEN.tag(token);
                } else if (detectedLanguage == Language.GERMAN) {
                    tag = posTaggerDE.tag(token);
                } else if (detectedLanguage == Language.FRENCH) {
                    tag = posTaggerFR.tag(token);
                }

                if (tag[0].equals("NUM") || tag[0].equals("DET") || tag[0].equals("ADP") || tag[0].equals("AUX")) {
                    documentWordsIterator.remove();
                }
            }

            String finalDocumentId = documentId;
            assert finalDocumentId != null;

            documentWords.forEach(word -> {
                if (!multiMap.containsKey(word)) multiMap.put(word, new HashMap<>());

                if (multiMap.get(word).containsKey(DOCUMENTS_MAP_KEY)) {
                    ((ArrayList<Object>) multiMap.get(word).get(DOCUMENTS_MAP_KEY)).add(finalDocumentId);
                } else {
                    ArrayList<Object> newArray = new ArrayList<>();
                    newArray.add(finalDocumentId);
                    multiMap.get(word).put(DOCUMENTS_MAP_KEY, newArray);
                }


                multiMap.get(word).put(LANGUAGE_MAP_KEY, documentWordsLanguageMap.get(word));
            });


        }
        try {
            for (InputStream inputStream : Arrays.asList(posModelInEN, posModelInDE, posModelInFR)) {
                if (inputStream != null) inputStream.close();
            }
            if (scanner != null) scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String json = gson.toJson(multiMap);

        try {
            FileWriter myWriter = new FileWriter("words.json");
            myWriter.write(json);
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}