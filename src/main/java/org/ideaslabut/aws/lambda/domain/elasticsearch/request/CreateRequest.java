package org.ideaslabut.aws.lambda.domain.elasticsearch.request;

import org.ideaslabut.aws.lambda.domain.websocket.Connection;

public class CreateRequest<C extends Connection> extends Indexable {
    public static class Builder<C extends Connection> extends BodyBuilder<C, CreateRequest<C>, Builder<C>> {
        @Override
        public CreateRequest<C> build() {
            return new CreateRequest<>(this);
        }
    }

    protected static abstract class BodyBuilder<C extends Connection, R extends CreateRequest<C>, B extends BodyBuilder<C, R, B>> extends Indexable.Builder<R, B> {
        protected C body;

        @SuppressWarnings("unchecked")
        public B withBody(C body) {
            this.body = body;
            return (B) this;
        }

        @Override
        public abstract R build();
    }

    public static Builder<Connection> newCreateBuilder() {
        return newCreateBuilder(Connection.class);
    }

    public static <B extends Connection> Builder<B> newCreateBuilder(Class<B> klass) {
        return new Builder<>();
    }

    private C body;

    @SuppressWarnings("unchecked")
    protected CreateRequest(BodyBuilder<C, ?, ?> builder) {
        super(builder);
        setBody(builder.body);
    }

    protected CreateRequest(Builder<C> builder) {
        this((BodyBuilder<C, ?, ?>) builder);
    }

    public C getBody() {
        return body;
    }

    public void setBody(C body) {
        this.body = body;
    }
}
