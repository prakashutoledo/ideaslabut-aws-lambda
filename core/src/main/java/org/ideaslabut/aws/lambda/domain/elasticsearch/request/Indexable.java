/*
 * Copyright 2022 IDEAS Lab @ University of Toledo. All rights reserved.
 */
package org.ideaslabut.aws.lambda.domain.elasticsearch.request;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

/**
 * An elasticsearch request which holds elasticsearch index information
 *
 * @author Prakash Khadka <br>
 *     Created On: Jun 27, 2022
 */
public abstract class Indexable extends Request {
    /**
     * An elasticsearch indexable request builder
     *
     * @param <I> A type of body holds by this builder
     * @param <B> A type of indexable builder
     */
    public abstract static class Builder<I extends Indexable, B extends Builder<I, B>> extends Request.BaseBuilder<I, B> {
        protected String index;

        /**
         * Sets the elasticsearch index name for this builder
         *
         * @param index an elasticsearch index name to set
         *
         * @return a reference of indexable builder
         */
        @SuppressWarnings("unchecked")
        public B withIndex(String index) {
            this.index = requireNonNull(index);
            return (B) this;
        }

        /**
         * Builds a new instance of indexable request
         *
         * @return a newly created instance of indexable request
         */
        @Override
        public abstract I build();
    }
    private String Index;

    /**
     * Creates a new instance of {@link Indexable}
     *
     * @param builder a indexable builder to set
     */
    protected Indexable(Builder<?, ?> builder) {
        super(builder);
        setIndex(builder.index);
    }

    public String getIndex() {
        return Index;
    }

    public void setIndex(String index) {
        Index = index;
    }
}
