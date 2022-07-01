/*
 * Copyright 2022 IDEAS Lab @ University of Toledo. All rights reserved.
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
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Service class as elasticsearch rest client
 *
 * @author Prakash Khadka <br>
 *     Created on: Jan 30, 2022
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

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new instance of {@link ElasticsearchService}
     *
     * @param httpClient a http client to set
     * @param objectMapper an object mapper to set
     */
    private ElasticsearchService(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Performs an elasticsearch search request for given request details <br>
     * <p>
     * api path : {@code  GET {indexName}/_search?size={size}&scroll={scrollTime}}
     *
     * @param searchRequest an elasticsearch search request to use
     *
     * @return an optional elasticsearch response
     */
    public Optional<Response> search(SearchRequest searchRequest) {
        LOGGER.debug("Performing elasticsearch search request {}", searchRequest);
        if (searchRequest == null || searchRequest.getIndex() == null) {
            return Optional.empty();
        }
        String apiPath = String.format("%s/_search?size=%d&scroll=%s", searchRequest.getIndex(), searchRequest.getSize(), searchRequest.getScroll());
        return send(httpRequest(HTTP_METHOD_GET, null, apiPath), searchRequest);
    }

    /**
     * Performs an elasticsearch scroll request for given scroll request details <br>
     * <p>
     * api path : {@code  GET _search/scroll?scroll={scrollValue}} with request body {"_scroll_id": ""}
     *
     * @param scrollRequest a scroll request to use
     *
     * @return an optional elasticsearch response
     */
    public Optional<Response> scroll(ScrollRequest scrollRequest) {
        LOGGER.debug("Performing elasticsearch scroll request {}", scrollRequest);
        if (scrollRequest == null || scrollRequest.getScrollId() == null) {
            return Optional.empty();
        }

        var apiPath = String.format("_search/scroll?scroll=%s", scrollRequest.getScroll());
        var scroll = new Scroll();
        scroll.setScrollId(scrollRequest.getScrollId());
        return send(httpRequest(HTTP_METHOD_GET, scroll, apiPath), scrollRequest);
    }

    /**
     * Performs create index api request for given elasticsearch create request details <br>
     * api path : {@code  POST _create/{indexName}/{uniqueDocumentId}} with request body {"connectionId": ""}
     *
     * @param createRequest a create request to use
     */
    public void create(CreateRequest<? extends IndexBody> createRequest) {
        LOGGER.debug("Performing elasticsearch create document request {}", createRequest);
        checkRequest(createRequest);
        var apiPath = String.format("%s/_create/%s", createRequest.getIndex(), createRequest.getBody().getId());
        send(httpRequest(HTTP_METHOD_POST, createRequest.getBody(), apiPath), createRequest);
    }

    /**
     * Performs elasticsearch document delete api operation for given delete request <br>
     * api path : {@code  DELETE {indexName}/_doc/{uniqueDocumentId}}
     *
     * @param deleteRequest a delete request to set
     */
    public void delete(DeleteRequest<? extends IndexBody> deleteRequest) {
        LOGGER.debug("Performing elasticsearch delete document request {}", deleteRequest);
        checkRequest(deleteRequest);
        var apiPath = String.format("%s/_doc/%s", deleteRequest.getIndex(), deleteRequest.getBody().getId());
        send(httpRequest(HTTP_METHOD_DELETE, null, apiPath), deleteRequest);
    }

    /**
     * Search all the elasticsearch documents in the index based on given search request.
     * First of all, it will perform basic elasticsearch search request to find the total document count
     * in the index, then it will perform scroll request based the scroll id given by search request.
     * Based on the size documents to be retrieved for each scroll request until it reaches the total document
     * count, each successful scroll request will invoke responseConsumer. Once all the scroll request
     * is completed then given onComplete consumer will be invoked to finalize the search all documents operation.
     *
     * @param searchRequest an elasticsearch search request to use
     * @param responseConsumer a response consumer to be invoked for each scroll request completion operation
     * @param onComplete an on complete consumer to be invoked to finalize search all operation
     */
    public void searchAll(SearchRequest searchRequest, Consumer<Response> responseConsumer, NoArgConsumer onComplete) {
        if (searchRequest.getScroll() == null) {
            return;
        }

        var searchResponse = search(searchRequest);
        if (searchResponse.isEmpty()) {
            return;
        }

        var totalCount = searchResponse.get().getHits().getTotal().getValue();
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

        do {
            var response = searchResponse.get();
            count += response.getHits().getHits().size();

            if (responseConsumer != null) {
                responseConsumer.accept(response);
            }

            scrollRequest.setScrollId(response.getScrollId());
            searchResponse = scroll(scrollRequest);
        } while(searchResponse.isPresent() && count != totalCount);

        Optional.ofNullable(onComplete).ifPresent(NoArgConsumer::accept);
    }

    /**
     * Checks the validity of this given request
     *
     * @param request a request to be validated
     *
     * @throws IllegalArgumentException if any of the request, index, body or bodyId is null
     */
    private void checkRequest(IndexableBodyRequest<? extends IndexBody> request) {
        if (request == null || request.getIndex() == null || request.getBody() == null || request.getBody().getId() == null) {
            throw new IllegalArgumentException("Invalid request");
        }
    }

    /**
     * Send the given http request using underlying http client.
     * This will not throw any exception rather it will catch any underlying exception and notify
     * the sender by using exception consumer.
     * <p>
     * If http client response is bad then it will notify the sender by using error consumer. If http client
     * response doesn't have any body but the request is successful, it will return empty elasticsearch response.
     * For any successful request it will use success consumer to notify sender that underlying http call was a success
     *
     * @param httpRequest an http request to send
     * @param elasticsearchRequest an elasticsearch request to use to perform completion of http request
     * @param <T> a type of elasticsearch request
     *
     * @return an optional elasticsearch response
     */
    private <T extends Request> Optional<Response> send(HttpRequest httpRequest, T elasticsearchRequest) {
        try {
            var response = httpClient.send(httpRequest, BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                LOGGER.error("Request with {} failed due to status code {}", elasticsearchRequest, response.statusCode());

                var errorConsumer = elasticsearchRequest.getErrorConsumer();

                if (errorConsumer != null) {
                    errorConsumer.accept(response);
                }
                return Optional.empty();
            }

            var successConsumer = elasticsearchRequest.getSuccessConsumer();
            if (successConsumer != null) {
                successConsumer.accept(response);
            }

            LOGGER.debug("Successfully processed request {} with status code {}", elasticsearchRequest, response.statusCode());

            if (response.body() != null) {
                return Optional.of(objectMapper.readValue(response.body(), Response.class));
            }

            return Optional.empty();

        }
        catch (IOException | InterruptedException exception) {
            LOGGER.error("Unable to perform request for {} because of {}", elasticsearchRequest, exception);

            var exceptionConsumer = elasticsearchRequest.getExceptionConsumer();
            if (exceptionConsumer != null) {
                exceptionConsumer.accept(exception);
            }

            return Optional.empty();
        }
    }

    /**
     * Builds the http request for given method, body, and api path.
     *
     * @param method a http method to set
     * @param body a request body to set
     * @param apiPath an api path to use
     * @param <T> a type of request body
     *
     * @return a newly created http request
     */
    private <T> HttpRequest httpRequest(String method, T body, String apiPath) {
        try {
            return httpRequest(method, objectMapper.writeValueAsString(body), apiPath);
        }
        catch (JsonProcessingException ignored) {
            return httpRequest(method, null, apiPath);
        }
    }

    /**
     * Builds a http request from given http method, json body and api path.
     * The base url is get from environment variable. All authentication header
     * and content type header are also set here.
     *
     * @param method a http method to set
     * @param jsonBody a string json body to set
     * @param apiPath an api path to be appended to base url
     *
     * @return a newly created http request
     */
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
