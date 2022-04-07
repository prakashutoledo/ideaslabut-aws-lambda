package org.ideaslabut.aws.lambda.domain;

/**
 * Pojo proxy request input for AWS Lambda function handler
 *
 * @author Prakash Khadka <br>
 *         Created on: Jan 30, 2022
 */
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
