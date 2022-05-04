package org.ideaslabut.aws.lambda.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.ideaslabut.aws.lambda.domain.websocket.ProxyRequestEvent;
import org.ideaslabut.aws.lambda.domain.websocket.RequestContext;
import org.ideaslabut.aws.lambda.service.WebSocketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

/**
 * Unit test for WebSocketMessageHandler
 *
 * @author Prakash Khadka <br>
 *         Created on: Jan 30, 2022
 */
public class WebSocketMessageHandlerTest {
    @Mock
    private WebSocketService webSocketService;

    @Captor
    private ArgumentCaptor<ProxyRequestEvent> proxyRequestEventArgumentCaptor;

    @BeforeEach
    public void initMocks() {
        openMocks(this);
    }

    @Test
    void handleRequest() {
        try(var mockedWebSocketService = mockStatic(WebSocketService.class)) {

            var webSocketRequestContext = new RequestContext();
            webSocketRequestContext.setConnectionId("fake-id");
            webSocketRequestContext.setRouteKey("fake-route-key");

            var webSocketProxyRequestEvent = new ProxyRequestEvent();
            webSocketProxyRequestEvent.setBody("fake-object");
            webSocketProxyRequestEvent.setRequestContext(webSocketRequestContext);

            mockedWebSocketService.when(WebSocketService::getInstance).thenReturn(webSocketService);

            new WebSocketMessageHandler().handleRequest(webSocketProxyRequestEvent, null);
            mockedWebSocketService.verify(WebSocketService::getInstance, times(1));
            verify(webSocketService, times(1)).processEvent(proxyRequestEventArgumentCaptor.capture());

            var actualProxyRequestEvent = proxyRequestEventArgumentCaptor.getValue();
            assertNotNull(actualProxyRequestEvent, "Captured Event should not be null");
            assertEquals(webSocketProxyRequestEvent.getBody(), actualProxyRequestEvent.getBody(), "Expected body should matched to actual");

            var actualWebSocketContext = actualProxyRequestEvent.getRequestContext();
            assertEquals(webSocketRequestContext.getConnectionId(), actualWebSocketContext.getConnectionId(), "Expected connection id should match actual");
            assertEquals(webSocketRequestContext.getRouteKey(), actualWebSocketContext.getRouteKey(), "Expected route key should match actual");
        }
    }
}
