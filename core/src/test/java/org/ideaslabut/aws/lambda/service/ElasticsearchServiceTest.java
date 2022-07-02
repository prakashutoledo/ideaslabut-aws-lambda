/*
 * Copyright 2022 IDEAS Lab @ University of Toledo. All rights reserved.
 */
package org.ideaslabut.aws.lambda.service;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import org.ideaslabut.aws.lambda.domain.elasticsearch.Response;
import org.ideaslabut.aws.lambda.domain.elasticsearch.request.SearchRequest;
import org.ideaslabut.aws.lambda.domain.sneaky.NoArgConsumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Unit test for {@link ElasticsearchService}
 *
 * @author Prakash Khadka <br>
 *     Created On: Jun 27, 2022
 */
class ElasticsearchServiceTest {
    private final AtomicInteger counter = new AtomicInteger(0);
    @Spy
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private HttpClient httpClient;
    @InjectMocks
    private ElasticsearchService elasticsearchService;
    @Captor
    private ArgumentCaptor<HttpRequest> httpRequestArgumentCaptor;
    @Mock
    private HttpResponse<String> httpResponse;

    @BeforeEach
    void setup() {
        openMocks(this);
    }

    @AfterEach
    void teardown() {
        counter.set(0);
    }

    @Test
    void getInstance() {
        assertNotNull(ElasticsearchService.getInstance(), "Singleton instance should be created");
    }

    @Test
    void nullRequest() {
        assertTrue(elasticsearchService.search(null).isEmpty(), "Search request is null");
        assertTrue(elasticsearchService.scroll(null).isEmpty(), "Search request is null");

        assertThrows(IllegalArgumentException.class, () -> elasticsearchService.create(null));
        assertThrows(IllegalArgumentException.class, () -> elasticsearchService.delete(null));
    }

    @Test
    void checkExceptions() throws IOException, InterruptedException {
        final List<Exception> exceptions = new ArrayList<>();
        Consumer<Exception> exceptionConsumer = exceptions::add;

        var searchRequest = SearchRequest.builder()
            .withIndex("fake-index")
            .withSize(1)
            .onException(exceptionConsumer)
            .build();

        mockThrowExceptions();

        var response = elasticsearchService.search(searchRequest);
        assertTrue(response.isEmpty(), "Response is empty");

        searchRequest.setSize(121);
        response = elasticsearchService.search(searchRequest);

        assertTrue(response.isEmpty(), "Second response");

        verifyCheckedExceptions(exceptions);

        verify(httpClient, times(2)).send(httpRequestArgumentCaptor.capture(), any());
        verifyRequest(
            httpRequestMatcher("GET", "fake-index/_search?size=1&scroll=1m"),
            httpRequestMatcher("GET", "fake-index/_search?size=121&scroll=1m")
        );
    }

    @Test
    void searchWithError() throws IOException, InterruptedException {
        mockError();

        var searchRequest = SearchRequest.
            builder()
            .withIndex("some-index")
            .withScroll("2d")
            .onHttpError(this::errorConsumer)
            .build();

        verifyWithError(
            () -> elasticsearchService.search(searchRequest),
            httpRequestMatcher("GET", "some-index/_search?size=10&scroll=2d")
        );
    }

    @SuppressWarnings("unchecked")
    private void mockThrowExceptions() throws IOException, InterruptedException {
        when(httpClient.send(any(), any()))
            .thenThrow(IOException.class, InterruptedException.class);
    }

    private void mockError() throws IOException, InterruptedException {
        when(httpResponse.statusCode()).thenReturn(400);
        when(httpClient.send(any(), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(httpResponse);
    }

    private void verifyCheckedExceptions(List<Exception> exceptions) {
        assertThat(
            exceptions,
            allOf(
                is(notNullValue()),
                is(not(empty())),
                hasSize(is(equalTo(2))),
                containsInAnyOrder(is(instanceOf(InterruptedException.class)), is(instanceOf(IOException.class)))
            )
        );
    }

    @SafeVarargs
    private void verifyWithError(NoArgConsumer consumer, Matcher<HttpRequest>... matchers) throws IOException, InterruptedException {
        consumer.accept();
        verify(httpClient).send(
            httpRequestArgumentCaptor.capture(),
            ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()
        );
        assertEquals(1, counter.intValue(), "Error consumer is invoked once");
        verifyRequest(matchers);
    }

    @SafeVarargs
    private void verifyWithError(final Supplier<Optional<Response>> supplier, Matcher<HttpRequest>... matchers) throws IOException, InterruptedException {
        verifyWithError(
            () -> {
                var response = supplier.get();
                assertTrue(response.isEmpty(), "Response should be empty");
            },
            matchers
        );
    }

    private void errorConsumer(HttpResponse<String> response) {
        counter.set(counter.get() + 1);
        assertEquals(httpResponse, response, "Response should match");
    }

    @SafeVarargs
    private void verifyRequest(Matcher<HttpRequest>... matchers) {
        var actualRequests = httpRequestArgumentCaptor.getAllValues();
        assertThat(
            actualRequests,
            allOf(
                is(notNullValue()),
                iterableWithSize(is(equalTo(matchers.length))),
                allOf(stream(matchers).map(Matchers::hasItem).collect(toList()))
            )
        );
    }

    private Matcher<HttpRequest> httpRequestMatcher(String method, String apiPath) {
        return allOf(httpMethod(method), withUri(apiPath), knownHeaders());
    }

    private Matcher<HttpRequest> httpMethod(String method) {
        return is(withHttpMethod(is(equalTo("GET"))));
    }

    private Matcher<HttpRequest> withUri(String apiPath) {
        return is(withUri(equalTo("https://fake-url/" + apiPath)));
    }

    private Matcher<HttpRequest> knownHeaders() {
        return is(withHeaders(
            allOf(
                hasEntry(is(equalTo("Content-Type")), is(equalTo(List.of("application/json")))),
                hasEntry(is(equalTo("Authorization")), is(equalTo(List.of("Basic abcde"))))
            )
        ));
    }

    private Matcher<HttpRequest> withHeaders(Matcher<Map<String, List<String>>> matcher) {
        return new FeatureMatcher<>(matcher, "Http headers", "headers") {
            @Override
            protected Map<String, List<String>> featureValueOf(HttpRequest actual) {
                return actual.headers().map();
            }
        };
    }

    private Matcher<HttpRequest> withHttpMethod(Matcher<String> methodMatcher) {
        return new FeatureMatcher<>(methodMatcher, "Http method", "method") {
            @Override
            protected String featureValueOf(HttpRequest actual) {
                return actual.method();
            }
        };
    }

    private Matcher<HttpRequest> withUri(Matcher<String> methodMatcher) {
        return new FeatureMatcher<>(methodMatcher, "URI", "uri") {
            @Override
            protected String featureValueOf(HttpRequest actual) {
                return actual.uri().toString();
            }
        };
    }
}
