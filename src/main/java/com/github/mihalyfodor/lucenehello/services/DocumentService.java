package com.github.mihalyfodor.lucenehello.services;

import com.github.mihalyfodor.lucenehello.models.SearchType;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Our business logic for actually interacting with lucene.
 * It is session scoped for this first implementation, since we are using an in memory index.
 */
@Service
@SessionScope
public class DocumentService {

    public static final int NUMBER_OF_TOP_MATCHES = 10;
    private Directory memoryIndex = new RAMDirectory();

    /**
     * Analysers define how lucene interprets the documets. There are several available.
     * The Standard one cleans up the text a bit by removing common connecting words and treating everything as lowercase.
     */
    private StandardAnalyzer analyzer = new StandardAnalyzer();

    /**
     * Lucene takes keywords and then it maps them to the underlying data when indexing or searching.
     * For an index here we take documents defined by title and body fields, the parameters we are interested in.
     * Also it is interesting to note that lucene won't give absolute results, but best matching ones
     *
     * @param title title of document
     * @param body description of document
     *
     * @throws IOException when an error occurs
     */
    public void addDocument(String title, String body) throws IOException {

        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        try (IndexWriter writer = new IndexWriter(memoryIndex, indexWriterConfig)) {
            Document document = new Document();
            document.add(new TextField("title", title, Field.Store.YES));
            document.add(new TextField("body", body, Field.Store.YES));
            document.add(new SortedDocValuesField("title", new BytesRef(title)));
            writer.addDocument(document);
        }
    }

    /**
     * Lucene provides a very robust searching mechanism, allowing for wildcards, starting with, etc.
     * This is handled by the query string itself, no need to pass in extra parameters.
     *
     * @param inField field to search
     * @param queryString query expression to use
     * @return found documents
     *
     * @throws ParseException when an error occurs
     * @throws IOException when an error occurs
     */
    public List<Document> searchIndex(String inField, String queryString) throws ParseException, IOException {
        Query query = new QueryParser(inField, analyzer).parse(queryString);
        IndexReader indexReader = DirectoryReader.open(memoryIndex);
        IndexSearcher searcher = new IndexSearcher(indexReader);
        TopDocs topDocs = searcher.search(query, NUMBER_OF_TOP_MATCHES);
        List<Document> documents = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            documents.add(searcher.doc(scoreDoc.doc));
        }

        return documents;
    }

    /**
     * Building on top of the previous search we can also specify the type of search we want to do.
     *
     * @param inField field to search
     * @param type the type of the search
     * @param queryString query expression to use
     * @return found documents
     *
     * @throws ParseException when an error occurs
     * @throws IOException when an error occurs
     */
    public List<Document> searchIndex(String inField, SearchType type, String queryString)  throws ParseException, IOException {

        Term term = new Term(inField, queryString);
        switch(type) {
            // term is the most basic one, it just goes after the word given
            case TERM: return searchIndex(new TermQuery(term));
            // prefix is the starts with search
            case PREFIX: return searchIndex(new PrefixQuery(term));
            // wildcard allows for * and ?
            case WILDCARD: return searchIndex(new WildcardQuery(term));
            // fuzzy is searching for similar results, not necessarily identical
            case FUZZY: return searchIndex(new FuzzyQuery(term));
            // as backup
            default: return searchIndex(inField, queryString);
        }

    }

    private List<Document> searchIndex(Query query) throws IOException {
        IndexReader indexReader = DirectoryReader.open(memoryIndex);
        IndexSearcher searcher = new IndexSearcher(indexReader);
        TopDocs topDocs = searcher.search(query, NUMBER_OF_TOP_MATCHES);
        List<Document> documents = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            documents.add(searcher.doc(scoreDoc.doc));
        }
        return documents;
    }


}
