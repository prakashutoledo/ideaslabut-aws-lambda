package org.ideaslabut.aws.lambda.domain.elasticsearch;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Scroll {
    @JsonProperty("scroll_id")
    private String scrollId;

    public String getScrollId() {
        return scrollId;
    }

    public void setScrollId(String scrollId) {
        this.scrollId = scrollId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("scrollId='").append(scrollId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
