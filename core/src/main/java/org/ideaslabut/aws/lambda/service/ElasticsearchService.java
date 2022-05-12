package org.ideaslabut.aws.lambda.service;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ideaslabut.aws.lambda.domain.elasticsearch.IndexBody;
import org.ideaslabut.aws.lambda.domain.elasticsearch.Response;
import org.ideaslabut.aws.lambda.domain.elasticsearch.Scroll;
import org.ideaslabut.aws.lambda.domain.elasticsearch.request.CreateRequest;
import org.ideaslabut.aws.lambda.domain.elasticsearch.request.DeleteRequest;
import org.ideaslabut.aws.lambda.domain.elasticsearch.request.Request;
import org.ideaslabut.aws.lambda.domain.elasticsearch.request.ScrollRequest;
import org.ideaslabut.aws.lambda.domain.elasticsearch.request.SearchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.Objects;
import java.util.Optional;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.function.Consumer;

public class ElasticsearchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchService.class);

    private static final String ELASTICSEARCH_URL = "ELASTICSEARCH_URL";
    private static final String ELASTICSEARCH_AUTHENTICATION_KEY = "ELASTICSEARCH_AUTHENTICATION_KEY";
    private static final String CONTENT_TYPE_JSON = "application/json";

    private static final String HTTP_METHOD_GET = "GET";
    private static final String HTTP_METHOD_POST = "POST";
    private static final String HTTP_METHOD_DELETE = "DELETE";

    private static ElasticsearchService INSTANCE = null;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

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

    private static ElasticsearchService buildInstance() {
        var httpClient = HttpClient.newHttpClient();
        var objectMapper = new ObjectMapper().configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        return new ElasticsearchService(httpClient, objectMapper);
    }

    private ElasticsearchService(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = Objects.requireNonNull(httpClient);
        this.objectMapper = objectMapper;
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

    public <T extends IndexBody> void create(CreateRequest<T> createRequest) {
        checkRequest(createRequest);
        var apiPath = String.format("%s/_create/%s", createRequest.getIndex(), createRequest.getBody().getId());
        send(httpRequest(HTTP_METHOD_POST, createRequest.getBody(), apiPath), createRequest);
    }

    public <T extends IndexBody> void delete(DeleteRequest<T> deleteRequest) {
        checkRequest(deleteRequest);
        var apiPath = String.format("%s/_doc/%s", deleteRequest.getIndex(), deleteRequest.getBody().getId());
        send(httpRequest(HTTP_METHOD_DELETE, null, apiPath), deleteRequest);
    }


    private void checkRequest(CreateRequest request) {
        if (request == null || request.getIndex() == null || request.getBody() == null || request.getBody().getId() == null) {
            throw new IllegalArgumentException("Invalid create request");
        }
    }

    public void searchAll(SearchRequest searchRequest, Consumer<Response> responseConsumer, Consumer<Void> onComplete) {
        if (searchRequest.getScroll() == null) {
            return;
        }

        var search = search(searchRequest);
        if (search.isEmpty()) {
            return;
        }

        var response = search.get();
        var totalCount = response.getHits().getTotal().getValue();
        if(totalCount == 0L) {
            return;
        }

        var scrollRequest = ScrollRequest.newBuilder()
                .withScroll(searchRequest.getScroll())
                .onException(searchRequest.getExceptionConsumer())
                .onHttpError(searchRequest.getErrorConsumer())
                .onHttpSuccess(searchRequest.getSuccessConsumer())
                .withIndex(searchRequest.getIndex())
                .build();
        long count = 0;

        while(count != totalCount) {
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
        if (onComplete != null) {
            onComplete.accept(null);
        }
    }

    private <T extends Request> Optional<Response> send(HttpRequest httpRequest, T searchRequest) {
        try {
            var response = httpClient.send(httpRequest, BodyHandlers.ofString());
            if (response.statusCode() >=  400) {
                Optional.ofNullable(searchRequest.getErrorConsumer()).ifPresent(consumer -> consumer.accept(response));
                return Optional.empty();
            }

            Optional.ofNullable(searchRequest.getSuccessConsumer()).ifPresent(consumer -> consumer.accept(response));

            if (response.statusCode() != 200) {
                return Optional.empty();
            }

            if (response.body() != null) {
                return Optional.of(objectMapper.readValue(response.body(), Response.class));
            }

            return Optional.empty();

        } catch (IOException  | InterruptedException exception) {
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

        LOGGER.info("Url {}", url);

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