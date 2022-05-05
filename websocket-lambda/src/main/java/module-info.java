module ideaslabut.aws.lambda {
    requires ideaslabut.aws.lambda.core;

    // Below 4 are automatic module
    requires aws.lambda.java.core;
    requires software.amazon.awssdk.core;
    requires software.amazon.awssdk.services.apigatewaymanagementapi;
    requires software.amazon.awssdk.regions;
    requires org.slf4j;

    uses com.amazonaws.services.lambda.runtime.Context;
    uses com.amazonaws.services.lambda.runtime.RequestHandler;
    uses org.ideaslabut.aws.lambda.domain.websocket.ProxyRequestEvent;
    uses org.ideaslabut.aws.lambda.domain.websocket.ProxyResponseEvent;
    uses org.ideaslabut.aws.lambda.service.WebSocketService;
}