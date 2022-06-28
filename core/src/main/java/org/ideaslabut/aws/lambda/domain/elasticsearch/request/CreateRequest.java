/*
 * Copyright 2022 IDEAS Lab @ University of Toledo. All rights reserved.
 */
package org.ideaslabut.aws.lambda.domain.elasticsearch.request;

import org.ideaslabut.aws.lambda.domain.elasticsearch.IndexBody;

/**
 * Elasticsearch request to create document to index api request
 *
 * @param <I> a type of body hold by this request
 *
 * @author Prakash Khadka <br>
 *     Created On: Jun 27, 2022
 */
public class CreateRequest<I extends IndexBody> extends IndexableBodyRequest<I> {
    /**
     * Elasticsearch create request builder
     *
     * @param <I> a type of body hold by this builder
     */
    public static class Builder<I extends IndexBody> extends IndexableBodyRequest.Builder<I, CreateRequest<I>, Builder<I>> {
        /**
         * Builds a new instance of {@link CreateRequest}
         *
         * @return a newly created {@link CreateRequest}
         */
        @Override
        public CreateRequest<I> build() {
            return new CreateRequest<>(this);
        }
    }

    /**
     * Creates a new instance of elasticsearch create request
     *
     * @param <I> a type of body hold by this builder
     *
     * @return a newly created builder
     */
    public static <I extends IndexBody> Builder<I> builder() {
        return new Builder<>();
    }

    /**
     * Creates a new instance of {@link CreateRequest}
     *
     * @param builder a create request builder to create {@link CreateRequest}
     */
    protected CreateRequest(Builder<I> builder) {
        super(builder);
    }
}
