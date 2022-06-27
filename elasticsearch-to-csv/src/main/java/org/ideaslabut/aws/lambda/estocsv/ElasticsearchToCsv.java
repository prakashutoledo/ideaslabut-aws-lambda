/*
 * Copyright 2022 IDEAS Lab @ UT. All rights reserved.
 */
package org.ideaslabut.aws.lambda.estocsv;

import org.ideaslabut.aws.lambda.domain.elasticsearch.Response;
import org.ideaslabut.aws.lambda.domain.elasticsearch.SourceHits;
import org.ideaslabut.aws.lambda.domain.elasticsearch.request.SearchRequest;
import org.ideaslabut.aws.lambda.domain.sneaky.NoArgUncheckedIOConsumer;
import org.ideaslabut.aws.lambda.domain.sneaky.UncheckedIOConsumer;
import org.ideaslabut.aws.lambda.extractor.util.CSVWriter;
import org.ideaslabut.aws.lambda.extractor.util.ProgressBar;
import org.ideaslabut.aws.lambda.service.ElasticsearchService;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A class with main method to convert elasticsearch response to csv format
 *
 * @author Prakash Khadka <br>
 *         Created on: Jan 30, 2022
 */
public class ElasticsearchToCsv {
    private static final ElasticsearchService ELASTICSEARCH_SERVICE = ElasticsearchService.getInstance();

    public static void main(String[] args) {
        Stream.of(IndexMap.of("accelerometer", 1000),
            IndexMap.of("gyroscope", 1000),
            IndexMap.of("gsr", 20),
            IndexMap.of("heartrate", 5),
            IndexMap.of("ibi", 5),
            IndexMap.of("bvp", 5),
            IndexMap.of("temperature", 1)
        ).forEach(ElasticsearchToCsv::searchAll);
    }

    /**
     * Performs search all elasticsearch operation for given index map
     *
     * @param indexMap an index map to perform
     */
    private static void searchAll(IndexMap indexMap) {
        searchAll(indexMap.indexName, indexMap.size);
    }

    /**
     * Performs search all elasticsearch operation for given index map
     *
     * @param indexName a index name to search
     * @param size a default search request size
     */
    private static void searchAll(String indexName, int size) {
        System.out.println();
        long startTime = System.currentTimeMillis();
        var totalElementSearch = SearchRequest.builder().withSize(1).withIndex(indexName).build();
        var totalElementSearchRequest = ElasticsearchService.getInstance().search(totalElementSearch);
        if (totalElementSearchRequest.isEmpty() || totalElementSearchRequest.get().getHits().getTotal().getValue() <= 0) {
            System.out.println("Index " + indexName+ " has no document to search");
            return;
        }

        final var progressBar = ProgressBar.builder()
            .withPrintStream(System.out)
            .withDelimiter("#")
            .withPrefix("Downloading: " + indexName)
            .withTotalElement(totalElementSearchRequest.get().getHits().getTotal().getValue())
            .withMaxStep(100).build();

        progressBar.initStartTime(startTime);

        final var csvWriter = CSVWriter.builder().withDelimiter(",").withFileName(indexName).build();

        var consumer = UncheckedIOConsumer.wrap((Response response) -> {
            var hits = response.getHits().getHits();
            progressBar.updateBy(hits.size());
            csvWriter.writeProperties(hits.stream().map(SourceHits::getSource).collect(Collectors.toList()));
            csvWriter.flush();
        });

        var onComplete = NoArgUncheckedIOConsumer.wrap(() -> {
            csvWriter.flush();
            csvWriter.close();
        });

        progressBar.initStartTime();
        var searchRequest = SearchRequest.builder()
            .withSize(size)
            .withScroll("1m")
            .withIndex(indexName)
            .build();

        ELASTICSEARCH_SERVICE.searchAll(searchRequest, consumer, onComplete);
    }

    /**
     * A pojo which holds default search request size for elasticsearch index
     */
    private static class IndexMap {
        private final String indexName;
        private final int size;

        private IndexMap(String indexName, int size) {
            this.indexName = indexName;
            this.size = size;
        }

        public static IndexMap of(String indexName, int size) {
            return new IndexMap(indexName, size);
        }
    }
}
