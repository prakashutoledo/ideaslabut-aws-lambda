package org.ideaslabut.aws.lambda.domain;

public class WebSocketProxyRequestEvent {
    private WebSocketRequestContext requestContext;
    private Object body;

    public WebSocketRequestContext getRequestContext() {
        return requestContext;
    }

    public void setRequestContext(WebSocketRequestContext requestContext) {
        this.requestContext = requestContext;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("requestContext=").append(requestContext);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
