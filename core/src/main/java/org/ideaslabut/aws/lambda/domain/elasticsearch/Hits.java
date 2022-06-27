/*
 * Copyright 2022 IDEAS Lab @ University of Toledo. All rights reserved.
 */
package org.ideaslabut.aws.lambda.domain.elasticsearch;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

/**
 * A pojo representing Elasticsearch response json hits node
 *
 * @author Prakash Khadka <br>
 *         Created on: Jan 30, 2022
 */
public class Hits {
    private HitsTotal total;
    private List<SourceHits> hits;

    public HitsTotal getTotal() {
        return total;
    }

    public void setTotal(HitsTotal total) {
        this.total = total;
    }

    public List<SourceHits> getHits() {
        return hits;
    }

    public void setHits(List<SourceHits> hits) {
        this.hits = hits;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
    }
}
