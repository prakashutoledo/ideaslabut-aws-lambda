package org.ideaslabut.aws.lambda.domain.elasticsearch.request;

import org.ideaslabut.aws.lambda.domain.websocket.Connection;


public class CreateRequest<C extends Connection> extends IndexableRequest {
    public static class Builder<C extends Connection> extends BaseBuilder<C, CreateRequest<C>, Builder<C>> {
        @Override
        public CreateRequest<C> build() {
            return new CreateRequest<>(this);
        }
    }

    protected static abstract class BaseBuilder<C extends Connection, R extends CreateRequest<C>, B extends BaseBuilder<C, R, B>> extends IndexableRequest.Builder<R, B> {
        protected C body;

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

    protected CreateRequest(BaseBuilder builder) {
        this((Builder<C>) builder);
    }

    protected CreateRequest(Builder<C> builder) {
        super(builder);
        setBody(builder.body);
    }

    public C getBody() {
        return body;
    }

    public void setBody(C body) {
        this.body = body;
    }
}
