package org.ideaslabut.aws.lambda.domain.elasticsearch.request;

import org.ideaslabut.aws.lambda.domain.elasticsearch.IndexBody;

public class DeleteRequest<I extends IndexBody> extends IndexableBodyRequest<I> {
    public static class Builder<I extends IndexBody> extends IndexableBodyRequest.Builder<I, DeleteRequest<I>, Builder<I>> {
        @Override
        public DeleteRequest<I> build() {
            return new DeleteRequest<>(this);
        }
    }

    public static <I extends IndexBody> Builder<I> builder() {
        return new Builder<>();
    }

    private DeleteRequest(Builder<I> builder) {
        super(builder);
    }
}
