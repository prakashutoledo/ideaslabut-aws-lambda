package org.ideaslabut.aws.lambda.domain.elasticsearch.request;

import org.ideaslabut.aws.lambda.domain.elasticsearch.IndexBody;

public class DeleteRequest<C extends IndexBody> extends CreateRequest<C> {
    public static class Builder<C extends IndexBody> extends BodyBuilder<C, DeleteRequest<C>, Builder<C>> {
        @Override
        public DeleteRequest<C> build() {
            return new DeleteRequest<>(this);
        }
    }

    public static Builder<IndexBody> newDeleteBuilder() {
        return newDeleteBuilder(IndexBody.class);
    }

    public static <C extends IndexBody> Builder<C> newDeleteBuilder(Class<C> klass) {
        return new Builder<>();
    }

    private DeleteRequest(Builder<C> builder) {
        super(builder);
    }
}
