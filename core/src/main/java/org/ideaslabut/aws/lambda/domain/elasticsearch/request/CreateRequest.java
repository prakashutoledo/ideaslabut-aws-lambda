package org.ideaslabut.aws.lambda.domain.elasticsearch.request;

import org.ideaslabut.aws.lambda.domain.elasticsearch.IndexBody;

/**
 * @param <I> a type of body hold by this request
 */
public class CreateRequest<I extends IndexBody> extends IndexableBodyRequest<I> {
    /**
     * Creates a new builder for creating
     *
     * @param <I> a type of body hold by this builder
     *
     * @return a newly created builder
     */
    public static <I extends IndexBody> Builder<I> builder() {
        return new Builder<>();
    }

    /**
     * @param <I> a type of body hold by this builder
     */
    public static class Builder<I extends IndexBody> extends IndexableBodyRequest.Builder<I, CreateRequest<I>, Builder<I>> {
        /**
         *
         * @return
         */
        @Override
        public CreateRequest<I> build() {
            return new CreateRequest<>(this);
        }
    }

    protected CreateRequest(Builder<I> builder) {
        super(builder);
    }
}
