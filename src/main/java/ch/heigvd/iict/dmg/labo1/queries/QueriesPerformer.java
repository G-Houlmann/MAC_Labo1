package ch.heigvd.iict.dmg.labo1.queries;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.misc.HighFreqTerms;
import org.apache.lucene.misc.TermStats;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Comparator;

public class QueriesPerformer {
	
	private Analyzer		analyzer		= null;
	private IndexReader 	indexReader 	= null;
	private IndexSearcher 	indexSearcher 	= null;

	public QueriesPerformer(Analyzer analyzer, Similarity similarity) {
		this.analyzer = analyzer;
		Path path = FileSystems.getDefault().getPath("index");
		Directory dir;
		try {
			dir = FSDirectory.open(path);
			this.indexReader = DirectoryReader.open(dir);
			this.indexSearcher = new IndexSearcher(indexReader);
			if(similarity != null)
				this.indexSearcher.setSimilarity(similarity);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void printTopRankingTerms(String field, int numTerms) {
		// This methods print the top ranking term for a field.
		// See "Reading Index".
		HighFreqTerms.TotalTermFreqComparator cmp = new HighFreqTerms.TotalTermFreqComparator();
		try {
			TermStats[] statsTable = HighFreqTerms.getHighFreqTerms(indexReader, numTerms, field, cmp);
			System.out.println("Top ranking terms for field ["  + field +"] are: ");
			for (TermStats stats : statsTable){
				System.out.println(new String(stats.termtext.bytes) + ", " + stats.totalTermFreq + " occurences");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void query(String q) {
		// TODO student
		// See "Searching" section

		System.out.println("Searching for [" + q +"]");
	}
	 
	public void close() {
		if(this.indexReader != null)
			try { this.indexReader.close(); } catch(IOException e) { /* BEST EFFORT */ }
	}
	
}
