package org.ideaslabut.aws.lambda.service;

import static com.amazonaws.regions.Regions.US_EAST_2;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApiClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.model.PostToConnectionRequest;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import org.ideaslabut.aws.lambda.domain.RouteKey;
import org.ideaslabut.aws.lambda.domain.WebSocketProxyRequestEvent;
import org.ideaslabut.aws.lambda.domain.WebSocketRequestContext;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class WebSocketService {
    private static final int HTTP_OK_STATUS_CODE = 200;
    private static final String WEB_SOCKET_CONNECTION_URL = "https://h8nhk262f7.execute-api.us-east-2.amazonaws.com/production";
    private static final AmazonApiGatewayManagementApi API_GATEWAY_MANAGEMENT_CLIENT = AmazonApiGatewayManagementApiClientBuilder
            .standard()
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(WEB_SOCKET_CONNECTION_URL, US_EAST_2.getName()))
            .build();

    public static WebSocketService INSTANCE = null;

    /**
     * A threadsafe singleton instance for WebSocketService
     *
     * @return a singleton instance which is thread safe
     */
    public static WebSocketService getInstance() {
        if (INSTANCE == null) {
            synchronized (WebSocketService.class) {
                if (INSTANCE == null) {
                    INSTANCE = new WebSocketService();
                }
            }
        }
        return INSTANCE;
    }

    private final Set<String> connectedIds = new HashSet<>();


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
    public APIGatewayProxyResponseEvent processEvent(WebSocketProxyRequestEvent webSocketProxyRequestEvent) {
        if (webSocketProxyRequestEvent == null || webSocketProxyRequestEvent.getRequestContext() == null) {
            return responseEvent(400);
        }

        WebSocketRequestContext requestContext = webSocketProxyRequestEvent.getRequestContext();
        RouteKey routeKey = RouteKey.fromAction(webSocketProxyRequestEvent.getRequestContext().getRouteKey());

        if (routeKey == null) {
            return responseEvent(400);
        }

        switch (routeKey) {
            case CONNECT:
                return this.addConnection(requestContext.getConnectionId());
            case DISCONNECT:
                return this.removeConnection(requestContext.getConnectionId());
            case SEND_MESSAGE:
                return this.sendWebSocketMessage(webSocketProxyRequestEvent.getBody());
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
    private APIGatewayProxyResponseEvent addConnection(String connectionId) {
        this.connectedIds.add(connectionId);
        return responseEvent(HTTP_OK_STATUS_CODE);
    }

    /**
     * Remove the given connection id from the connected webSocket connection set
     *
     * @param connectionId a connection id to be removed
     *
     * @return an api gateway response event with status code 200
     */
    private APIGatewayProxyResponseEvent removeConnection(String connectionId) {
        this.connectedIds.remove(connectionId);
        return responseEvent(HTTP_OK_STATUS_CODE);
    }

    /**
     * Sends the given message body to all available webSocket connections
     *
     * @param body a message body to be sent to all available connection
     *
     * @return an api gateway response event with status code 200 if successful
     */
    private APIGatewayProxyResponseEvent sendWebSocketMessage(final Object body) {
        if (body == null) {
            throw new NullPointerException("A valid message body is required");
        }

        this.connectedIds.forEach(connectionId -> this.sendMessage(connectionId, body));
        return responseEvent(HTTP_OK_STATUS_CODE);
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

        API_GATEWAY_MANAGEMENT_CLIENT.postToConnection(connectionRequest);
    }

    /**
     * Builds a api gateway proxy response event with given status code
     * so that as per api gateway rule so that response body is parsed properly
     *
     * @param statusCode a status code to write to response
     * @return a api gateway response event with given status code and empty body
     */
    private APIGatewayProxyResponseEvent responseEvent(int statusCode) {
        return new APIGatewayProxyResponseEvent()
                .withBody("")
                .withStatusCode(statusCode)
                .withIsBase64Encoded(false);
    }
}
