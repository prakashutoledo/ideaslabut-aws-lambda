package org.ideaslabut.aws.lambda.domain;

import com.fasterxml.jackson.annotation.JsonAlias;

public class WebSocketHits {
    @JsonAlias("_source")
    private WebSocket source;

    public WebSocket getSource() {
        return source;
    }

    public void setSource(WebSocket source) {
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
