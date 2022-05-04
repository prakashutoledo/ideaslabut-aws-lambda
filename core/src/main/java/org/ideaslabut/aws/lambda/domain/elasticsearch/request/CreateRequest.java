package org.ideaslabut.aws.lambda.domain.elasticsearch.request;

import org.ideaslabut.aws.lambda.domain.elasticsearch.Response;

public class CreateRequest<C extends Response.IndexBody> extends Indexable {
    public static class Builder<C extends Response.IndexBody> extends BodyBuilder<C, CreateRequest<C>, Builder<C>> {
        @Override
        public CreateRequest<C> build() {
            return new CreateRequest<>(this);
        }
    }

    protected static abstract class BodyBuilder<C extends Response.IndexBody, R extends CreateRequest<C>, B extends BodyBuilder<C, R, B>> extends Indexable.Builder<R, B> {
        protected C body;

        @SuppressWarnings("unchecked")
        public B withBody(C body) {
            this.body = body;
            return (B) this;
        }

        @Override
        public abstract R build();
    }

    public static Builder<Response.IndexBody> newCreateBuilder() {
        return newCreateBuilder(Response.IndexBody.class);
    }

    public static <B extends Response.IndexBody> Builder<B> newCreateBuilder(Class<B> klass) {
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
