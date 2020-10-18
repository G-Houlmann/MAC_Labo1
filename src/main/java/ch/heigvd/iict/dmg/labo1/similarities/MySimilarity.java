package ch.heigvd.iict.dmg.labo1.similarities;

import org.apache.lucene.search.similarities.ClassicSimilarity;

public class MySimilarity extends ClassicSimilarity {

    @Override
    public float tf(float freq) {
        return (float) (1 + Math.log10(freq));
    }

    @Override
    public float idf(long docFreq, long docCount) {
        return (float) (Math.log10(docCount / (docFreq + 1.0)) + 1);
    }

    @Override
    public float lengthNorm(int numTerms) {
        return 1;
    }
}
