package com.github.mihalyfodor.lucenehello.services;

import com.github.mihalyfodor.lucenehello.models.LuceneHelloDocument;
import com.github.mihalyfodor.lucenehello.models.SearchType;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This service provides an abstraction of the underlying lucene logic, in order to minimize business logic
 * in the controller itself.
 */
@Service
public class LuceneHelloService {

    private DocumentService documentService;

    public LuceneHelloService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void addDocument(LuceneHelloDocument document) throws IOException {
        documentService.addDocument(document.getTitle(), document.getText());
    }

    public void addDocuments(List<LuceneHelloDocument> documents) throws IOException {
        for (LuceneHelloDocument document : documents) {
            documentService.addDocument(document.getTitle(), document.getText());
        }
    }

    public List<LuceneHelloDocument> searchDocuments(String field, String text) throws IOException, ParseException {
        return documentService.searchIndex(field, text)
                .stream()
                .map(e -> new LuceneHelloDocument(e.get("title"), e.get("text")))
                .collect(Collectors.toList());
    }

    public List<LuceneHelloDocument> advancedSearchDocuments(String field, String type, String text) throws IOException, ParseException {

        SearchType searchType;

        try {
            searchType = SearchType.valueOf(type);
        } catch (NullPointerException | IllegalArgumentException e) {
            searchType = SearchType.OTHER;
        }

        return documentService.searchIndex(field, searchType, text)
                .stream()
                .map(e -> new LuceneHelloDocument(e.get("title"), e.get("body")))
                .collect(Collectors.toList());
    }
}
