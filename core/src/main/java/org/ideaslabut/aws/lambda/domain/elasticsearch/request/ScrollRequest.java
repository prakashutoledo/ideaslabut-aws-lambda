package org.ideaslabut.aws.lambda.domain.elasticsearch.request;

public class ScrollRequest extends Scrollable {
    public static class Builder extends Scrollable.Builder<ScrollRequest, Builder> {
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

    public static Builder builder() {
        return new Builder();
    }

    private String scrollId;

    private ScrollRequest(Builder builder) {
        super(builder);
        setScrollId(builder.scrollId);
    }

    public String getScrollId() {
        return scrollId;
    }

    public void setScrollId(String scrollId) {
        this.scrollId = scrollId;
    }
}
