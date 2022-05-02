package org.ideaslabut.aws.lambda.domain.elasticsearch;

import java.util.List;

/**
 * Elasticsearch hits holding webSocket hits value
 *
 * @author Prakash Khadka <br>
 *         Created on: Jan 30, 2022
 */
public class Hits {
    private HitsTotal total;
    private List<SourceHits> hits;

    public HitsTotal getTotal() {
        return total;
    }

    public void setTotal(HitsTotal total) {
        this.total = total;
    }

    public List<SourceHits> getHits() {
        return hits;
    }

    public void setHits(List<SourceHits> hits) {
        this.hits = hits;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("total=").append(total);
        sb.append(", hits=").append(hits);
        sb.append('}');
        return sb.toString();
    }
}
