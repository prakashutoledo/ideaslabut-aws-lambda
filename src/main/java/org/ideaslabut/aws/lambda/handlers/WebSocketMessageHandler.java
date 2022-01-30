package org.ideaslabut.aws.lambda.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import org.ideaslabut.aws.lambda.domain.WebSocketProxyRequestEvent;
import org.ideaslabut.aws.lambda.service.WebSocketService;

public class WebSocketMessageHandler implements RequestHandler<WebSocketProxyRequestEvent, APIGatewayProxyResponseEvent> {
    /**
     * Handles input request for given
     *
     * @param event a map of data attributes passed as an input for given lambda function
     * @param context a current context for given lambda function
     * @return a response from current lambda function
     */
    @Override
    public APIGatewayProxyResponseEvent handleRequest(WebSocketProxyRequestEvent event, Context context) {
        return WebSocketService.getInstance().processEvent(event);
    }
}
