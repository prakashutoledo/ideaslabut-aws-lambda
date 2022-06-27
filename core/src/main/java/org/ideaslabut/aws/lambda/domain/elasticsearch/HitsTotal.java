/*
 * Copyright 2022 IDEAS Lab @ University of Toledo.. All rights reserved.
 */
package org.ideaslabut.aws.lambda.domain.elasticsearch;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * A pojo representing Elasticsearch search response hits total node
 *
 * @author Prakash Khadka <br>
 *     Created On: Jun 27, 2022
 */
public class HitsTotal {
    private long value;

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
    }
}
