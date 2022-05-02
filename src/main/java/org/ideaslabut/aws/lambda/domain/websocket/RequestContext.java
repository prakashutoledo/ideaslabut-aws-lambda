package org.ideaslabut.aws.lambda.domain.websocket;

/**
 * Pojo for AWS lambda function proxy request context
 *
 * @author Prakash Khadka <br>
 *         Created on: Jan 30, 2022
 */
public class RequestContext {
    private String connectionId;
    private String routeKey;

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public String getRouteKey() {
        return routeKey;
    }

    public void setRouteKey(String routeKey) {
        this.routeKey = routeKey;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("connectionId='").append(connectionId).append('\'');
        sb.append(", routeKey='").append(routeKey).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
