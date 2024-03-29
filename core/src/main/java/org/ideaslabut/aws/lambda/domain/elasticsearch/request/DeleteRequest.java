/*
 * Copyright 2022 IDEAS Lab @ University of Toledo. All rights reserved.
 */
package org.ideaslabut.aws.lambda.domain.elasticsearch.request;

import org.ideaslabut.aws.lambda.domain.elasticsearch.IndexBody;

/**
 * Elasticsearch delete document from index api request
 *
 * @param <I> A type of body hold by this request
 *
 * @author Prakash Khadka <br>
 *     Created On: Jun 27, 2022
 */
public class DeleteRequest<I extends IndexBody> extends IndexableBodyRequest<I> {
    /**
     * Elasticsearch delete request builder
     *
     * @param <I> a type of body holds by this builder
     */
    public static class Builder<I extends IndexBody> extends IndexableBodyRequest.Builder<I, DeleteRequest<I>, Builder<I>> {

        /**
         * Builds a new instance of {@link DeleteRequest}
         *
         * @return a newly created {@link DeleteRequest}
         */
        @Override
        public DeleteRequest<I> build() {
            return new DeleteRequest<>(this);
        }
    }

    /**
     * Creates a new instance of elasticsearch delete document from index request {@link Builder}
     *
     * @param <I> a type of body holds by this builder
     *
     * @return a newly created elasticsearch delete index request {@link Builder}
     */
    public static <I extends IndexBody> Builder<I> builder() {
        return new Builder<>();
    }

    /**
     * Creates a new instance of {@link DeleteRequest}
     *
     * @param builder a delete request builder to create {@link DeleteRequest}
     */
    private DeleteRequest(Builder<I> builder) {
        super(builder);
    }
}
