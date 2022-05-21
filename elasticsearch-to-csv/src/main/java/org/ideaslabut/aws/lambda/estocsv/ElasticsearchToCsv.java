package org.ideaslabut.aws.lambda.estocsv;

import org.ideaslabut.aws.lambda.domain.elasticsearch.Response;
import org.ideaslabut.aws.lambda.domain.elasticsearch.SourceHits;
import org.ideaslabut.aws.lambda.domain.elasticsearch.request.SearchRequest;
import org.ideaslabut.aws.lambda.extractor.util.CSVWriter;
import org.ideaslabut.aws.lambda.extractor.util.ProgressBar;
import org.ideaslabut.aws.lambda.service.ElasticsearchService;
import org.ideaslabut.aws.lambda.service.WebSocketService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ElasticsearchToCsv {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchToCsv.class);

    public static void main(String... main) {
        long startTime = System.currentTimeMillis();
        LOGGER.info("Response event {}", WebSocketService.getInstance().processEvent(null));
        var totalElementSearch = SearchRequest.builder().withSize(1).withIndex("socket").build();
        var totalElementSearchRequest = ElasticsearchService.getInstance().search(totalElementSearch);
        if (totalElementSearchRequest.isEmpty() || totalElementSearchRequest.get().getHits().getTotal().getValue() <= 0) {
            return;
        }

        final var progressBar = ProgressBar.builder()
                .withPrintStream(System.out)
                .withDelimiter("#")
                .withPrefix("Downloading")
                .withTotalElement(totalElementSearchRequest.get().getHits().getTotal().getValue())
                .withMaxStep(100).build();

        progressBar.initStartTime(startTime);

        final var csvWriter = CSVWriter.builder().withDelimiter(",").withFileName("socket").build();

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
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
        };

        progressBar.initStartTime();
        var searchRequest = SearchRequest.builder()
                .withSize(20)
                .withScroll("1m")
                .withIndex("socket")
                .build();

        ElasticsearchService.getInstance().searchAll(searchRequest, consumer, onComplete);
    }
}
