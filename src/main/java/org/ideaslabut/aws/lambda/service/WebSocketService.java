package org.ideaslabut.aws.lambda.service;

//import static com.amazonaws.regions.Regions.US_EAST_2;
import static software.amazon.awssdk.regions.Region.US_EAST_2;
import static java.util.Objects.requireNonNull;


import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Request;
import okhttp3.CacheControl;

import org.ideaslabut.aws.lambda.domain.ElasticsearchResponse;
import org.ideaslabut.aws.lambda.domain.RouteKey;
import org.ideaslabut.aws.lambda.domain.WebSocket;
import org.ideaslabut.aws.lambda.domain.WebSocketProxyResponseEvent;
import org.ideaslabut.aws.lambda.domain.WebSocketProxyRequestEvent;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.PostToConnectionRequest;


import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

public class WebSocketService {
    private static final int HTTP_OK_STATUS_CODE = 200;
    private static final int HTTP_BAD_RESPONSE_STATUS_CODE = 400;
    private static final int HTTP_PARTIAL_CONTENT_STATUS_CODE = 206;

    private static final String APPLICATION_PROPERTIES_FILE = "application.properties";
    private static final String APPLICATION_PROPERTIES_LOCAL_FILE = "application.properties.local";
    private static final String ELASTICSEARCH_URL = "elasticsearch.url";
    private static final String ELASTICSEARCH_AUTHENTICATION_KEY = "elasticsearch.authenticationKey";
    private static final String HTTP_METHOD_GET = "GET";
    private static final String HTTP_METHOD_POST = "POST";
    private static final String HTTP_METHOD_DELETE = "DELETE";
    private static final String WEBSOCKET_MANAGEMENT_URL = "websocket.management.url";

    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final MediaType MEDIA_TYPE_JSON = MediaType.get(String.format("%s; %s",CONTENT_TYPE_JSON,"charset=utf-8"));

    private static WebSocketService INSTANCE = null;

    /**
     * A threadsafe singleton instance for WebSocketService
     *
     * @return a singleton instance which is thread safe
     */
    public static WebSocketService getInstance() {
        if (INSTANCE == null) {
            synchronized (WebSocketService.class) {
                if (INSTANCE == null) {
                    INSTANCE = buildInstance();
                }
            }
        }
        return INSTANCE;
    }

    private static WebSocketService buildInstance() {
        var properties = new Properties();
        try {
            properties.load(WebSocketService.class.getResourceAsStream(APPLICATION_PROPERTIES_FILE));
            properties.load(WebSocketService.class.getResourceAsStream(APPLICATION_PROPERTIES_LOCAL_FILE));
        } catch (IOException ignored) {
        }

       var apiGatewayManagementClient = ApiGatewayManagementApiClient.builder()
               .region(US_EAST_2)
               .endpointOverride(URI.create(properties.getProperty(WEBSOCKET_MANAGEMENT_URL)))
               .build();
        var objectMapper = new ObjectMapper().configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        var okHttpClient = new OkHttpClient().newBuilder().cache(null).build();
        return new WebSocketService(objectMapper, apiGatewayManagementClient, okHttpClient, properties);
    }

    private final Properties properties;
    private final ObjectMapper objectMapper;
    private final OkHttpClient okHttpClient;
    private final ApiGatewayManagementApiClient apiGatewayManagementClient;

    public WebSocketService(ObjectMapper objectMapper,
                            ApiGatewayManagementApiClient apiGatewayManagementClient,
                            OkHttpClient okHttpClient, Properties properties) {
        this.objectMapper = objectMapper;
        this.apiGatewayManagementClient = apiGatewayManagementClient;
        this.okHttpClient = okHttpClient;
        this.properties = properties;
    }


    /**
     * Process the given websocket proxy event by parsing defined routeKey. Default route key $connect and $disconnect
     * will do nothing but setting the connection and disconnecting. RouteKey <code>sendMessage</code> will send the
     * given payload body message to all available connection
     *
     * @param webSocketProxyRequestEvent a websocket proxy event to process
     *
     * @return an api gateway response event with proper status code and empty body
     *         For null events and requestContext a status code of 400 will be returned otherwise returns 200
     *
     * @throws IllegalStateException if no route key are matched, api gateway default route key is considered
     *                               as unmatched
     */
    public WebSocketProxyResponseEvent processEvent(WebSocketProxyRequestEvent webSocketProxyRequestEvent) {
        if (webSocketProxyRequestEvent == null || webSocketProxyRequestEvent.getRequestContext() == null) {
            return responseEvent(HTTP_BAD_RESPONSE_STATUS_CODE);
        }

        var requestContext = webSocketProxyRequestEvent.getRequestContext();
        var routeKey = RouteKey.fromAction(webSocketProxyRequestEvent.getRequestContext().getRouteKey());

        if (routeKey.isEmpty()) {
            return responseEvent(HTTP_BAD_RESPONSE_STATUS_CODE);
        }

        switch (routeKey.get()) {
            case CONNECT:
                return addConnection(requestContext.getConnectionId());
            case DISCONNECT:
                return removeConnection(requestContext.getConnectionId());
            case SEND_MESSAGE:
                return sendWebSocketMessage(requestContext.getConnectionId(), webSocketProxyRequestEvent.getBody());
            default:
                throw new IllegalStateException("Unsupported routeKey: " + routeKey);
        }
    }

