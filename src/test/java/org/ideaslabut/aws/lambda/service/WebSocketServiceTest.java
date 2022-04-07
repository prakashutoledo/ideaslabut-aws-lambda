package org.ideaslabut.aws.lambda.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.MockitoAnnotations.openMocks;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import okio.Buffer;
import org.ideaslabut.aws.lambda.domain.*;
import org.ideaslabut.aws.lambda.domain.WebSocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.PostToConnectionRequest;

/**
 * Unit test for WebSocketService
 *
 * @author Prakash Khadka <br>
 *         Created on: Jan 30, 2022
 */
public class WebSocketServiceTest {
    private String fakeURL = "http://base-elasticsearch-url.com";

    @Spy
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Spy
    private final Properties properties = new Properties();

    @Mock
    private ApiGatewayManagementApiClient apiGatewayManagementApiClient;

    @Mock
    private OkHttpClient okHttpClient;

    @Captor
    private ArgumentCaptor<Request> requestArgumentCaptor;

    @Mock
    private Call remoteCall;

    @InjectMocks
    private WebSocketService webSocketService;

    @BeforeEach
    public void initMocks() {
        openMocks(this);
        properties.setProperty("elasticsearch.url", fakeURL);
        properties.setProperty("elasticsearch.authenticationKey", "fake-user:fake-password");
    }

    @Test
    void processNullEvent() {
        verifyProxyResponseEvent(proxyResponseEvent(400), webSocketService.processEvent(null));
        verifyProxyResponseEvent(proxyResponseEvent(400), webSocketService.processEvent(new WebSocketProxyRequestEvent()));
    }

    @Test
    void nullRouteKey() {
        var proxyRequestEvent = new WebSocketProxyRequestEvent();
        proxyRequestEvent.setRequestContext(new WebSocketRequestContext());
        verifyProxyResponseEvent(proxyResponseEvent(400), webSocketService.processEvent(proxyRequestEvent));
    }

    @Test
    void addWebSocketConnection() throws IOException {
        mockHttpClient(201, "", false);
        var actualProxyResponseEvent = webSocketService.processEvent(proxyRequestEvent("fake-body", requestContext("fake-id", "$connect")));
        verifyHttpClient();
        var websocket = new WebSocket();
        websocket.setConnectionId("fake-id");
        var expectedRequestBody = RequestBody.Companion.create(objectMapper.writeValueAsString(websocket), MediaType.get(String.format("%s; %s","application/json","charset=utf-8")));
        var expectedRequest = httpRequest("http://base-elasticsearch-url.com/socket/_create/fake-id", "POST", expectedRequestBody);
        verifyRequest(expectedRequest, requestArgumentCaptor.getValue());
        var actualWebSocket = objectMapper.readValue(requestBodyToString(requestArgumentCaptor.getValue()), WebSocket.class);
        assertEquals(websocket.getConnectionId(), actualWebSocket.getConnectionId());
        verifyProxyResponseEvent(proxyResponseEvent(201), actualProxyResponseEvent);
    }

    @Test
    void removeWebSocketConnection() throws IOException {
        mockHttpClient(200, "", false);
        var actualProxyResponse = webSocketService.processEvent(proxyRequestEvent("fake-body", requestContext("fake-id-1", "$disconnect")));
        verifyHttpClient();
        verifyRequest(httpRequest("http://base-elasticsearch-url.com/socket/_doc/fake-id-1", "DELETE", null), requestArgumentCaptor.getValue());
        verifyProxyResponseEvent(proxyResponseEvent(200), actualProxyResponse);
    }

    @Test
    void sendMessageFilterSingleSelf() throws IOException {
        mockHttpClient(200, "{\"hits\":{\"hits\":[{\"source\":{\"connectionId\":\"fake-id-3\"}}]}}", false);
        var actualProxyResponse = webSocketService.processEvent(proxyRequestEvent("{}", requestContext("fake-id-3", "sendMessage")));
        verifyNoInteractions(apiGatewayManagementApiClient);
        verifyProxyResponseEvent(proxyResponseEvent(206), actualProxyResponse);
    }

    @Test
    void sendMessageMultipleFilterSelf() throws IOException {
        mockHttpClient(200, "{\"hits\":{\"hits\":[{\"source\":{\"connectionId\":\"fake-id-1\"}},{\"source\":{\"connectionId\":\"fake-id-2\"}},{\"source\":{\"connectionId\":\"fake-id-3\"}}]}}", false);
        var actualProxyResponse = webSocketService.processEvent(proxyRequestEvent("{}", requestContext("fake-id-3", "sendMessage")));
        verifyHttpClient();
        verify(apiGatewayManagementApiClient, times(2)).postToConnection(any(PostToConnectionRequest.class));
        verifyProxyResponseEvent(proxyResponseEvent(206), actualProxyResponse);
    }

