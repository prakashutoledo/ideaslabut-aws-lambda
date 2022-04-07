package org.ideaslabut.aws.lambda.domain;

import java.util.List;

/**
 * Elasticsearch hits holding webSocket hits value
 *
 * @author Prakash Khadka <br>
 *         Created on: Jan 30, 2022
 */
public class ElasticsearchHits {
    private List<WebSocketHits> hits;

    public List<WebSocketHits> getHits() {
        return hits;
    }

    public void setHits(List<WebSocketHits> hits) {
        this.hits = hits;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("hits=").append(hits);
        sb.append('}');
        return sb.toString();
    }
}
