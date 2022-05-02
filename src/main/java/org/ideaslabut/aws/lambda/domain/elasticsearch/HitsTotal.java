package org.ideaslabut.aws.lambda.domain.elasticsearch;

public class HitsTotal {
    private long value;

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("value=").append(value);
        sb.append('}');
        return sb.toString();
    }
}
