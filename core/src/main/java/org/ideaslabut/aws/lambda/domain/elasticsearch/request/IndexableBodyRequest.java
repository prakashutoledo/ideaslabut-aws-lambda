package org.ideaslabut.aws.lambda.domain.elasticsearch.request;

import org.ideaslabut.aws.lambda.domain.elasticsearch.IndexBody;

public abstract class IndexableBodyRequest<I extends IndexBody> extends Indexable {
    protected static abstract class Builder<I extends IndexBody, R extends IndexableBodyRequest<I>, B extends Builder<I, R, B>> extends Indexable.Builder<R, B> {
        protected I body;

        @SuppressWarnings("unchecked")
        public B withBody(I body) {
            this.body = body;
            return (B) this;
        }

        @Override
        public abstract R build();
    }

    private I body;

    protected IndexableBodyRequest(Builder<I, ?, ?> builder) {
        super(builder);
        setBody(builder.body);
    }

    public I getBody() {
        return body;
    }

    public void setBody(I body) {
        this.body = body;
    }
}
