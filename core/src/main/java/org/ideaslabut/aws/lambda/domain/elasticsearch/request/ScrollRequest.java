/*
 * Copyright 2022 IDEAS Lab @ University of Toledo. All rights reserved.
 */
package org.ideaslabut.aws.lambda.domain.elasticsearch.request;

import static java.util.Objects.requireNonNull;

/**
 * An elasticsearch request which helps to perform scroll request
 *
 * @author Prakash Khadka <br>
 *     Created On: Jun 27, 2022
 */
public class ScrollRequest extends Scrollable {
    /**
     * An elasticsearch scroll request builder
     */
    public static class Builder extends Scrollable.Builder<ScrollRequest, Builder> {
        private String scrollId;

        /**
         * Creates a new instance of {@link Builder}
         */
        private Builder() {
            super();
        }

        /**
         * Sets the scroll id for this elasticsearch scroll request builder
         *
         * @param scrollId a scroll id to set
         *
         * @return a reference of this builder {@link Builder}
         */
        public Builder withScrollId(String scrollId) {
            this.scrollId = requireNonNull(scrollId);
            return this;
        }

        /**
         * Builds a new instance of {@link ScrollRequest}
         *
         * @return a newly created scroll request
         */
        @Override
        public ScrollRequest build() {
            return new ScrollRequest(this);
        }
    }

    /**
     * Creates a new instance of {@link Builder}
     *
     * @return a newly created builder {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }
    private String scrollId;

    /**
     * Creates a new instance of an elasticsearch scroll request
     *
     * @param builder a scroll request builder to use
     */
    private ScrollRequest(Builder builder) {
        super(builder);
        setScrollId(builder.scrollId);
    }

    public String getScrollId() {
        return scrollId;
    }

    public void setScrollId(String scrollId) {
        this.scrollId = scrollId;
    }
}