    /**
     * Adds the given connection id to the connected webSocket connection set
     *
     * @param connectionId a connection id to be removed
     *
     * @return an api gateway response event with status code 200
     */
    private WebSocketProxyResponseEvent addConnection(String connectionId) {
        int responseStatusCode;

        try {
            var webSocketConnection = new WebSocket();
            webSocketConnection.setConnectionId(connectionId);
            var body = RequestBody.Companion.create(objectMapper.writeValueAsString(webSocketConnection), MEDIA_TYPE_JSON);
            var elasticsearchURL = String.format("%s/%s/%s", properties.getProperty(ELASTICSEARCH_URL), "socket/_create", connectionId);
            var socketCreateResponse = okHttpClient.newCall(httpRequest(elasticsearchURL, HTTP_METHOD_POST, body)).execute();
            responseStatusCode = socketCreateResponse.code();
        } catch (IOException exception) {
            responseStatusCode = HTTP_BAD_RESPONSE_STATUS_CODE;
        }

        return responseEvent(responseStatusCode);
    }

    /**
     * Remove the given connection id from the connected webSocket connection set
     *
     * @param connectionId a connection id to be removed
     *
     * @return an api gateway response event with status code 200
     */
    private WebSocketProxyResponseEvent removeConnection(String connectionId) {
        int statusCode;
        try {
            var elasticsearchURL = String.format("%s/%s/%s", properties.getProperty(ELASTICSEARCH_URL), "socket/_doc", connectionId);
            var removeSocketResponse = okHttpClient
                    .newCall(httpRequest(elasticsearchURL, HTTP_METHOD_DELETE, null))
                    .execute();
            statusCode = removeSocketResponse.code();
        } catch (IOException e) {
            statusCode = HTTP_BAD_RESPONSE_STATUS_CODE;
        }

        return responseEvent(statusCode);
    }

    /**
     * Sends the given message body to all available webSocket connections by filtering
     * current sender
     *
     * @param senderConnectionId A connection id of the sender to be filtered out
     * @param body a message body to be sent to all available connection
     *
     * @return an api gateway response event with status code 200 if successful
     */
    private WebSocketProxyResponseEvent sendWebSocketMessage(String senderConnectionId, final Object body) {
        if (body == null) {
            throw new NullPointerException("A valid message body is required");
        }

        int statusCode;
        try {
            var elasticsearchSocketSearchURL = String.format("%s/%s", properties.getProperty(ELASTICSEARCH_URL), "socket/_search");
            var socketSearchResponse = okHttpClient
                    .newCall(httpRequest(elasticsearchSocketSearchURL, HTTP_METHOD_GET, null))
                    .execute();
            statusCode = socketSearchResponse.code();
            if (statusCode == HTTP_OK_STATUS_CODE) {
                var elasticsearchResponse = objectMapper
                        .readValue(requireNonNull(socketSearchResponse.body()).string(), ElasticsearchResponse.class);

                var success = elasticsearchResponse.getHits()
                        .getHits().stream()
                        .map(hit -> hit.getSource().getConnectionId())
                        .filter(connectionId -> connectionId != null && !connectionId.equals(senderConnectionId))
                        .map(connectionId -> sendMessage(connectionId, body))
                        .reduce(true, (partial, sendMessageStatus) -> partial && sendMessageStatus);

                statusCode = success ? HTTP_OK_STATUS_CODE : HTTP_PARTIAL_CONTENT_STATUS_CODE;
            }
        } catch (IOException ignored) {
           statusCode = HTTP_BAD_RESPONSE_STATUS_CODE;
        }
        return responseEvent(statusCode);
    }

    /**
     * Send the given message body to given webSocket connection id
     *
     * @param connectionId a webSocket connection to send given message body
     * @param body a message body to be sent to given connection id
     *
     * @return <code>true</code> if successful otherwise <code>false</code>
     */
    private boolean sendMessage(String connectionId, Object body) {
        var connectionRequest = PostToConnectionRequest.builder().connectionId(connectionId)
                .data(SdkBytes.fromByteBuffer(ByteBuffer.wrap(body.toString().getBytes(StandardCharsets.UTF_8))))
                .build();

        try {
            apiGatewayManagementClient.postToConnection(connectionRequest);
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }

    /**
     * Builds a api gateway proxy response event with given status code
     * so that as per api gateway rule so that response body is parsed properly
     *
     * @param statusCode a status code to write to response
     * @return a api gateway response event with given status code and empty body
     */
    private WebSocketProxyResponseEvent responseEvent(int statusCode) {
        var proxyResponseEvent = new WebSocketProxyResponseEvent();
        proxyResponseEvent.setBody("");
        proxyResponseEvent.setStatusCode(statusCode);
        proxyResponseEvent.setIsBase64Encoded(false);
        return proxyResponseEvent;
    }

    private Request httpRequest(String elasticsearchURL, String httpMethod, RequestBody requestBody) {
        return new Request.Builder()
                .url(elasticsearchURL)
                .cacheControl(CacheControl.FORCE_NETWORK)
                .method(httpMethod, requestBody)
                .addHeader("Authorization", String.format("Basic %s", properties.getProperty(ELASTICSEARCH_AUTHENTICATION_KEY)))
                .addHeader("Content-Type", CONTENT_TYPE_JSON)
                .build();
    }
}
