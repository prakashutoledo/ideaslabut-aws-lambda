package org.ideaslabut.aws.lambda.domain;

import com.fasterxml.jackson.annotation.JsonAlias;

public class WebSocketHits {
    @JsonAlias("_source")
    private WebSocketConnection source;

    public WebSocketConnection getSource() {
        return source;
    }

    public void setSource(WebSocketConnection source) {
        this.source = source;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("source=").append(source);
        sb.append('}');
        return sb.toString();
    }
}
