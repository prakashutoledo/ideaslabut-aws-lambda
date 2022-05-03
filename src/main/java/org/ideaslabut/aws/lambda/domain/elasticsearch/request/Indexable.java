package org.ideaslabut.aws.lambda.domain.elasticsearch.request;

public abstract class Indexable extends Request {
    public static abstract class Builder<I extends Indexable, B extends Builder<I, B>> extends Request.BaseBuilder<I, B> {
        protected String index;

        @SuppressWarnings("unchecked")
        public B withIndex(String index)  {
            this.index = index;
            return (B) this;
        }

        @Override
        public abstract I build();
    }

    protected Indexable() {
    }

    protected Indexable(Builder<?, ?> builder) {
        super(builder);
        setIndex(builder.index);
    }

    private String Index;

    public String getIndex() {
        return Index;
    }

    public void setIndex(String index) {
        Index = index;
    }
}
