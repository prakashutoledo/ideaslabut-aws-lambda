/*
 * Copyright 2022 IDEAS Lab @ UT. All rights reserved.
 */
package org.ideaslabut.aws.lambda.domain.elasticsearch.request;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.net.http.HttpResponse;
import java.util.function.Consumer;

/**
 * A base abstract class represented as an Elasticsearch request
 *
 * @author Prakash Khadka <br>
 *     Created On: Jun 27, 2022
 */
public abstract class Request {

    /**
     * A base builder class for {@link Request}
     *
     * @param <R> a type of request to build
     * @param <B> a type builder
     */
    @SuppressWarnings("unchecked")
    protected static abstract class BaseBuilder<R extends Request, B extends BaseBuilder<R, B>> {
        protected Consumer<Exception> exceptionConsumer;
        protected Consumer<HttpResponse<String>> errorConsumer;
        protected Consumer<HttpResponse<String>> successConsumer;

        /**
         * Creates a new instance of {@link BaseBuilder}
         */
        protected BaseBuilder() {
        }

        /**
         * Sets the exception consumer for this request
         *
         * @param exceptionConsumer a consumer which handles exception for the request
         *
         * @return a reference of this builder {@link B}
         */
        public B onException(Consumer<Exception> exceptionConsumer) {
            this.exceptionConsumer = exceptionConsumer;
            return (B) this;
        }

        /**
         * Sets the error consumer for this request builder
         *
         * @param errorConsumer a consumer which handles error on performed request
         *
         * @return a reference of this builder {@link B}
         */
        public B onHttpError(Consumer<HttpResponse<String>> errorConsumer) {
            this.errorConsumer = errorConsumer;
            return (B) this;
        }

        /**
         * Sets the success consumer for this request builder
         *
         * @param successConsumer a consumer which handles success on performed request
         *
         * @return a reference of this builder {@link B}
         */
        public B onHttpSuccess(Consumer<HttpResponse<String>> successConsumer) {
            this.successConsumer = successConsumer;
            return (B) this;
        }

        /**
         * Builds a new instance of elasticsearch request {@link R}
         *
         * @return a newly created elasticsearch request {@link R}
         */
        public abstract R build();
    }

    private Consumer<Exception> exceptionConsumer;
    private Consumer<HttpResponse<String>> errorConsumer;
    private Consumer<HttpResponse<String>> successConsumer;

    /**
     * Creates a new instance of elasticsearch request {@link Request}
     *
     * @param baseBuilder an elasticsearch builder to create request
     */
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
