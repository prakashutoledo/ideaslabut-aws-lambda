package org.ideaslabut.aws.lambda.domain.elasticsearch.request;

public class ScrollRequest extends ScrollableRequest {
    public static class Builder extends ScrollableRequest.Builder<ScrollRequest, Builder> {
        private String scrollId;

        private Builder() {
            super();
        }

        public Builder withScrollId(String scrollId) {
            this.scrollId = scrollId;
            return this;
        }

        @Override
        public ScrollRequest build() {
            return new ScrollRequest(this);
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    private String scrollId;

    private ScrollRequest(Builder builder) {
        setScrollId(builder.scrollId);
        setScroll(builder.scroll);
        setSuccessConsumer(builder.successConsumer);
        setExceptionConsumer(builder.exceptionConsumer);
        setErrorConsumer(builder.errorConsumer);
    }

    public String getScrollId() {
        return scrollId;
    }

    public void setScrollId(String scrollId) {
        this.scrollId = scrollId;
    }
}
