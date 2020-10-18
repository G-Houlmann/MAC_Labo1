# MAC - Labo 1

Authors :

- Gildas HOULMANN
- Thibaud FRANCHETTI

## 2.1 Understanding the Lucene API

> 1. What are the types of the fields in the index?

`TextField` (contents), `StringField` (path) and `LongPoint` (modified)

> 2. What are the characteristics of each type of field in terms of indexing, storage and tokenization?

In this case (some fields can be stored or not), we have:

| | Indexed | Stored | Tokenized |
|:-|:-:|:-:|:-:|
| `TextField` | yes | yes | no |
| `StringField` |  yes | no | yes |
| `LongPoint` | yes | no | no |

> 3. Does the command line demo use stopword removal? Explain how you find out the answer.

No, search `index` returns less results than `the index`.

> 4. Does the command line demo use stemming? Explain how you find out the answer.

No, `step` returns 49 results. `steps` only 19.

> 5. Is the search of the command line demo case insensitive? How did you find out the answer?

No, `index` and `INDEX` return the same number of results.

> 6. Does it matter whether stemming occurs before or after stopword removal? Consider this as a general question.

The order should not have any incidence on the results, however processing the stemming before stopwords removal would be wasting time and resources on those stopwords that will be removed anyway.

## 3.1 Indexing

> 1. Find out what is a “term vector” in Lucene vocabulary?

A term vector is a list of all terms in a field for a given document, possibly with their frequency, positions and offsets

> 2. What should be added to the code to have access to the “term vector” in the index? Have a look at the different methods of the `class FieldType`. Use *Luke* to check that the “term vector” is included in the
index.

The following methods must be called on our custom `FieldType`:

``` Java
    summaryFieldType.setStoreTermVectors(true);
    summaryFieldType.setStoreTermVectorPositions(true);
    summaryFieldType.setStoreTermVectorOffsets(true);
```

> 3. Compare the size of the index before and after enabling "term vector", discuss the results.

The size of the index before and after enabling "term vector" is the following (all fields saved) :

| Before | After |
| :-: | :-: |
| 1.7 Mo | 2.5 Mo |

As expected, it takes more space after than before. This is quite logical since we add data in the index.

### Code of indexing


``` Java
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
            doc.add(new StringField(AUTHORS_FIELD, author, Field.Store.YES)
        );
}

// Add title
if (title != null && !title.isEmpty()) doc.add(new TextField(TITLE_FIELD, title, Field.Store.YES));

// Add summary
if (summary != null && !summary.isEmpty()) doc.add(new Field(SUMMARY_FIELD, summary, summaryFieldType));
```

## 3.2 Using different Analyzers

> 2. Explain the difference of these five analyzers. 

`WhitespaceAnalyzer`

Trivial analyzer. The text is divided at every whitespace, no additional treatment is done.

`EnglishAnalyzer`

Common stopwords from the English language are removed (the list could have been provided but in our case we used the default one), some treatment is done (lower case, removing possessive `'s`) on the tokens before.

`ShingleAnalyzerWrapper`

Allows to consider n-grams (`n` between two bounds `min` and `max`, both included, given as argument) as tokens in addition to the unigrams. N-grams are built with adjacent words.

By default, used in combination with a `StandardAnalyzer` (lower case filter).

`StopAnalyzer`

Apply a lower case filter and remove all words from the given file.

> 3. Look at the index using Luke and for each created index find out the following information:
>    1. The number of indexed documents and indexed terms.
>    2. The number of indexed terms in the summary field.
>    3. The top 10 frequent terms of the summary field in the index.
>    4. The size of the index on disk.
>    5. The required time for indexing (e.g. using `System currentTimeMillis()` before and after the indexing).

General data:

| | Nb documents | Nb terms | Index size | Indexing time |
| :- | :-: |:-: |:-: |:-: |
| `WhitespaceAnalyzer` | 3203 | 34826 | 2.7 Mo | 1170 ms |
| `EnglishAnalyzer` | 3203 | 23009 | 2.2 Mo | 1386 ms |
| `ShingleAnalyzerWrapper 1&2` | 3203 | 119845 | 5.2 Mo | 2826 ms | 
| `ShingleAnalyzerWrapper 1&3` | 3203 | 158875 | 6.5 Mo | 3504 ms |
| `StopAnalyzer` | 3203 | 24662 | 2.1 Mo | 1322 ms |

Top 10 frequent terms for the summary field:

|| `Whitespace` | `English` | `Shingle 1&2` | `Shingle 1&3` | `StopAnalyzer` |
| :- |:- |:- |:- |:- |:- |
| 1 |of | us| the | the | system|
| 2 | the| which| of| of | computer|
| 3 |is | comput| a|a | paper|
| 4 | a| program | is| is| presented |
| 5 |and |system | and|and |  time|
| 6 | to| present| to|to | method |
| 7 | in| describ| in|in | program |
| 8 | for| paper| for |for | data |
| 9 | The| method| are| are| algorithm |
| 10 | are| can| of the| this| discussed |

> 4. Make 3 concluding statements bases on the above observations.

1. The `EnglishAnalyzer` obviously uses stemming, the others probably don't (it's certain for the `StopAnalyzer` since *computer* isn't stemmed as *comput* but unclear for the others, although the previous description suggest they don't either).
2. The best performances are achieved by the stopwords-based strategies (i.e. `EnglishAnalyzer` and `StopAnalyzer`), they also produce the most relevant tokens. A trivial analyzer like the `WhitespaceAnalyzer` is totally 
3. Shingle-based strategies can *really* quickly become time and resources consuming with the growth of the shingle size, so we should carefully think if we need it or not.
By default, use a `StandardAnalyzer` (lower case filter)

