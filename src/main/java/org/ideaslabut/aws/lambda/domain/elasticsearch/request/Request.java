package org.ideaslabut.aws.lambda.domain.elasticsearch.request;

import java.net.http.HttpResponse;
import java.util.function.Consumer;

public abstract class Request {
    @SuppressWarnings("unchecked")
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

    protected Request() {
    }

    protected Request(BaseBuilder baseBuilder) {
        setErrorConsumer(baseBuilder.errorConsumer);
        setSuccessConsumer(baseBuilder.successConsumer);
        setExceptionConsumer(baseBuilder.exceptionConsumer);
    }

    private Consumer<Exception> exceptionConsumer;
    private Consumer<HttpResponse<String>> errorConsumer;
    private Consumer<HttpResponse<String>> successConsumer;

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
}
