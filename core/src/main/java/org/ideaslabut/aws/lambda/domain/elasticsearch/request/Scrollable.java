package org.ideaslabut.aws.lambda.domain.elasticsearch.request;

public abstract class Scrollable extends Indexable {
    public static abstract class Builder<S extends Scrollable, B extends Builder<S, B>> extends Indexable.Builder<S, B> {
        protected String scroll;

        protected Builder() {
            this.scroll = "1m";
        }

        @SuppressWarnings("unchecked")
        public B withScroll(String scroll) {
            this.scroll = scroll;
            return (B) this;
        }
    }

    private String scroll;

    protected Scrollable(Builder<?, ?> builder) {
        super(builder);
        setScroll(builder.scroll);
    }

    public String getScroll() {
        return scroll;
    }

    public void setScroll(String scroll) {
        this.scroll = scroll;
    }
}
