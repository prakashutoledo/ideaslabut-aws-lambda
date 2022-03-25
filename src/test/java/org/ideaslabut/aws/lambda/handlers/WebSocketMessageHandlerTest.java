package org.ideaslabut.aws.lambda.handlers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

import org.ideaslabut.aws.lambda.domain.WebSocketProxyRequestEvent;
import org.ideaslabut.aws.lambda.domain.WebSocketRequestContext;
import org.ideaslabut.aws.lambda.service.WebSocketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

public class WebSocketMessageHandlerTest {
    @Mock
    private WebSocketService webSocketService;

    @Captor
    private ArgumentCaptor<WebSocketProxyRequestEvent> proxyRequestEventArgumentCaptor;

    @BeforeEach
    public void initMocks() {
        openMocks(this);
    }

    @Test
    void handleRequest() {
        try(var mockedWebSockedService = mockStatic(WebSocketService.class)) {

            var webSocketRequestContext = new WebSocketRequestContext();
            webSocketRequestContext.setConnectionId("fake-id");
            webSocketRequestContext.setRouteKey("fake-route-key");

            var webSocketProxyRequestEvent = new WebSocketProxyRequestEvent();
            webSocketProxyRequestEvent.setBody("fake-object");
            webSocketProxyRequestEvent.setRequestContext(webSocketRequestContext);

            mockedWebSockedService.when(WebSocketService::getInstance).thenReturn(webSocketService);

            new WebSocketMessageHandler().handleRequest(webSocketProxyRequestEvent, null);
            mockedWebSockedService.verify(WebSocketService::getInstance, times(1));
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
