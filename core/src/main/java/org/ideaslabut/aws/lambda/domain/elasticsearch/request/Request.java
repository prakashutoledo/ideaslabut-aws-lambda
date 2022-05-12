package org.ideaslabut.aws.lambda.domain.elasticsearch.request;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.net.http.HttpResponse;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public abstract class Request {
    protected static abstract class BaseBuilder<R extends Request, B extends BaseBuilder<R, B>> {
        protected Consumer<Exception> exceptionConsumer;
        protected Consumer<HttpResponse<String>> errorConsumer;
        protected Consumer<HttpResponse<String>> successConsumer;

        protected BaseBuilder() {
        }

        public B onException(Consumer<Exception> exceptionConsumer) {
            this.exceptionConsumer = exceptionConsumer;
            return (B) this;
        }

        public B onHttpError(Consumer<HttpResponse<String>> errorConsumer) {
            this.errorConsumer = errorConsumer;
            return (B) this;
        }

        public B onHttpSuccess(Consumer<HttpResponse<String>> successConsumer) {
            this.successConsumer = successConsumer;
            return (B) this;
        }

        public abstract R build();
    }

    private Consumer<Exception> exceptionConsumer;
    private Consumer<HttpResponse<String>> errorConsumer;
    private Consumer<HttpResponse<String>> successConsumer;
    
    protected Request() {
    }

    protected Request(BaseBuilder<?, ?> baseBuilder) {
        setErrorConsumer(baseBuilder.errorConsumer);
        setSuccessConsumer(baseBuilder.successConsumer);
        setExceptionConsumer(baseBuilder.exceptionConsumer);
    }

    public Consumer<Exception> getExceptionConsumer() {
        return exceptionConsumer;
    }

    public void setExceptionConsumer(Consumer<Exception> exceptionConsumer) {
        this.exceptionConsumer = exceptionConsumer;
    }

    public Consumer<HttpResponse<String>> getErrorConsumer() {
        return errorConsumer;
    }

    public void setErrorConsumer(Consumer<HttpResponse<String>> errorConsumer) {
        this.errorConsumer = errorConsumer;
    }

    public Consumer<HttpResponse<String>> getSuccessConsumer() {
        return successConsumer;
    }

    public void setSuccessConsumer(Consumer<HttpResponse<String>> successConsumer) {
        this.successConsumer = successConsumer;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
    }
}