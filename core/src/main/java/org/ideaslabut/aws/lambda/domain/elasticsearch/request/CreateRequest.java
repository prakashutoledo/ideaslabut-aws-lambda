package org.ideaslabut.aws.lambda.domain.elasticsearch.request;

import org.ideaslabut.aws.lambda.domain.elasticsearch.IndexBody;

public class CreateRequest<I extends IndexBody> extends IndexableBodyRequest<I> {
    public static class Builder<I extends IndexBody> extends IndexableBodyRequest.Builder<I, CreateRequest<I>, Builder<I>> {
        @Override
        public CreateRequest<I> build() {
            return new CreateRequest<>(this);
        }
    }

    public static <I extends IndexBody> Builder<I> builder() {
        return new Builder<>();
    }

    protected CreateRequest(Builder<I> builder) {
        super(builder);
    }
}
