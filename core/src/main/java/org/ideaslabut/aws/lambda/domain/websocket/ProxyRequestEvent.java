package org.ideaslabut.aws.lambda.domain.websocket;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Pojo proxy request input for AWS Lambda function handler
 *
 * @author Prakash Khadka <br>
 *         Created on: Jan 30, 2022
 */
public class ProxyRequestEvent {
    private RequestContext requestContext;
    private Object body;

    public RequestContext getRequestContext() {
        return requestContext;
    }

    public void setRequestContext(RequestContext requestContext) {
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
        return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
    }
}
