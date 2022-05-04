package org.ideaslabut.aws.lambda.domain.elasticsearch.request;

import org.ideaslabut.aws.lambda.domain.elasticsearch.Response;

public class DeleteRequest<C extends Response.IndexBody> extends CreateRequest<C> {
    public static class Builder<C extends Response.IndexBody> extends BodyBuilder<C, DeleteRequest<C>, Builder<C>> {
        @Override
        public DeleteRequest<C> build() {
            return new DeleteRequest<>(this);
        }
    }

    public static Builder<Response.IndexBody> newDeleteBuilder() {
        return newDeleteBuilder(Response.IndexBody.class);
    }

    public static <C extends Response.IndexBody> Builder<C> newDeleteBuilder(Class<C> klass) {
        return new Builder<>();
    }

    private DeleteRequest(Builder<C> builder) {
        super(builder);
    }
}
