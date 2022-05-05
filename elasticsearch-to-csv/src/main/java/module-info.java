module ideaslabut.aws.lambda.elasticsearch.to.csv {
    requires ideaslabut.aws.lambda.core;
    requires ideaslabut.aws.lambda.extractor.util;

    requires org.slf4j;
    requires software.amazon.awssdk.core;
    requires software.amazon.awssdk.services.apigatewaymanagementapi;
    requires software.amazon.awssdk.regions;

    uses org.ideaslabut.aws.lambda.domain.elasticsearch.Response;
    uses org.ideaslabut.aws.lambda.domain.elasticsearch.SourceHits;
    uses org.ideaslabut.aws.lambda.domain.elasticsearch.request.SearchRequest;
    uses org.ideaslabut.aws.lambda.extractor.util.CSVWriter;
    uses org.ideaslabut.aws.lambda.extractor.util.ProgressBar;
    uses org.ideaslabut.aws.lambda.service.ElasticsearchService;
    uses org.slf4j.Logger;
    uses org.slf4j.LoggerFactory;
    uses java.net.http.HttpResponse;
}