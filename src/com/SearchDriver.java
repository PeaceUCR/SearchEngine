package com;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class SearchDriver {
  public static void main(String[] args) {

	  List<String> r =search("trump",10);
	  System.out.println("---------");
	  for(int i=0;i<r.size();i++){
		  System.out.println(r.get(i));
	  }
  }
  
  public static List<String> search(String s,int start){
	    SearchIndex searchIndex = new SearchIndex();
	    QueryParser queryParser = searchIndex.init();
	    
	    String userQuery = s;
	    try {
	        
	        Query query = queryParser.parse(userQuery);
	        String parsedQuery = "";
	        if (query != null) {
	          parsedQuery = query.toString(searchIndex.getField());
	        }
	        System.out.println("Searching for " + userQuery);
	        String suggestions = searchIndex.spellcheck(parsedQuery);

	        if (suggestions.equals(parsedQuery)) {
	          return searchIndex.search(query, userQuery,start);
	        } else {
	          System.out.println("Auto-correcting to " + suggestions);
	          return searchIndex.search(queryParser.parse(suggestions), userQuery,start);
	        }

	      } catch (ParseException pe){
	    	  	pe.printStackTrace();
	      }
	    
		searchIndex.finish();
		return null;
  }
}


