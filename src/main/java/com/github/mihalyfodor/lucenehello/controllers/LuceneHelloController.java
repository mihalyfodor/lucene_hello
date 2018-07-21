package com.github.mihalyfodor.lucenehello.controllers;

import com.github.mihalyfodor.lucenehello.models.LuceneHelloDocument;
import com.github.mihalyfodor.lucenehello.services.LuceneHelloService;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/lucenehello/")
public class LuceneHelloController {

    private LuceneHelloService luceneHelloService;

    public LuceneHelloController(LuceneHelloService luceneHelloService) {
        this.luceneHelloService = luceneHelloService;
    }

    @PostMapping("document")
    public ResponseEntity addDocument(@RequestBody LuceneHelloDocument document) {
        try {
            luceneHelloService.addDocument(document);
        } catch (IOException e) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("documents")
    public ResponseEntity addDocument(@RequestBody List<LuceneHelloDocument> documents) {
        try {
            luceneHelloService.addDocuments(documents);
        } catch (IOException e) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("search/{field}/{text}")
    public ResponseEntity<List<LuceneHelloDocument>> searchDocument(@PathVariable String field, @PathVariable String text) {
        try {
            return new ResponseEntity<>(luceneHelloService.searchDocuments(field, text), HttpStatus.OK);
        } catch (IOException | ParseException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}