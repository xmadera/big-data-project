package org.ulpgc.invertedIndex;

import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import com.google.gson.Gson;
import one.util.streamex.EntryStream;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import com.github.pemistahl.lingua.api.LanguageDetector;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.ulpgc.tools.StringTools.isStringNumeric;

public class InvertedIndex {

    Timer timer;

    static final String DOCUMENTS_MAP_KEY = "documents";
    static final String LANGUAGE_MAP_KEY = "language";
    static final String ID_MAP_KEY = "id";
    static final String TITLE_MAP_KEY = "title";
    static final String AUTHOR_MAP_KEY = "author";
    static final String RELEASE_DATE_MAP_KEY = "release_date";
    static final String DATA_MART_PATH = "src/main/data_mart/";
    static final String DATA_LAKE_PATH = "src/main/data_lake/";
    static final String WORDS_JSON_FILE = DATA_MART_PATH + "words.json";
    static final String METADATA_JSON_FILE = DATA_MART_PATH + "metadata.json";

    static final ArrayList<String> TAGS_TO_REMOVE = new ArrayList<String>(List.of("NUM", "DET", "ADP", "AUX"));

    static ArrayList<File> listOfProcessedFiles = new ArrayList<>();
    static Map<Object, Map<Object, Object>> multiMap = new HashMap<>();
    static Map<Object, Object> documentMetadataMap = new HashMap<>();
    static Gson gson = new Gson();
    static ClassLoader classloader = Thread.currentThread().getContextClassLoader();

    static InputStream posModelInEN = classloader.getResourceAsStream("opennlp-en-ud-ewt-pos-1.0-1.9.3.bin");
    static InputStream posModelInDE = classloader.getResourceAsStream("opennlp-de-ud-gsd-pos-1.0-1.9.3.bin");
    static InputStream posModelInFR = classloader.getResourceAsStream("opennlp-fr-ud-ftb-pos-1.0-1.9.3.bin");

    static POSTaggerME posTaggerEN = null;
    static POSTaggerME posTaggerDE = null;
    static POSTaggerME posTaggerFR = null;

    static final LanguageDetector detector = LanguageDetectorBuilder.fromLanguages(Language.ENGLISH, Language.GERMAN, Language.FRENCH).build();

    public InvertedIndex() {
        try {
            File theRepo = new File(DATA_MART_PATH);

            if (!Files.exists(Path.of(theRepo.toURI()))) {
                Files.createDirectory(theRepo.toPath());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            POSModel posModelEN = new POSModel(posModelInEN);
            POSModel posModelDE = new POSModel(posModelInDE);
            POSModel posModelFR = new POSModel(posModelInFR);

            for (InputStream inputStream : Arrays.asList(posModelInEN, posModelInDE, posModelInFR)) {
                if (inputStream != null) inputStream.close();
            }

            posTaggerEN = new POSTaggerME(posModelEN);
            posTaggerDE = new POSTaggerME(posModelDE);
            posTaggerFR = new POSTaggerME(posModelFR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        inverted_index_of();
    }

    public void inverted_index_of() {

        // Get list of txt files from data lake folder
        File folder = new File(DATA_LAKE_PATH);

        if (folder.listFiles() == null) return;

        ArrayList<File> tempListFiles = new ArrayList<>(List.of(Objects.requireNonNull(folder.listFiles())));
        tempListFiles.removeIf(file -> listOfProcessedFiles.contains(file));
        listOfProcessedFiles.addAll(tempListFiles);

        List<String> documentPaths = tempListFiles.stream().map(File::getPath).collect(Collectors.toList());

        if (documentPaths.isEmpty()) return;

        System.out.println("Documents to process inside inverted_index: " + documentPaths);
        executeInvertedIndex(documentPaths);
    }

    public static void executeInvertedIndex(List<String> documentList) {
        String documentId;
        Scanner scanner;

        for (String document : documentList) {
            ArrayList<String> documentWords = new ArrayList<>();

            try {
                File file = new File(document);
                documentId = file.getName().substring(0, file.getName().lastIndexOf('.'));
                scanner = new Scanner(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            documentMetadataMap.put(ID_MAP_KEY, documentId);

            while (scanner.hasNextLine()) {
                String nextLine = scanner.nextLine();
                if (nextLine.contains("Title: ")) {
                    String title = StringUtils.substringAfter(nextLine, "Title: ");
                    documentMetadataMap.put(TITLE_MAP_KEY, title);
                }
                if (nextLine.contains("Author: ")) {
                    String author = StringUtils.substringAfter(nextLine, "Author: ");
                    documentMetadataMap.put(AUTHOR_MAP_KEY, author);
                }
                if (nextLine.contains("Release date: ")) {
                    String releaseDate = StringUtils.substringAfter(nextLine, "Release date: ");
                    documentMetadataMap.put(RELEASE_DATE_MAP_KEY, releaseDate);
                }
                if (nextLine.contains("Language: ")) {
                    String language = StringUtils.substringAfter(nextLine, "Language: ");
                    documentMetadataMap.put(LANGUAGE_MAP_KEY, language);
                }
            }

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

                documentWordsLanguageMap.put(token[0], detectedLanguage.getIsoCode639_1().toString().toLowerCase());

                if (detectedLanguage == Language.ENGLISH) {
                    tag = posTaggerEN.tag(token);
                } else if (detectedLanguage == Language.GERMAN) {
                    tag = posTaggerDE.tag(token);
                } else if (detectedLanguage == Language.FRENCH) {
                    tag = posTaggerFR.tag(token);
                }

                if (tag[0] != null) {
                    if (TAGS_TO_REMOVE.contains(tag[0])) {
                        documentWordsIterator.remove();
                    }
                }
            }

            String finalDocumentId = documentId;
            documentWords.forEach(word -> {
                if (!multiMap.containsKey(word)) multiMap.put(word, new HashMap<>());

                if (multiMap.get(word).containsKey(DOCUMENTS_MAP_KEY)) {
                    ((ArrayList<Object>) multiMap.get(word).get(DOCUMENTS_MAP_KEY)).add(finalDocumentId);
                } else {
                    ArrayList<Object> newArray = new ArrayList<>();
                    newArray.add(finalDocumentId);
                    multiMap.get(word).put(DOCUMENTS_MAP_KEY, newArray);
                    multiMap.get(word).put(LANGUAGE_MAP_KEY, documentWordsLanguageMap.get(word));
                }
            });

            scanner.close();
        }

        try {
            File existingFile = new File(WORDS_JSON_FILE);
            if (existingFile.isFile()) {
                FileReader myReader = new FileReader(WORDS_JSON_FILE);
                Map<Object, Map<Object, Object>> existingMultimap = gson.fromJson(myReader, Map.class);

                multiMap = EntryStream.of(existingMultimap).append(EntryStream.of(multiMap)).toMap((w1, w2) -> {
                    ((ArrayList<Object>) w1.get(DOCUMENTS_MAP_KEY)).addAll(((ArrayList<Object>) w2.get(DOCUMENTS_MAP_KEY)));
                    return w1;
                });
            }

            String json = gson.toJson(multiMap);
            multiMap.clear();

            FileWriter myWriter = new FileWriter(WORDS_JSON_FILE);
            myWriter.write(json);
            myWriter.close();
            System.out.println("Words json updated");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
