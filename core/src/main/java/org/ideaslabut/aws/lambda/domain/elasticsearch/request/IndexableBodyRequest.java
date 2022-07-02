/*
 * Copyright 2022 IDEAS Lab @ University of Toledo. All rights reserved.
 */
package org.ideaslabut.aws.lambda.domain.elasticsearch.request;

import static java.util.Objects.requireNonNull;

import org.ideaslabut.aws.lambda.domain.elasticsearch.IndexBody;

import java.util.Objects;

/**
 * An elasticsearch request to hold indexable body information
 *
 * @param <I> a type of index body holds by this request
 *
 * @author Prakash Khadka <br>
 *     Created On: Jun 27, 2022
 */
public abstract class IndexableBodyRequest<I extends IndexBody> extends Indexable {
    /**
     * An elasticsearch indexable body request builder i.e a builder for {@link IndexableBodyRequest}
     *
     * @param <I> A type of indexable body holds by this builder
     * @param <R> a type of indexable body request
     * @param <B> a type of indexable body request builder
     */
    protected static abstract class Builder<I extends IndexBody, R extends IndexableBodyRequest<I>, B extends Builder<I, R, B>> extends Indexable.Builder<R, B> {
        protected I body;

        /**
         * Sets the indexable body for this builder
         *
         * @param body a indexable body to set
         *
         * @return a reference of this indexable body request builder
         */
        @SuppressWarnings("unchecked")
        public B withBody(I body) {
            if (body == null) {
                throw new NullPointerException("Body is null");
            }
            if (body.getId() == null) {
                throw new NullPointerException("Invalid body");
            }

            this.body = body;
            return (B) this;
        }

        /**
         * Builds a new instance of {@link IndexableBodyRequest}
         *
         * @return a newly created instance of {@link IndexableBodyRequest}
         */
        @Override
        public abstract R build();
    }

    private I body;

    /**
     * Creates a new instance of {@link IndexableBodyRequest}
     *
     * @param builder a indexable body request builder to create {@link IndexableBodyRequest}
     */
    protected IndexableBodyRequest(Builder<I, ?, ?> builder) {
        super(builder);
        setBody(builder.body);
    }

    public I getBody() {
        return body;
    }

    public void setBody(I body) {
        this.body = body;
    }
}
