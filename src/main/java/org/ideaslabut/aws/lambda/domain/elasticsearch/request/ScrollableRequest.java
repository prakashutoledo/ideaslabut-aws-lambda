package org.ideaslabut.aws.lambda.domain.elasticsearch.request;

@SuppressWarnings("unchecked")
public abstract class ScrollableRequest extends IndexableRequest {
    public static abstract class Builder<S extends ScrollableRequest, B extends Builder<S, B>> extends IndexableRequest.Builder<S, B> {
        protected String scroll;

        protected Builder() {
            this.scroll = "1m";
        }

        public B withScroll(String scroll) {
            this.scroll = scroll;
            return (B) this;
        }
    }

    private String scroll;

    protected ScrollableRequest() {
    }

    protected ScrollableRequest(Builder builder) {
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
