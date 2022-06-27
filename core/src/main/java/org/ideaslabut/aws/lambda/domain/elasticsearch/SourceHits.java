/*
 * Copyright 2022 IDEAS Lab @ University of Toledo.. All rights reserved.
 */
package org.ideaslabut.aws.lambda.domain.elasticsearch;

import com.fasterxml.jackson.annotation.JsonAlias;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Map;

/**
 * Pojo that mimics Elasticsearch search hits source json node
 *
 * @author Prakash Khadka <br>
 *     Created on: Jan 30, 2022
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
        return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
    }
}
