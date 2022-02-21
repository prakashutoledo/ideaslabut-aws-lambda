package org.ideaslabut.aws.lambda.service;

import static com.amazonaws.regions.Regions.US_EAST_2;
import static java.util.Objects.requireNonNull;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApiClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.model.PostToConnectionRequest;

import com.fasterxml.jackson.databind.DeserializationFeature;
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
import org.ideaslabut.aws.lambda.domain.WebSocketRequestContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class WebSocketService {
    private static final int HTTP_OK_STATUS_CODE = 200;
    private static final int HTTP_BAD_RESPONSE_STATUS_CODE = 400;

    private static final String APPLICATION_PROPERTIES_FILE = "application.properties";
    private static final String ELASTICSEARCH_URL = "elasticsearch.url";
    private static final String ELASTICSEARCH_AUTHENTICATION_KEY = "elasticsearch.authenticationKey";
    private static final String HTTP_METHOD_GET = "GET";
    private static final String HTTP_METHOD_POST = "POST";
    private static final String HTTP_METHOD_DELETE = "DELETE";
    private static final String WEB_SOCKET_CONNECTION_URL = "websocket.connection.url";
;

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
        } catch (IOException ignored) {
        }

        var endPointConfiguration = new EndpointConfiguration(properties.getProperty(WEB_SOCKET_CONNECTION_URL), US_EAST_2.getName());
        var apiGatewayManagementClient = AmazonApiGatewayManagementApiClientBuilder
                .standard()
                .withEndpointConfiguration(endPointConfiguration)
                .build();
        var objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        var okHttpClient = new OkHttpClient().newBuilder().cache(null).build();
        return new WebSocketService(objectMapper, apiGatewayManagementClient, okHttpClient, properties);
    }

    private final Properties properties;
    private final ObjectMapper objectMapper;
    private final OkHttpClient okHttpClient;
    private final AmazonApiGatewayManagementApi amazonApiGatewayManagementApi;

    public WebSocketService(ObjectMapper objectMapper,
                            AmazonApiGatewayManagementApi amazonApiGatewayManagementApi,
                            OkHttpClient okHttpClient, Properties properties) {
        this.objectMapper = objectMapper;
        this.amazonApiGatewayManagementApi = amazonApiGatewayManagementApi;
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

        WebSocketRequestContext requestContext = webSocketProxyRequestEvent.getRequestContext();
        RouteKey routeKey = RouteKey.fromAction(webSocketProxyRequestEvent.getRequestContext().getRouteKey());

        if (routeKey == null) {
            return responseEvent(HTTP_BAD_RESPONSE_STATUS_CODE);
        }

        switch (routeKey) {
            case CONNECT:
                return addConnection(requestContext.getConnectionId());
            case DISCONNECT:
                return removeConnection(requestContext.getConnectionId());
            case SEND_MESSAGE:
                return sendWebSocketMessage(webSocketProxyRequestEvent.getBody());
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
            var createResponse = okHttpClient.newCall(httpRequest(elasticsearchURL, HTTP_METHOD_POST, body)).execute();
            responseStatusCode = createResponse.code();
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
            var deleteResponse = okHttpClient
                    .newCall(httpRequest(elasticsearchURL, HTTP_METHOD_DELETE, null))
                    .execute();
            statusCode = deleteResponse.code();
        } catch (IOException e) {
            statusCode = HTTP_BAD_RESPONSE_STATUS_CODE;
        }

        return responseEvent(statusCode);
    }

    /**
     * Sends the given message body to all available webSocket connections
     *
     * @param body a message body to be sent to all available connection
     *
     * @return an api gateway response event with status code 200 if successful
     */
    private WebSocketProxyResponseEvent sendWebSocketMessage(final Object body) {
        if (body == null) {
            throw new NullPointerException("A valid message body is required");
        }

        int statusCode;
        try {
            var elasticsearchSocketSearchURL = String.format("%s/%s", properties.getProperty(ELASTICSEARCH_URL), "socket/_search");
            var getResponse = okHttpClient
                    .newCall(httpRequest(elasticsearchSocketSearchURL, HTTP_METHOD_GET, null))
                    .execute();
            statusCode = getResponse.code();
            if (statusCode == HTTP_OK_STATUS_CODE) {
                var elasticsearchResponse = objectMapper
                        .readValue(requireNonNull(getResponse.body()).string(), ElasticsearchResponse.class);

                elasticsearchResponse.getHits()
                        .getHits().stream()
                        .map(hit -> hit.getSource().getConnectionId())
                        .forEach(connectionId -> this.sendMessage(connectionId, body));
            }
        } catch (IOException ioe) {
           statusCode = HTTP_BAD_RESPONSE_STATUS_CODE;
        }
        return responseEvent(statusCode);
    }

    /**
     * Send the given message body to given webSocket connection id
     *
     * @param connectionId a webSocket connection to send given message body
     * @param body a message body to be sent to given connection id
     */
    private void sendMessage(String connectionId, Object body) {
        PostToConnectionRequest connectionRequest = new PostToConnectionRequest()
                .withConnectionId(connectionId)
                .withData(ByteBuffer.wrap(body.toString().getBytes(StandardCharsets.UTF_8)));

        try {
            amazonApiGatewayManagementApi.postToConnection(connectionRequest);
        } catch (Exception ignored) {
        }
    }

    /**
     * Builds a api gateway proxy response event with given status code
     * so that as per api gateway rule so that response body is parsed properly
     *
     * @param statusCode a status code to write to response
     * @return a api gateway response event with given status code and empty body
     */
    private WebSocketProxyResponseEvent responseEvent(int statusCode) {
        WebSocketProxyResponseEvent proxyResponseEvent = new WebSocketProxyResponseEvent();
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
