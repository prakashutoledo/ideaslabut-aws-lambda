package org.ideaslabut.aws.lambda.domain.elasticsearch.request;

@SuppressWarnings("unchecked")
public abstract class IndexableRequest extends Request {
    public static abstract class Builder<I extends IndexableRequest, B extends Builder<I, B>> extends Request.BaseBuilder<I, B> {
        protected String index;

        public B withIndex(String index)  {
            this.index = index;
            return (B) this;
        }

        @Override
        public abstract I build();
    }

    protected IndexableRequest() {
    }

    private String Index;

    public String getIndex() {
        return Index;
    }

    public void setIndex(String index) {
        Index = index;
    }
}
