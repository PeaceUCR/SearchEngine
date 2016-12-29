package com;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.ConcurrentMergeScheduler;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SearchIndex {

  public static int RESULT_NUM=1;
  public static final String INDEX_DIR = "/index";

  private static final String CONTENTS = "contents";
  private static final String USER = "user";

  private static final int RESULTS_PER_PAGE = 10;

  private IndexReader indexReader;
  private IndexSearcher indexSearcher;
  private FSDirectory indexDir;
  private Analyzer analyzer;
  private String field;

  public String getField() {
    return field;
  }

  public QueryParser init() {
    field = CONTENTS;
    //http://stackoverflow.com/questions/6326228/how-to-change-system-getpropertyuser-dir-to-project-workspace
    //directly map to eclispe install space,
    final String cwd = System.getProperty("user.dir");

    try {
      indexDir = FSDirectory.open(Paths.get(cwd + INDEX_DIR));
      indexReader = DirectoryReader.open(indexDir);
      indexSearcher = new IndexSearcher(indexReader);
      indexSearcher.setSimilarity(new BM25Similarity());
      analyzer = CustomAnalyzer.builder()
          .withTokenizer("classic")
          .addTokenFilter("lowercase")
          .addTokenFilter("stop")
          .addTokenFilter("trim")
          .addTokenFilter("kstem")
          .build();
      return new QueryParser(field, analyzer);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public String spellcheck(String query) {

    IndexWriterConfig iwconfig = new IndexWriterConfig();
    iwconfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
    ConcurrentMergeScheduler mergescheduler = new ConcurrentMergeScheduler();
    iwconfig.setMergeScheduler(mergescheduler);
    Similarity rankingAlg = new BM25Similarity();
    iwconfig.setSimilarity(rankingAlg);
    iwconfig.setUseCompoundFile(true);
    LuceneDictionary dictionary = new LuceneDictionary(indexReader, field);

    SpellChecker spellChecker = null;
    StringBuilder suggestedQuery = new StringBuilder();
    try {
      spellChecker = new SpellChecker(indexDir);
      spellChecker.indexDictionary(dictionary, iwconfig, true);
      String[] individualWords = query.split("\\s");
      for (String words : individualWords) {
        String[] suggestions = new String[1];
        if (!spellChecker.exist(words)) {
          try {
            suggestions = spellChecker.suggestSimilar(words, 1);
          } catch (NegativeArraySizeException nase) {

          }
        }
        if (suggestions.length > 0 && suggestions[0] != null) {
          suggestedQuery.append(suggestions[0]);
          suggestedQuery.append(" ");
        }
      }
      if (suggestedQuery.length() != 0) {
        return suggestedQuery.toString();
      } else {
        return query;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return query;
  }

  public List<String> search(final Query query, String s) {
	  
	  return search(query,s,0);
   }
  
  public List<String> search(final Query query, String s, int pagestart) {
	    //ExecutorService executor = new Executors.newFixedThreadPool(LuceneIndex.THREAD_COUNT);
		  List<String> r = new ArrayList<String>();
		  try{
			  
		      TopDocs topDocs = indexSearcher.search(query, 10 * RESULTS_PER_PAGE);
		      ScoreDoc[] results = topDocs.scoreDocs;

		      int numTotalResult = topDocs.totalHits;
		      RESULT_NUM = numTotalResult;
		      System.out.println(numTotalResult + " total results found.");
		      int pageStart = pagestart;
		      int pageEnd = Math.min(numTotalResult, RESULTS_PER_PAGE);
		      
		      results = indexSearcher.search(query, numTotalResult).scoreDocs;
		     
		      pageEnd = Math.min(results.length, pageStart + RESULTS_PER_PAGE);

		      for (int i=pageStart; i < pageEnd; i++) {
		          Document document = indexSearcher.doc(results[i].doc);
		          StringBuilder stringBuilder = new StringBuilder((i+1) + ". ");
		          String[] users = document.getValues("user");
		          for (int j=0; j < users.length; j++) {
		            stringBuilder.append("(" + users[j] + ") ");
		            if (j == users.length - 1) {
		              stringBuilder.append("- ");
		            }
		          }
			       String content = document.get("contents");
			         if (content != null) {
			           stringBuilder.append(content);
			         }
			       System.out.println(stringBuilder.toString());
			       r.add(stringBuilder.toString());
		       }
		      
		  }catch(IOException e){
			  e.printStackTrace();
		  }
		  

		  return r;


	   }
  public static int getResultNum(){
	  return RESULT_NUM;
  }
  
  public void finish() {
    try {
      indexReader.close();
      indexDir.close();
      analyzer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
