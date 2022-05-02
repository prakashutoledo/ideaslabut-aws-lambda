package org.ideaslabut.aws.lambda.domain.elasticsearch.request;

import org.ideaslabut.aws.lambda.domain.websocket.Connection;

public class DeleteRequest <C extends Connection> extends CreateRequest<C> {
    public static class Builder<C extends Connection> extends CreateRequest.BaseBuilder<C, DeleteRequest<C>, Builder<C>>{
        @Override
        public DeleteRequest<C> build() {
            return new DeleteRequest<>(this);
        }
    }

    public static Builder<Connection> newDeleteBuilder() {
        return newDeleteBuilder(Connection.class);
    }

    public static <B extends Connection> Builder<B> newDeleteBuilder(Class<B> klass) {
        return new Builder<>();
    }

    private DeleteRequest(Builder<C> builder) {
        setIndex(builder.index);
        setBody(builder.body);
        setExceptionConsumer(builder.exceptionConsumer);
        setErrorConsumer(builder.errorConsumer);
        setSuccessConsumer(builder.successConsumer);
    }
}
