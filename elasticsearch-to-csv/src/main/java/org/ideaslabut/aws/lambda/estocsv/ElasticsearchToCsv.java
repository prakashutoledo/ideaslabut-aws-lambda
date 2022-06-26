package org.ideaslabut.aws.lambda.estocsv;

import org.ideaslabut.aws.lambda.domain.elasticsearch.Response;
import org.ideaslabut.aws.lambda.domain.elasticsearch.SourceHits;
import org.ideaslabut.aws.lambda.domain.elasticsearch.request.SearchRequest;
import org.ideaslabut.aws.lambda.extractor.util.CSVWriter;
import org.ideaslabut.aws.lambda.extractor.util.ProgressBar;
import org.ideaslabut.aws.lambda.service.ElasticsearchService;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ElasticsearchToCsv {
    private static final ElasticsearchService ELASTICSEARCH_SERVICE = ElasticsearchService.getInstance();
    
    public static void main(String[] args) {
        String indexName = "accelerometer";
        searchAll("accelerometer", 1000);
        searchAll("gyroscope", 1000);
        searchAll("gsr", 20);
        searchAll("heartrate", 5);
        searchAll("ibi", 5);
        searchAll("temperature", 1);
    }
    
    private static void searchAll(String indexName, int size) {
        long startTime = System.currentTimeMillis();
        var totalElementSearch = SearchRequest.builder().withSize(1).withIndex(indexName).build();
        var totalElementSearchRequest = ElasticsearchService.getInstance().search(totalElementSearch);
        if (totalElementSearchRequest.isEmpty() || totalElementSearchRequest.get().getHits().getTotal().getValue() <= 0) {
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

        Consumer<Response> consumer = response -> {
            progressBar.updateBy(response.getHits().getHits().size());
            csvWriter.writeProperties(response.getHits().getHits().stream().map(SourceHits::getSource).collect(Collectors.toList()));
            try {
                csvWriter.flush();
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
        };

        Consumer<Void> onComplete = ignored -> {
            try {
                csvWriter.flush();
                csvWriter.close();
                System.out.println();
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
        };

        progressBar.initStartTime();
        var searchRequest = SearchRequest.builder()
                .withSize(size)
                .withScroll("1m")
                .withIndex(indexName)
                .build();

        ELASTICSEARCH_SERVICE.searchAll(searchRequest, consumer, onComplete);   
    }
}
