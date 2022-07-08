/*
 * Copyright 2022 IDEAS Lab @ University of Toledo. All rights reserved.
 */
package org.ideaslabut.aws.lambda.estocsv;

import static java.util.stream.Collectors.toList;

import org.ideaslabut.aws.lambda.domain.elasticsearch.Response;
import org.ideaslabut.aws.lambda.domain.elasticsearch.SourceHits;
import org.ideaslabut.aws.lambda.domain.elasticsearch.request.SearchRequest;
import org.ideaslabut.aws.lambda.domain.sneaky.NoArgUncheckedIOConsumer;
import org.ideaslabut.aws.lambda.domain.sneaky.UncheckedIOConsumer;
import org.ideaslabut.aws.lambda.extractor.util.CSVWriter;
import org.ideaslabut.aws.lambda.extractor.util.ProgressBar;
import org.ideaslabut.aws.lambda.service.ElasticsearchService;

import java.time.Instant;
import java.util.stream.Stream;

/**
 * A class with main method to convert elasticsearch response to csv format
 *
 * @author Prakash Khadka <br>
 *     Created on: Jan 30, 2022
 */
public class ElasticsearchToCsv {
    private static final ElasticsearchService ELASTICSEARCH_SERVICE = ElasticsearchService.getInstance();

    /**
     * A pojo which holds default search request size for elasticsearch index
     */
    private static class IndexMap {
        /**
         * Creates index map from given index name and size
         *
         * @param indexName a name of index
         * @param size a default size
         *
         * @return a newly created instance of index map
         */
        public static IndexMap of(String indexName, int size) {
            return new IndexMap(indexName, size);
        }

        private final String indexName;
        private final int size;

        /**
         * Creates a new instance of index map
         *
         * @param indexName a name of index to set
         * @param size a default size to set
         */
        private IndexMap(String indexName, int size) {
            this.indexName = indexName;
            this.size = size;
        }
    }

    /**
     * Entry point for the conversion of elasticsearch data to csv fromat
     *
     * @param args a command line arguments to use
     */
    public static void main(String[] args) {
        Stream.of(
            IndexMap.of("accelerometer", 1000),
            IndexMap.of("gyroscope", 1000),
            IndexMap.of("gsr", 20),
            IndexMap.of("heartrate", 5),
            IndexMap.of("ibi", 5),
            IndexMap.of("bvp", 1000),
            IndexMap.of("temperature", 1),
            IndexMap.of("rating", 1)
        ).forEach(ElasticsearchToCsv::searchAll);
    }

    /**
     * Performs search all elasticsearch operation for given index map
     *
     * @param indexMap an index map to perform
     */
    private static void searchAll(IndexMap indexMap) {
        var startTime = Instant.now();
        System.out.println();

        String indexName = indexMap.indexName;
        int size = indexMap.size;

        var totalElementSearch = SearchRequest.builder().withSize(1).withIndex(indexName).build();
        var totalElementSearchRequest = ELASTICSEARCH_SERVICE.search(totalElementSearch);

        if (totalElementSearchRequest.isEmpty()) {
            System.out.printf("Index %s doesn't exist%s", indexName, System.lineSeparator());
            return;
        }

        if (totalElementSearchRequest.get().getHits().getTotal().getValue() <= 0) {
            System.out.printf("Index %s has no document to search%s", indexName, System.lineSeparator());
            return;
        }

        final var progressBar = ProgressBar.builder()
            .withPrintStream(System.out)
            .withPrefix("Downloading: " + indexName)
            .withTotalElement(totalElementSearchRequest.get().getHits().getTotal().getValue())
            .withMaxStep(100)
            .build();

        final var csvWriter = CSVWriter.builder().withDelimiter(",").withFileName(indexName).build();

        var consumer = UncheckedIOConsumer.wrap((Response response) -> {
            var hits = response.getHits().getHits();
            progressBar.updateBy(hits.size());
            csvWriter.writeRows(hits.stream().map(SourceHits::getSource).collect(toList()));
            csvWriter.flush();
        });

        var onComplete = NoArgUncheckedIOConsumer.wrap(() -> {
            csvWriter.flush();
            csvWriter.close();
        });

        var searchRequest = SearchRequest.builder()
            .withSize(size)
            .withScroll("1m")
            .withIndex(indexName)
            .build();

        progressBar.initStartTime(startTime);
        ELASTICSEARCH_SERVICE.searchAll(searchRequest, consumer, onComplete);
    }
}
