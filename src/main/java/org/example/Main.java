package org.example;

import org.example.invertedIndex.InvertedIndex;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        InvertedIndex invertedIndex = new InvertedIndex();

        invertedIndex.inverted_index_of(List.of("https://www.gutenberg.org/cache/epub/69042/pg69042.txt"));    }
}