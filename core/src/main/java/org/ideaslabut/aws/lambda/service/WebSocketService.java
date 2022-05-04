package org.ideaslabut.aws.lambda.service;

import static software.amazon.awssdk.regions.Region.US_EAST_2;

import org.ideaslabut.aws.lambda.domain.elasticsearch.Response;
import org.ideaslabut.aws.lambda.domain.elasticsearch.request.CreateRequest;
import org.ideaslabut.aws.lambda.domain.elasticsearch.request.DeleteRequest;
import org.ideaslabut.aws.lambda.domain.elasticsearch.request.SearchRequest;
import org.ideaslabut.aws.lambda.domain.websocket.ProxyRequestEvent;
import org.ideaslabut.aws.lambda.domain.websocket.ProxyResponseEvent;
import org.ideaslabut.aws.lambda.domain.websocket.RouteKey;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.PostToConnectionRequest;

import java.net.URI;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Service class for managing webSocket request context route
 *
 * @author Prakash Khadka <br>
 * Created on: Jan 30, 2022
 */
public class WebSocketService {
    private static final int HTTP_OK_STATUS_CODE = 200;
    private static final int HTTP_BAD_RESPONSE_STATUS_CODE = 400;
    private static final int HTTP_PARTIAL_CONTENT_STATUS_CODE = 206;
    private static final String WEBSOCKET_MANAGEMENT_URL = "websocket.management.url";
    private static final String WEB_SOCKET_INDEX_NAME = "socket";

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
        var apiGatewayManagementClient = ApiGatewayManagementApiClient.builder()
                .region(US_EAST_2)
                .endpointOverride(URI.create(ApplicationPropertiesService.getInstance().getProperty(WEBSOCKET_MANAGEMENT_URL)))
                .build();
        return new WebSocketService(apiGatewayManagementClient, ElasticsearchService.getInstance());
    }

    private final ApiGatewayManagementApiClient apiGatewayManagementClient;
    private final ElasticsearchService elasticsearchService;

    private WebSocketService(ApiGatewayManagementApiClient apiGatewayManagementClient, ElasticsearchService elasticsearchService) {
        this.elasticsearchService = elasticsearchService;
        this.apiGatewayManagementClient = apiGatewayManagementClient;
    }

    /**
     * Process the given websocket proxy event by parsing defined routeKey. Default route key $connect and $disconnect
     * will do nothing but setting the connection and disconnecting. RouteKey <code>sendMessage</code> will send the
     * given payload body message to all available connection
     *
     * @param proxyRequestEvent a websocket proxy event to process
     * @return an api gateway response event with proper status code and empty body
     * For null events and requestContext a status code of 400 will be returned otherwise returns 200
     * @throws IllegalStateException if no route key are matched, api gateway default route key is considered
     *                               as unmatched
     */
    public ProxyResponseEvent processEvent(ProxyRequestEvent proxyRequestEvent) {
        if (proxyRequestEvent == null || proxyRequestEvent.getRequestContext() == null) {
            return responseEvent(HTTP_BAD_RESPONSE_STATUS_CODE);
        }

        var requestContext = proxyRequestEvent.getRequestContext();
        var routeKey = RouteKey.fromAction(proxyRequestEvent.getRequestContext().getRouteKey());

        if (routeKey.isEmpty()) {
            return responseEvent(HTTP_BAD_RESPONSE_STATUS_CODE);
        }

        switch (routeKey.get()) {
            case CONNECT:
                return addConnection(requestContext.getConnectionId());
            case DISCONNECT:
                return removeConnection(requestContext.getConnectionId());
            case SEND_MESSAGE:
                return sendWebSocketMessage(requestContext.getConnectionId(), proxyRequestEvent.getBody());
            default:
                throw new IllegalStateException("Unsupported routeKey: " + routeKey);
        }
    }

    /**
     * Adds the given connection id to the connected webSocket connection set
     *
     * @param connectionId a connection id to be removed
     * @return an api gateway response event with status code 200
     */
    private ProxyResponseEvent addConnection(String connectionId) {
        final AtomicInteger statusCode = new AtomicInteger();
        Consumer<HttpResponse<String>> responseConsumer = httpResponse -> statusCode.set(httpResponse.statusCode());

        elasticsearchService.create(CreateRequest
                .newCreateBuilder()
                .withIndex(WEB_SOCKET_INDEX_NAME)
                .withBody(connection(connectionId))
                .onHttpSuccess(responseConsumer)
                .onHttpError(responseConsumer)
                .build());

        return responseEvent(statusCode.get());
    }

    /**
     * Remove the given connection id from the connected webSocket connection set
     *
     * @param connectionId a connection id to be removed
     * @return an api gateway response event with status code 200
     */
    private ProxyResponseEvent removeConnection(String connectionId) {
        final AtomicInteger statusCode = new AtomicInteger();
        Consumer<HttpResponse<String>> responseConsumer = httpResponse -> statusCode.set(httpResponse.statusCode());

        elasticsearchService.delete(DeleteRequest
                .newDeleteBuilder(Response.IndexBody.class)
                .withBody(connection(connectionId))
                .onHttpError(responseConsumer)
                .onHttpSuccess(responseConsumer)
                .withIndex(WEB_SOCKET_INDEX_NAME)
                .build());

        return responseEvent(statusCode.get());
    }

    /**
     * Sends the given message body to all available webSocket connections by filtering
     * current sender
     *
     * @param senderConnectionId a connection id of the sender to be filtered out
     * @param body               a message body to be sent to all available connection
     * @return an api gateway response event with status code 200 if successful
     */
    private ProxyResponseEvent sendWebSocketMessage(String senderConnectionId, final Object body) {
        if (body == null) {
            throw new NullPointerException("A valid message body is required");
        }

        final List<Boolean> statusList = new ArrayList<>();
        Consumer<Response> responseConsumer = response -> {
            var status = response
                    .getHits().getHits().stream()
                    .map(hit -> hit.getSource().get("connectionId"))
                    .filter(Objects::nonNull)
                    .map(connectionId -> sendMessage(connectionId, senderConnectionId, body))
                    .reduce(true, (partial, sendMessageStatus) -> partial && sendMessageStatus);
            statusList.add(status);
        };

        elasticsearchService.searchAll(SearchRequest.newBuilder().withSize(10).withIndex(WEB_SOCKET_INDEX_NAME).withScroll("1m").build(), responseConsumer, null);
        return responseEvent(statusList.stream().anyMatch(value -> !value) ? HTTP_PARTIAL_CONTENT_STATUS_CODE : HTTP_OK_STATUS_CODE);
    }

    /**
     * Send the given message body to given webSocket connection id
     *
     * @param toConnectionId a webSocket connection to send given message body
     * @param body           a message body to be sent to given connection id
     * @return <code>true</code> if successful otherwise <code>false</code>
     */
    private boolean sendMessage(String toConnectionId, String fromConnectionId, Object body) {
        if (toConnectionId.equals(fromConnectionId)) {
            return false;
        }
        var connectionRequest = PostToConnectionRequest
                .builder()
                .connectionId(toConnectionId)
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
    private ProxyResponseEvent responseEvent(int statusCode) {
        var proxyResponseEvent = new ProxyResponseEvent();
        proxyResponseEvent.setBody("");
        proxyResponseEvent.setStatusCode(statusCode);
        proxyResponseEvent.setIsBase64Encoded(false);
        return proxyResponseEvent;
    }

    private Response.IndexBody connection(String connectionId) {
        var connection = new Response.IndexBody();
        connection.setId(connectionId);
        return connection;
    }
}