    @Test
    public void sendMessageFailedException() throws IOException {
        mockHttpClient(200, "", true);
        var actualProxyResponse = webSocketService.processEvent(proxyRequestEvent("{}", requestContext("fake-id-3", "sendMessage")));
        verifyHttpClient();
        verifyNoInteractions(apiGatewayManagementApiClient);
        verifyProxyResponseEvent(proxyResponseEvent(400), actualProxyResponse);
    }

    private void verifyHttpClient() throws IOException {
        verify(okHttpClient, times(1)).newCall(requestArgumentCaptor.capture());
        verify(remoteCall, times(1)).execute();
    }

    private void verifyRequest(Request expected, Request actual) {
        assertNotNull(actual, "Actual request should not be null");
        assertNotNull(expected, "Expected request should not be null");
        assertEquals(expected.url(), actual.url());
        var actualHeaders = actual.headers();
        var expectedHeaders = expected.headers();
        assertEquals(expectedHeaders.get("Cache-Control"), actualHeaders.get("Cache-Control"));
        assertEquals(expectedHeaders.get("Authorization"), actualHeaders.get("Authorization"));
        assertEquals(expectedHeaders.get("Content-Type"), actualHeaders.get("Content-Type"));
    }

    private Request httpRequest(String fakeURL, String httpMethod, RequestBody requestBody) {
        return new Request.Builder()
                .url(fakeURL)
                .cacheControl(CacheControl.FORCE_NETWORK)
                .method(httpMethod, requestBody)
                .addHeader("Authorization", String.format("Basic %s", properties.getProperty("elasticsearch.authenticationKey")))
                .addHeader("Content-Type", "application/json")
                .build();
    }

    private void mockHttpClient(int statusCode, String body, boolean throwException) throws IOException {
        var response = new Response.Builder()
                .request(new Request.Builder().url(fakeURL).build())
                .protocol(Protocol.HTTP_1_1)
                .body(ResponseBody.Companion.create(body, MediaType.get(String.format("%s; %s", "application/json", "charset=utf-8"))))
                .message("")
                .code(statusCode).build();

        when(okHttpClient.newCall(any())).thenReturn(remoteCall);

        var remoteCallStub = when(remoteCall.execute());
        if (throwException) {
            remoteCallStub.thenThrow(IOException.class);
        }
        else {
            remoteCallStub.thenReturn(response);
        }
    }

    private String requestBodyToString(Request request) {
        String body;
        try(var buffer = new Buffer()) {
            var requestCopy = request.newBuilder().build();
            Objects.requireNonNull(requestCopy.body()).writeTo(buffer);
            body = buffer.readUtf8();
        } catch (IOException ignored) {
            body = null;
        }
        return body;
    }

    private void verifyProxyResponseEvent(WebSocketProxyResponseEvent expected, WebSocketProxyResponseEvent actual) {
        assertNotNull(actual, "Actual proxy request event should not be null");
        assertEquals(expected.getStatusCode(), actual.getStatusCode(), "Expected status code should match actual");
        assertEquals(expected.getBody(), actual.getBody(), "Expected body should match actual");
        assertEquals(expected.getIsBase64Encoded(), actual.getIsBase64Encoded(), "Expected is base 64 encoded should match actual");
    }

    private WebSocketProxyResponseEvent proxyResponseEvent(int statusCode) {
        var proxyResponseEvent = new WebSocketProxyResponseEvent();
        proxyResponseEvent.setStatusCode(statusCode);
        proxyResponseEvent.setBody("");
        proxyResponseEvent.setIsBase64Encoded(false);
        return proxyResponseEvent;
    }

    private WebSocketRequestContext requestContext(String connectionId, String routeKey) {
        var requestContext = new WebSocketRequestContext();
        requestContext.setRouteKey(routeKey);
        requestContext.setConnectionId(connectionId);
        return requestContext;
    }

    private WebSocketProxyRequestEvent proxyRequestEvent(Object body, WebSocketRequestContext requestContext) {
        var proxyRequestEvent = new WebSocketProxyRequestEvent();
        proxyRequestEvent.setBody(body);
        proxyRequestEvent.setRequestContext(requestContext);
        return proxyRequestEvent;
    }
}
