/*
 * Copyright 2022 IDEAS Lab @ University of Toledo. All rights reserved.
 */
package org.ideaslabut.aws.lambda.domain.elasticsearch.request;

/**
 * An elasticsearch search document from index api request
 *
 * @author Prakash Khadka <br>
 *     Created On: Jun 27, 2022
 */
public class SearchRequest extends Scrollable {
    /**
     * A builder for {@link SearchRequest}
     */
    public static class Builder extends Scrollable.Builder<SearchRequest, Builder> {
        private long size;

        /**
         * Creates a new instance of an elasticsearch search request builder
         */
        private Builder() {
            super();
            this.size = 10;
        }

        /**
         * A size of search request holds by this builder
         *
         * @param size a size to set
         *
         * @return a reference of this elasticsearch search request builder
         */
        public Builder withSize(long size) {
            this.size = size;
            return this;
        }

        /**
         * Builds a new instance of {@link SearchRequest}
         *
         * @return a newly created search request
         */
        @Override
        public SearchRequest build() {
            return new SearchRequest(this);
        }
    }

    /**
     * Creates a new instance of elasticsearch search request builder
     *
     * @return a newly created elasticsearch search request builder
     */
    public static Builder builder() {
        return new Builder();
    }

    private long size;

    /**
     * Creates a new instance of {@link SearchRequest}
     *
     * @param builder an elasticsearch search request builder to use
     */
    private SearchRequest(Builder builder) {
        super(builder);
        setSize(builder.size);
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
