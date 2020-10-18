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
|:-:|:-:|:-:|:-:|
| `TextField` | yes | yes | no |
| `StringField` |  yes | no | yes |
| `LongPoint` | yes | no | no |

> 3. Does the command line demo use stopword removal? Explain how you find out the answer.

No, search `index` returns less results than `the index`.

> 4. Does the command line demo use stemming? Explain how you find out the answer.

No, `step` returns 49 results. `steps` only 19.

> 5. Is the search of the command line demo case insensitive? How did you find out the answer?

No, `index` and `INDEX` return the same number of results.

> 6.Does it matter whether stemming occurs before or after stopword removal? Consider this as a general question.

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

By default, use a `StandardAnalyzer` (lower case filter)



## 3.4 Searching

Searching for ["Information Retrieval"]
DEBUG: Parsed query : summary:"inform retriev"
11 results
Top 10 results: 
891: Everyman's Information Retrieval System (5.791674)
1457: Data Manipulation and Programming Problemsin Automatic Information Retrieval (5.791674)
1699: Experimental Evaluation of InformationRetrieval Through a Teletypewriter (5.791674)
1652: A Code for Non-numeric Information ProcessingApplications in Online Systems (4.4516068)
1935: Randomized Binary Search Technique (4.4516068)
2307: Dynamic Document Processing (4.4516068)
2451: Design of Tree Structures for Efficient Querying (4.4516068)
2516: Hierarchical Storage in Information Retrieval (4.4516068)
2519: On the Problem of Communicating Complex Information (4.4516068)
2795: Sentence Paraphrasing from a Conceptual Base (4.4516068)
Searching for [Information AND Retrieval]
DEBUG: Parsed query : +summary:inform +summary:retriev
23 results
Top 10 results: 
3134: The Use of Normal Multiplication Tablesfor Information Storage and Retrieval (6.749342)
1032: Theoretical Considerations in Information Retrieval Systems (6.5755634)
1457: Data Manipulation and Programming Problemsin Automatic Information Retrieval (6.5755634)
891: Everyman's Information Retrieval System (6.2221165)
1699: Experimental Evaluation of InformationRetrieval Through a Teletypewriter (6.2221165)
2307: Dynamic Document Processing (6.1451206)
1527: A Grammar Base Question Answering Procedure (5.7916746)
1652: A Code for Non-numeric Information ProcessingApplications in Online Systems (5.6600485)
1681: Easy English,a Language for InformationRetrieval Through a Remote Typewriter Console (5.6178956)
2990: Effective Information Retrieval Using Term Accuracy (5.6178956)
Searching for [+Retrieval Information NOT Database]
DEBUG: Parsed query : +summary:retriev summary:inform -summary:databas
54 results
Top 10 results: 
3134: The Use of Normal Multiplication Tablesfor Information Storage and Retrieval (6.749342)
1032: Theoretical Considerations in Information Retrieval Systems (6.5755634)
1457: Data Manipulation and Programming Problemsin Automatic Information Retrieval (6.5755634)
891: Everyman's Information Retrieval System (6.2221165)
1699: Experimental Evaluation of InformationRetrieval Through a Teletypewriter (6.2221165)
2307: Dynamic Document Processing (6.1451206)
1527: A Grammar Base Question Answering Procedure (5.7916746)
1652: A Code for Non-numeric Information ProcessingApplications in Online Systems (5.6600485)
1681: Easy English,a Language for InformationRetrieval Through a Remote Typewriter Console (5.6178956)
2990: Effective Information Retrieval Using Term Accuracy (5.6178956)
Searching for [Info*]
DEBUG: Parsed query : summary:info*
193 results
Top 10 results: 
222: Coding Isomorphisms (1.0)
272: A Storage Allocation Scheme for ALGOL 60 (1.0)
396: Automation of Program  Debugging (1.0)
397: A Card Format for Reference Files in Information Processing (1.0)
409: CL-1, An Environment for a Compiler (1.0)
440: Record Linkage (1.0)
483: On the Nonexistence of a Phrase Structure Grammar for ALGOL 60 (1.0)
616: An Information Algebra - Phase I Report-LanguageStructure Group of the CODASYL Development Committee (1.0)
644: A String Language for Symbol Manipulation Based on ALGOL 60 (1.0)
655: COMIT as an IR Language (1.0)
Searching for [ "Information Retrieval"~5 ]
DEBUG: Parsed query : summary:"inform retriev"~5
15 results
Top 10 results: 
891: Everyman's Information Retrieval System (5.791674)
1457: Data Manipulation and Programming Problemsin Automatic Information Retrieval (5.791674)
1699: Experimental Evaluation of InformationRetrieval Through a Teletypewriter (5.791674)
2307: Dynamic Document Processing (5.235496)
1652: A Code for Non-numeric Information ProcessingApplications in Online Systems (4.4516068)
1935: Randomized Binary Search Technique (4.4516068)
2451: Design of Tree Structures for Efficient Querying (4.4516068)
2516: Hierarchical Storage in Information Retrieval (4.4516068)
2519: On the Problem of Communicating Complex Information (4.4516068)
2795: Sentence Paraphrasing from a Conceptual Base (4.4516068)

Process finished with exit code 0
