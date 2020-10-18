package ch.heigvd.iict.dmg.labo1.queries;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.misc.HighFreqTerms;
import org.apache.lucene.misc.TermStats;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
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

		try {
			//Obtain the terms statistics
			HighFreqTerms.TotalTermFreqComparator cmp = new HighFreqTerms.TotalTermFreqComparator();
			TermStats[] statsTable = HighFreqTerms.getHighFreqTerms(indexReader, numTerms, field, cmp);

			//print
			System.out.println("Top ranking terms for field ["  + field +"] are: ");
			for (TermStats stats : statsTable){
				System.out.println(new String(stats.termtext.bytes) + ", " + stats.totalTermFreq + " occurrences");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void query(String q) {
		// See "Searching" section
		try {
			System.out.println("Searching for [" + q +"]");

			//Parse the query
			QueryParser parser = new QueryParser("summary", analyzer);
			Query query = parser.parse(q);

			//obtain the hits
			ScoreDoc[] hits = indexSearcher.search(query, indexReader.maxDoc()).scoreDocs;
			int nbHits = hits.length;

			//print
			System.out.println(nbHits + " results");
			System.out.println("Top 10 results: ");
			for(int i = 0; i < 10; ++i){
				Document doc = indexSearcher.doc(hits[i].doc);
				System.out.println(doc.get("id") + ": " + doc.get("title") + " (" +
						hits[i].score + ")");
			}



		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
	}
	 
	public void close() {
		if(this.indexReader != null)
			try { this.indexReader.close(); } catch(IOException e) { /* BEST EFFORT */ }
	}
	
}
