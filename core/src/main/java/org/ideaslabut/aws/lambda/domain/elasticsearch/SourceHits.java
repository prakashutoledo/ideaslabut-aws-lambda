package org.ideaslabut.aws.lambda.domain.elasticsearch;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.Map;

/**
 * Pojo that mimics Elasticsearch search hits source json node
 *
 * @author Prakash Khadka <br>
 *         Created on: Jan 30, 2022
 */
public class SourceHits {
    @JsonAlias("_source")
    private Map<String, String> source;

    public Map<String, String> getSource() {
        return source;
    }

    public void setSource(Map<String, String> source) {
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
