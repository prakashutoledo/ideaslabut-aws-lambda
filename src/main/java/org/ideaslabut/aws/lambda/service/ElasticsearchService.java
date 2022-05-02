package org.ideaslabut.aws.lambda.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ideaslabut.aws.lambda.domain.elasticsearch.*;
import org.ideaslabut.aws.lambda.domain.elasticsearch.request.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.function.Consumer;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

public class ElasticsearchService {
    private static final String ELASTICSEARCH_URL = "elasticsearch.url";
    private static final String ELASTICSEARCH_AUTHENTICATION_KEY = "elasticsearch.authenticationKey";
    private static final String CONTENT_TYPE_JSON = "application/json";

    private static final String HTTP_METHOD_GET = "GET";
    private static final String HTTP_METHOD_POST = "POST";
    private static final String HTTP_METHOD_DELETE = "DELETE";

    private static ElasticsearchService INSTANCE = null;
    private final HttpClient httpClient;
    private final ApplicationPropertiesService applicationPropertiesService;
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
        var applicationPropertiesService = ApplicationPropertiesService.getInstance();
        var objectMapper = new ObjectMapper().configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        return new ElasticsearchService(httpClient, applicationPropertiesService, objectMapper);
    }

    private ElasticsearchService(HttpClient httpClient, ApplicationPropertiesService applicationPropertiesService, ObjectMapper objectMapper) {
        this.httpClient = Objects.requireNonNull(httpClient);
        this.applicationPropertiesService = applicationPropertiesService;
        this.objectMapper = objectMapper;
    }

    public Optional<Response> search(SearchRequest searchRequest) {
        if (searchRequest == null || searchRequest.getIndex() == null) {
            Optional.empty();
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

    public Optional<Response> create(CreateRequest createRequest) {
        if (createRequest == null || createRequest.getIndex() == null || createRequest.getBody() == null || createRequest.getBody().getId() == null) {
            return Optional.empty();
        }

        var apiPath = String.format("%s/_create/%s", createRequest.getIndex(), createRequest.getBody().getId());
        return send(httpRequest(HTTP_METHOD_POST, createRequest.getBody(), apiPath), createRequest);
    }

    public Optional<Response> delete(DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getIndex() == null || deleteRequest.getBody() == null || deleteRequest.getBody().getId() == null) {
            return Optional.empty();
        }

        var apiPath = String.format("%s/_doc/%s", deleteRequest.getIndex(), deleteRequest.getBody().getId());
        return send(httpRequest(HTTP_METHOD_DELETE, null, apiPath), deleteRequest);
    }

    public void searchAll(SearchRequest searchRequest, Consumer<Response> responseConsumer, Consumer<Void> onComplete) {
        var search = search(searchRequest);
        if (search.isEmpty()) {
            return;
        }

        var response = search.get();

        if(response.getHits().getTotal().getValue() == 0L) {
            return;
        }

        var scrollRequest = ScrollRequest.newBuilder()
                .withScroll(searchRequest.getScroll())
                .withIndex(searchRequest.getIndex())
                .build();

        while(!response.getHits().getHits().isEmpty()) {
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
        String url = applicationPropertiesService.getProperty(ELASTICSEARCH_URL);

        if (apiPath != null && !apiPath.isEmpty()) {
            url = String.format("%s/%s", url, apiPath);
        }

        HttpRequest.BodyPublisher bodyPublisher = Optional.ofNullable(jsonBody)
                .map(body -> HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .orElseGet(HttpRequest.BodyPublishers::noBody);
        return HttpRequest.newBuilder().method(method, bodyPublisher)
                .uri(URI.create(url))
                .setHeader("Authorization", String.format("Basic %s", applicationPropertiesService.getProperty(ELASTICSEARCH_AUTHENTICATION_KEY)))
                .setHeader("Content-Type", CONTENT_TYPE_JSON)
                .build();
    }
}
