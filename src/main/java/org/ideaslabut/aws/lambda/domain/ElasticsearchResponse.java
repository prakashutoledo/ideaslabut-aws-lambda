package org.ideaslabut.aws.lambda.domain;

public class ElasticsearchResponse {
    private ElasticsearchHits hits;

    public ElasticsearchHits getHits() {
        return hits;
    }

    public void setHits(ElasticsearchHits hits) {
        this.hits = hits;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("hits=").append(hits);
        sb.append('}');
        return sb.toString();
    }
}
