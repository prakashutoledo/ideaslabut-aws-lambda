package org.ideaslabut.aws.lambda.domain;

/**
 * Pojo holding webSocket connection details
 *
 * @author Prakash Khadka <br>
 *         Created on: Jan 30, 2022
 */
public class WebSocket {
    private String connectionId;

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("connectionId='").append(connectionId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
