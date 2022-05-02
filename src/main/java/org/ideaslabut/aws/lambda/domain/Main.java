package org.ideaslabut.aws.lambda.domain;

import org.ideaslabut.aws.lambda.domain.elasticsearch.Response;
import org.ideaslabut.aws.lambda.domain.elasticsearch.request.SearchRequest;
import org.ideaslabut.aws.lambda.domain.elasticsearch.SourceHits;
import org.ideaslabut.aws.lambda.service.CSVWriter;
import org.ideaslabut.aws.lambda.service.ElasticsearchService;
import org.ideaslabut.aws.lambda.service.ProgressBar;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.http.HttpResponse;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Main {

    public static void main(String... main) throws Exception {
        long startTime = System.currentTimeMillis();
        var totalElementSearch =  SearchRequest.newBuilder().withSize(1).withIndex("socket").build();
        var totalElementSearchRequest = ElasticsearchService.getInstance().search(totalElementSearch);
        if (totalElementSearchRequest.isEmpty() || totalElementSearchRequest.get().getHits().getTotal().getValue() <= 0) {
            return;
        }

        final var progressBar = ProgressBar.builder()
                .withPrintStream(System.out)
                .withDelimiter("#").withPrefix("Downloading")
                .withTotalElement(totalElementSearchRequest.get().getHits().getTotal().getValue())
                .withMaxStep(100).build();

        progressBar.initStartTime(startTime);

        final var csvWriter = CSVWriter.newBuilder().withDelimiter(",").withFileName("socket").build();

        Consumer<Response> consumer = response ->  {
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
        var searchRequest = SearchRequest.newBuilder()
                .withSize(20)
                .withScroll("1m")
                .withIndex("socket")
                .build();

        ElasticsearchService.getInstance().searchAll(searchRequest, consumer, onComplete);
    }

    public static void exception(Exception exception) {
        exception.printStackTrace();
    }

    public static void error(HttpResponse<String> response) {
        System.out.println(response.statusCode());
        System.out.println(response.body());
    }
}
