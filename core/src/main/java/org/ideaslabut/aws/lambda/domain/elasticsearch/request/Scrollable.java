/*
 * Copyright 2022 IDEAS Lab @ University of Toledo. All rights reserved.
 */
package org.ideaslabut.aws.lambda.domain.elasticsearch.request;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

/**
 * An elasticsearch request which holds scroll information
 *
 * @author Prakash Khadka <br>
 *     Created On: Jun 27, 2022
 */
public abstract class Scrollable extends Indexable {
    /**
     * An elasticsearch scrollable request builder
     *
     * @param <S> a type of scrollable this builder holds
     * @param <B> a type of scrollable builder
     */
    public static abstract class Builder<S extends Scrollable, B extends Builder<S, B>> extends Indexable.Builder<S, B> {
        protected String scroll;

        /**
         * Creates a new instance of {@link Builder}
         */
        protected Builder() {
            this.scroll = "1m";
        }

        /**
         * Sets the scroll for this scrollable builder
         *
         * @param scroll a scroll to set
         *
         * @return a reference of this builder {@link B}
         */
        @SuppressWarnings("unchecked")
        public B withScroll(String scroll) {
            this.scroll = requireNonNull(scroll);
            return (B) this;
        }
    }

    private String scroll;

    /**
     * Creates a new instance of an elasticsearch scrollable request
     *
     * @param builder a scrollable builder to use to create this request
     */
    protected Scrollable(Builder<?, ?> builder) {
        super(builder);
        setScroll(builder.scroll);
    }

    public String getScroll() {
        return scroll;
    }

    public void setScroll(String scroll) {
        this.scroll = scroll;
    }
}
