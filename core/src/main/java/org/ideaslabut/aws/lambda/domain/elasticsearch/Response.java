package org.ideaslabut.aws.lambda.domain.elasticsearch;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class that mimic Elasticsearch search api response with hits value only
 *
 * @author Prakash Khadka <br>
 *         Created on: Jan 30, 2022
 */
public class Response {
    @JsonAlias("_scroll_id")
    private String scrollId;
    private Hits hits;

    public String getScrollId() {
        return scrollId;
    }

    public void setScrollId(String scrollId) {
        this.scrollId = scrollId;
    }

    public Hits getHits() {
        return hits;
    }

    public void setHits(Hits hits) {
        this.hits = hits;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("scrollId='").append(scrollId).append('\'');
        sb.append(", hits=").append(hits);
        sb.append('}');
        return sb.toString();
    }
}
