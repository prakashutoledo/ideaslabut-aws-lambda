/*
 * Copyright 2022 IDEAS Lab @ UT. All rights reserved.
 */
package org.ideaslabut.aws.lambda.service;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

import org.ideaslabut.aws.lambda.domain.elasticsearch.IndexBody;
import org.ideaslabut.aws.lambda.domain.elasticsearch.Response;
import org.ideaslabut.aws.lambda.domain.elasticsearch.Scroll;
import org.ideaslabut.aws.lambda.domain.elasticsearch.request.CreateRequest;
import org.ideaslabut.aws.lambda.domain.elasticsearch.request.DeleteRequest;
import org.ideaslabut.aws.lambda.domain.elasticsearch.request.IndexableBodyRequest;
import org.ideaslabut.aws.lambda.domain.elasticsearch.request.Request;
import org.ideaslabut.aws.lambda.domain.elasticsearch.request.ScrollRequest;
import org.ideaslabut.aws.lambda.domain.elasticsearch.request.SearchRequest;
import org.ideaslabut.aws.lambda.domain.sneaky.NoArgConsumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Service class as elasticsearch client
 *
 * @author Prakash Khadka <br>
 *         Created on: Jan 30, 2022
 */
public class ElasticsearchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchService.class);

    private static final String ELASTICSEARCH_URL = "ELASTICSEARCH_URL";
    private static final String ELASTICSEARCH_AUTHENTICATION_KEY = "ELASTICSEARCH_AUTHENTICATION_KEY";
    private static final String CONTENT_TYPE_JSON = "application/json";

    private static final String HTTP_METHOD_GET = "GET";
    private static final String HTTP_METHOD_POST = "POST";
    private static final String HTTP_METHOD_DELETE = "DELETE";

    private static volatile ElasticsearchService INSTANCE = null;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new instance of {@link ElasticsearchService}
     *
     * @param httpClient a http client to set
     * @param objectMapper an object mapper to set
     */
    private ElasticsearchService(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = Objects.requireNonNull(httpClient);
        this.objectMapper = objectMapper;
    }

    /**
     * Gets the thread safe singleton instance of {@link ElasticsearchService}
     *
     * @return a thread safe singleton instance of elasticsearch service
     */
    public static ElasticsearchService getInstance() {
        if (INSTANCE == null) {
            synchronized (ElasticsearchService.class) {
                if (INSTANCE == null) {
                    INSTANCE = buildInstance();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Creates a new instance of {@link ElasticsearchService}
     *
     * @return a newly created elasticsearch service
     */
    private static ElasticsearchService buildInstance() {
        var httpClient = HttpClient.newHttpClient();
        var objectMapper = new ObjectMapper().configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        return new ElasticsearchService(httpClient, objectMapper);
    }

    public Optional<Response> search(SearchRequest searchRequest) {
        LOGGER.info("Search elasticsearch for request {}", searchRequest);
        if (searchRequest == null || searchRequest.getIndex() == null) {
            return Optional.empty();
        }
        String apiPath = String.format("%s/_search?size=%d&scroll=%s", searchRequest.getIndex(), searchRequest.getSize(), searchRequest.getScroll());
        return send(httpRequest(HTTP_METHOD_GET, null, apiPath), searchRequest);
    }

    public Optional<Response> scroll(ScrollRequest scrollRequest) {
        if (scrollRequest == null || scrollRequest.getScrollId() == null) {
            return Optional.empty();
        }

        var apiPath = String.format("_search/scroll?scroll=%s", scrollRequest.getScroll());
        var scroll = new Scroll();
        scroll.setScrollId(scrollRequest.getScrollId());
        return send(httpRequest(HTTP_METHOD_GET, scroll, apiPath), scrollRequest);
    }

    public void create(CreateRequest<? extends IndexBody> createRequest) {
        checkRequest(createRequest);
        var apiPath = String.format("%s/_create/%s", createRequest.getIndex(), createRequest.getBody().getId());
        send(httpRequest(HTTP_METHOD_POST, createRequest.getBody(), apiPath), createRequest);
    }

    public void delete(DeleteRequest<? extends IndexBody> deleteRequest) {
        checkRequest(deleteRequest);
        var apiPath = String.format("%s/_doc/%s", deleteRequest.getIndex(), deleteRequest.getBody().getId());
        send(httpRequest(HTTP_METHOD_DELETE, null, apiPath), deleteRequest);
    }


    private void checkRequest(IndexableBodyRequest<? extends IndexBody> request) {
        if (request == null || request.getIndex() == null || request.getBody() == null || request.getBody().getId() == null) {
            throw new IllegalArgumentException("Invalid request");
        }
    }

    public void searchAll(SearchRequest searchRequest, Consumer<Response> responseConsumer, NoArgConsumer onComplete) {
        if (searchRequest.getScroll() == null) {
            return;
        }

        var search = search(searchRequest);
        if (search.isEmpty()) {
            return;
        }

        var response = search.get();
        var totalCount = response.getHits().getTotal().getValue();
        if (totalCount == 0L) {
            return;
        }

        var count = 0L;
        var scrollRequest = ScrollRequest.builder()
                .withScroll(searchRequest.getScroll())
                .onException(searchRequest.getExceptionConsumer())
                .onHttpError(searchRequest.getErrorConsumer())
                .onHttpSuccess(searchRequest.getSuccessConsumer())
                .withIndex(searchRequest.getIndex())
                .build();

        while (count != totalCount) {
            count += response.getHits().getHits().size();

            if (responseConsumer != null) {
                responseConsumer.accept(response);
            }

            scrollRequest.setScrollId(response.getScrollId());
            search = scroll(scrollRequest);
            if (search.isEmpty()) {
                break;
            }
            response = search.get();
        }

        Optional.ofNullable(onComplete).ifPresent(NoArgConsumer::accept);
    }

    private <T extends Request> Optional<Response> send(HttpRequest httpRequest, T searchRequest) {
        try {
            var response = httpClient.send(httpRequest, BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                LOGGER.debug("Request with {} failed due to status code {}", searchRequest, response.statusCode());
                Optional.ofNullable(searchRequest.getErrorConsumer()).ifPresent(consumer -> consumer.accept(response));
                return Optional.empty();
            }

            Optional.ofNullable(searchRequest.getSuccessConsumer()).ifPresent(consumer -> consumer.accept(response));

            LOGGER.info("Successfully processed request {} with status code {}", searchRequest, response.statusCode());

            if (response.body() != null) {
                return Optional.of(objectMapper.readValue(response.body(), Response.class));
            }

            return Optional.empty();

        } catch (IOException | InterruptedException exception) {
            LOGGER.debug("Unable to perform request for {}", searchRequest);
            Optional.ofNullable(searchRequest.getExceptionConsumer()).ifPresent(consumer -> consumer.accept(exception));
            return Optional.empty();
        }
    }

    private <T> HttpRequest httpRequest(String method, T body, String apiPath) {
        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException ignored) {
            jsonBody = null;
        }
        return httpRequest(method, jsonBody, apiPath);
    }

    private HttpRequest httpRequest(String method, String jsonBody, String apiPath) {
        var url = System.getenv(ELASTICSEARCH_URL);
        if (apiPath != null && !apiPath.isEmpty()) {
            url = String.format("%s/%s", url, apiPath);
        }

        var bodyPublisher = Optional.ofNullable(jsonBody)
                .map(BodyPublishers::ofString)
                .orElseGet(BodyPublishers::noBody);
        return HttpRequest.newBuilder().method(method, bodyPublisher)
                .uri(URI.create(url))
                .setHeader("Authorization", String.format("Basic %s", System.getenv(ELASTICSEARCH_AUTHENTICATION_KEY)))
                .setHeader("Content-Type", CONTENT_TYPE_JSON)
                .build();
    }
}
