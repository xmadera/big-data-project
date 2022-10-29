package org.example.invertedIndex;

import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import com.github.pemistahl.lingua.api.LanguageDetector;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import static org.example.tools.StringTools.isStringNumeric;

public class InvertedIndex implements InvertedIndexInterface {

    @Override
    public void inverted_index_of(List<String> documentList) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        InputStream posModelInEN = classloader.getResourceAsStream("opennlp-en-ud-ewt-pos-1.0-1.9.3.bin");
        InputStream posModelInDE = classloader.getResourceAsStream("opennlp-de-ud-gsd-pos-1.0-1.9.3.bin");
        InputStream posModelInFR = classloader.getResourceAsStream("opennlp-fr-ud-ftb-pos-1.0-1.9.3.bin");

        Scanner scanner = null;

        for (String document : documentList) {
            ArrayList<String> documentWords = new ArrayList<>();

            try {
                URL url = new URL(document);
                scanner = new Scanner(url.openStream()).useDelimiter("\\W+");

                assert posModelInEN != null;
                POSModel posModelEN = new POSModel(posModelInEN);
                assert posModelInDE != null;
                POSModel posModelDE = new POSModel(posModelInDE);
                assert posModelInFR != null;
                POSModel posModelFR = new POSModel(posModelInFR);

                POSTaggerME posTaggerEN = new POSTaggerME(posModelEN);
                POSTaggerME posTaggerDE = new POSTaggerME(posModelDE);
                POSTaggerME posTaggerFR = new POSTaggerME(posModelFR);

                final LanguageDetector detector = LanguageDetectorBuilder.fromLanguages(
                        Language.ENGLISH,
                        Language.GERMAN,
                        Language.FRENCH
                ).build();

                while (scanner.hasNext()) {
                    String nextWord = scanner.next().toLowerCase();
                    if (!isStringNumeric(nextWord) && nextWord.length() > 1 && !documentWords.contains(nextWord)) {
                        documentWords.add(nextWord);
                    }
                }

                Iterator<String> documentWordsIterator = documentWords.iterator();

                while (documentWordsIterator.hasNext()) {
                    String[] token = new String[1];
                    String[] tag = new String[1];
                    token[0] = documentWordsIterator.next();

                    Language detectedLanguage = detector.detectLanguageOf(token[0]);

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

                System.out.println(documentWords);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (scanner != null) scanner.close();
            }
        }

        try {
            for (InputStream inputStream : Arrays.asList(posModelInEN, posModelInDE, posModelInFR)) {
                if (inputStream != null) inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
