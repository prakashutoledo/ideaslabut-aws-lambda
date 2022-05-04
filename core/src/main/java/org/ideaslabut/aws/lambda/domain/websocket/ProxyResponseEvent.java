package org.ideaslabut.aws.lambda.domain.websocket;

/**
 * Pojo proxy response for AWS Lambda function handler
 *
 * @author Prakash Khadka <br>
 *         Created on: Jan 30, 2022
 */
public class ProxyResponseEvent {
    private Integer statusCode;
    private Boolean isBase64Encoded;
    private String body;

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Boolean getIsBase64Encoded() {
        return isBase64Encoded;
    }

    public void setIsBase64Encoded(Boolean isBase64Encoded) {
        this.isBase64Encoded = isBase64Encoded;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("statusCode=").append(statusCode);
        sb.append(", isBase64Encoded=").append(isBase64Encoded);
        sb.append(", body='").append(body).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