## 3.3 Reading Index
> 1. What is the author with the highest number of publications? How many publications does he/she have?

The author with the highest number of publications is `Thacher Jr., H. C.` , with 38 publications

> 2. List the top 10 terms in the title field with their frequency.

When using the standard analyzer, we obtained these results: 

| term | Frequency |
| :- | :-: |
|of | 1215 |
|algorithm | 983 |
|a | 963 |
|for | 694 |
|the | 675 |
|and | 425 |
|in | 387 |
|on | 338 |
|an | 278 |
|computer | 262 |  

Code used to obtain these results:
``` Java
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
```

## 3.4 Searching

### Code written

``` Java
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
```

> 1. Searching for publications containing the term “Information Retrieval”.

Parsed query : `summary:"inform retriev"`  
11 results

Top 10 results:

| id | title | score |
| :-: | :- | :-: |
| 891 | Everyman's Information Retrieval System | 5.791674 |
|1457 | Data Manipulation and Programming Problemsin Automatic Information Retrieval |5.791674|
|1699 | Experimental Evaluation of InformationRetrieval Through a Teletypewriter |5.791674|
|1652 | A Code for Non-numeric Information ProcessingApplications in Online Systems |4.4516068|
|1935 | Randomized Binary Search Technique |4.4516068|
|2307 | Dynamic Document Processing |4.4516068|
|2451 | Design of Tree Structures for Efficient Querying |4.4516068|
|2516 | Hierarchical Storage in Information Retrieval |4.4516068|
|2519 | On the Problem of Communicating Complex Information |4.4516068|
|2795 | Sentence Paraphrasing from a Conceptual Base |4.4516068|  


> 2. Searching for publications containing both “Information” and “Retrieval”.
> 
Parsed query : `+summary:inform +summary:retriev`  
23 results  

Top 10 results: 

| id | title | score |
| :-: | :- | :-: |
|3134 | The Use of Normal Multiplication Tablesfor Information Storage and Retrieval |6.749342|
|1032 | Theoretical Considerations in Information Retrieval Systems |6.5755634|
|1457 | Data Manipulation and Programming Problemsin Automatic Information Retrieval |6.5755634|
|891 | Everyman's Information Retrieval System |6.2221165|
|1699 | Experimental Evaluation of InformationRetrieval Through a Teletypewriter |6.2221165|
|2307 | Dynamic Document Processing |6.1451206|
|1527 | A Grammar Base Question Answering Procedure |5.7916746|
|1652 | A Code for Non-numeric Information ProcessingApplications in Online Systems |5.6600485|
|1681 | Easy English,a Language for InformationRetrieval Through a Remote Typewriter Console |5.6178956|
|2990 | Effective Information Retrieval Using Term Accuracy |5.6178956|

> 3. Searching for publications containing at least the term “Retrieval” and, possibly “Information” but not “Database”.

Parsed query : `+summary:retriev summary:inform -summary:databas`  
54 results  

Top 10 results: 

| id | title | score |
| :-: | :- | :-: |
|3134 | The Use of Normal Multiplication Tablesfor Information Storage and Retrieval |6.749342|
|1032 | Theoretical Considerations in Information Retrieval Systems |6.5755634|
|1457 | Data Manipulation and Programming Problemsin Automatic Information Retrieval |6.5755634|
|891 | Everyman's Information Retrieval System |6.2221165|
|1699 | Experimental Evaluation of InformationRetrieval Through a Teletypewriter |6.2221165|
|2307 | Dynamic Document Processing |6.1451206|
|1527 | A Grammar Base Question Answering Procedure |5.7916746|
|1652 | A Code for Non-numeric Information ProcessingApplications in Online Systems |5.6600485|
|1681 | Easy English,a Language for InformationRetrieval Through a Remote Typewriter Console |5.6178956|
|2990 | Effective Information Retrieval Using Term Accuracy |5.6178956|

> 4. Searching for publications containing a term starting with “Info”.

Parsed query : `summary:info*`  
193 results

Top 10 results: 

| id | title | score |
| :-: | :- | :-: |
|222 | Coding Isomorphisms |1.0|
|272 | A Storage Allocation Scheme for ALGOL 60 |1.0|
|396 | Automation of Program  Debugging |1.0|
|397 | A Card Format for Reference Files in Information Processing |1.0|
|409 | CL-1, An Environment for a Compiler |1.0|
|440 | Record Linkage |1.0|
|483 | On the Nonexistence of a Phrase Structure Grammar for ALGOL 60 |1.0|
|616 | An Information Algebra - Phase I Report-LanguageStructure Group of the CODASYL Development Committee |1.0|
|644 | A String Language for Symbol Manipulation Based on ALGOL 60 |1.0|
|655 | COMIT as an IR Language |1.0|

> 5. Searching for publications containing the term “Information” close to “Retrieval” (max distance 5).

Parsed query : `summary:"inform retriev"~5`  
15 results

Top 10 results: 

| id | title | score |
| :-: | :- | :-: |
|891 | Everyman's Information Retrieval System |5.791674|
|1457 | Data Manipulation and Programming Problemsin Automatic Information Retrieval |5.791674|
|1699 | Experimental Evaluation of InformationRetrieval Through a Teletypewriter |5.791674|
|2307 | Dynamic Document Processing |5.235496|
|1652 | A Code for Non-numeric Information ProcessingApplications in Online Systems |4.4516068|
|1935 | Randomized Binary Search Technique |4.4516068|
|2451 | Design of Tree Structures for Efficient Querying |4.4516068|
|2516 | Hierarchical Storage in Information Retrieval |4.4516068|
|2519 | On the Problem of Communicating Complex Information |4.4516068|
|2795 | Sentence Paraphrasing from a Conceptual Base |4.4516068|

