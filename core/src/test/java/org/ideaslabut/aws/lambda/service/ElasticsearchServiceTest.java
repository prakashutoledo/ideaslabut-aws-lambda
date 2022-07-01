/*
 * Copyright 2022 IDEAS Lab @ University of Toledo. All rights reserved.
 */
package org.ideaslabut.aws.lambda.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import org.ideaslabut.aws.lambda.domain.elasticsearch.request.SearchRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

/**
 * Unit test for {@link ElasticsearchService}
 *
 * @author Prakash Khadka <br>
 *     Created On: Jun 27, 2022
 */
class ElasticsearchServiceTest {
    @Mock
    private HttpClient httpClient;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ElasticsearchService elasticsearchService;

    @Captor
    private ArgumentCaptor<HttpRequest> httpRequestArgumentCaptor;

    @Captor
    private ArgumentCaptor<HttpResponse.BodyHandler<String>> bodyHandlerArgumentCaptor;

    @BeforeEach
    void setup() {
        openMocks(this);
    }

    @Test
    void getInstance() {
        assertNotNull(ElasticsearchService.getInstance(), "Singleton instance should be created");
    }

    @Test
    void emptySearch() {
        assertTrue(elasticsearchService.search(null).isEmpty(), "Search request is null");
    }

    @Test
    void searchWithError() throws IOException, InterruptedException {
        var searchRequest = SearchRequest.builder().withIndex("fake-index").withSize(1).build();
        when(httpClient.send(any(), any()))
            .thenThrow(IOException.class);
        var response = elasticsearchService.search(searchRequest);
        assertTrue(response.isEmpty(), "Response is empty");

        searchRequest.setSize(121);
        response = elasticsearchService.search(searchRequest);
        assertTrue(response.isEmpty(), "Second response");

        verify(httpClient, times(2)).send(httpRequestArgumentCaptor.capture(), any());
    }t 
}
