package org.ideaslabut.aws.lambda.domain.elasticsearch.request;

public class SearchRequest extends Scrollable {
    public static class Builder extends Scrollable.Builder<SearchRequest, Builder> {
        private long size;

        private Builder() {
            super();
            this.size = 10;
        }

        public Builder withSize(long size) {
            this.size = size;
            return this;
        }

        @Override
        public SearchRequest build() {
            return new SearchRequest(this);
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    private long size;

    private SearchRequest(Builder builder) {
        super(builder);
        setSize(builder.size);
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
