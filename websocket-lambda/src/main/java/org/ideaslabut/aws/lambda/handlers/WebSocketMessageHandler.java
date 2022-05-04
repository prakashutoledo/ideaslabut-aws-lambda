package org.ideaslabut.aws.lambda.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import org.ideaslabut.aws.lambda.domain.websocket.ProxyRequestEvent;
import org.ideaslabut.aws.lambda.domain.websocket.ProxyResponseEvent;
import org.ideaslabut.aws.lambda.service.WebSocketService;

/**
 * AWS Lamba function request handler for handling webSocket request context route
 *
 * @author Prakash Khadka <br>
 *         Created on: Jan 30, 2022
 */
public class WebSocketMessageHandler implements RequestHandler<ProxyRequestEvent, ProxyResponseEvent> {
    /**
     * Handles input request for given
     *
     * @param event a map of data attributes passed as an input for given lambda function
     * @param context a current context for given lambda function
     * @return a response from current lambda function
     */
    @Override
    public ProxyResponseEvent handleRequest(ProxyRequestEvent event, Context context) {
        var webSocketService = WebSocketService.getInstance();
        return webSocketService.processEvent(event);
    }
}