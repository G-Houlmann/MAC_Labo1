package ch.heigvd.iict.dmg.labo1.indexer;

import ch.heigvd.iict.dmg.labo1.parsers.ParserListener;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Arrays;

public class CACMIndexer implements ParserListener {

	private static String AUTHOR_SEP = ";";

	private static String ID_FIELD = "id";
	private static String AUTHORS_FIELD = "authors";
	private static String TITLE_FIELD = "title";
	private static String SUMMARY_FIELD = "summary";
	
	private Directory 	dir 			= null;
	private IndexWriter indexWriter 	= null;
	
	private Analyzer 	analyzer 		= null;
	private Similarity 	similarity 		= null;
	
	public CACMIndexer(Analyzer analyzer, Similarity similarity) {
		this.analyzer = analyzer;
		this.similarity = similarity;
	}
	
	public void openIndex() {
		// 1.2. create an index writer config
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		iwc.setOpenMode(OpenMode.CREATE); // create and replace existing index
		iwc.setUseCompoundFile(false); // not pack newly written segments in a compound file: 
		//keep all segments of index separately on disk
		if(similarity != null)
			iwc.setSimilarity(similarity);
		// 1.3. create index writer
		Path path = FileSystems.getDefault().getPath("index");
		try {
			this.dir = FSDirectory.open(path);
			this.indexWriter = new IndexWriter(dir, iwc);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onNewDocument(Long id, String authors, String title, String summary) {
		Document doc = new Document();

		// Field type for the summary
		FieldType summaryFieldType = new FieldType();
		summaryFieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
		summaryFieldType.setTokenized(true);
		summaryFieldType.setStored(true);
		summaryFieldType.setStoreTermVectors(true);
		summaryFieldType.setStoreTermVectorPositions(true);
		summaryFieldType.setStoreTermVectorOffsets(true);
		summaryFieldType.freeze();

		// Add id
		doc.add(new LongPoint(ID_FIELD, id));
		doc.add(new StoredField(ID_FIELD, id));

		// Add all authors if not null
		if (authors != null && !authors.isEmpty()) {
			Arrays.stream(authors.split(AUTHOR_SEP)).forEach(author ->
					doc.add(new StringField(AUTHORS_FIELD, author, Field.Store.YES)));
		}

		// Add title
		if (title != null && !title.isEmpty()) doc.add(new TextField(TITLE_FIELD, title, Field.Store.YES));

		// Add summary
		if (summary != null && !summary.isEmpty()) doc.add(new Field(SUMMARY_FIELD, summary, summaryFieldType));

		try {
			this.indexWriter.addDocument(doc);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void finalizeIndex() {
		if(this.indexWriter != null)
			try { this.indexWriter.close(); } catch(IOException e) { /* BEST EFFORT */ }
		if(this.dir != null)
			try { this.dir.close(); } catch(IOException e) { /* BEST EFFORT */ }
	}
	
}
