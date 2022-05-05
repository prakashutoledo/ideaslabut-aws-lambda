module ideaslabut.aws.lambda {
    requires ideaslabut.aws.lambda.core;
    requires aws.lambda.java.core;
    requires org.slf4j;

    uses com.amazonaws.services.lambda.runtime.Context;
    uses com.amazonaws.services.lambda.runtime.RequestHandler;
    uses org.ideaslabut.aws.lambda.domain.websocket.ProxyRequestEvent;
    uses org.ideaslabut.aws.lambda.domain.websocket.ProxyResponseEvent;
    uses org.ideaslabut.aws.lambda.service.WebSocketService;
}