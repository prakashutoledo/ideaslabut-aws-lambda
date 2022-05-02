package org.ideaslabut.aws.lambda.domain.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Pojo holding webSocket connection details
 *
 * @author Prakash Khadka <br>
 *         Created on: Jan 30, 2022
 */
public class Connection {
    @JsonProperty("connectionId")
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("connectionId='").append(id).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
