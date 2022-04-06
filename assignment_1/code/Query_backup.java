/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.util.ArrayList;
import java.util.*;
import java.util.StringTokenizer;
import java.util.Iterator;
import java.nio.charset.*;
import java.io.*;


/**
 *  A class for representing a query as a list of words, each of which has
 *  an associated weight.
 */
public class Query {

    /**
     *  Help class to represent one query term, with its associated weight. 
     */
    class QueryTerm {
        String term;
        double weight;
        QueryTerm( String t, double w ) {
            term = t;
            weight = w;
        }
    }

    /** 
     *  Representation of the query as a list of terms with associated weights.
     *  In assignments 1 and 2, the weight of each term will always be 1.
     */
    public ArrayList<QueryTerm> queryterm = new ArrayList<QueryTerm>();

    /**  
     *  Relevance feedback constant alpha (= weight of original query terms). 
     *  Should be between 0 and 1.
     *  (only used in assignment 3).
     */
    double alpha = 0.2;

    /**  
     *  Relevance feedback constant beta (= weight of query terms obtained by
     *  feedback from the user). 
     *  (only used in assignment 3).
     */
    double beta = 1 - alpha;
    
    
    /**
     *  Creates a new empty Query 
     */
    public Query() {
    }
    
    
    /**
     *  Creates a new Query from a string of words
     */
    public Query( String queryString  ) {
        StringTokenizer tok = new StringTokenizer( queryString );
        while ( tok.hasMoreTokens() ) {
            queryterm.add( new QueryTerm(tok.nextToken(), 1.0) );
        }    
    }
    
    
    /**
     *  Returns the number of terms
     */
    public int size() {
        return queryterm.size();
    }
    
    
    /**
     *  Returns the Manhattan query length
     */
    public double length() {
        double len = 0;
        for ( QueryTerm t : queryterm ) {
            len += t.weight; 
        }
        return len;
    }
    
    
    /**
     *  Returns a copy of the Query
     */
    public Query copy() {
        Query queryCopy = new Query();
        for ( QueryTerm t : queryterm ) {
            queryCopy.queryterm.add( new QueryTerm(t.term, t.weight) );
        }
        return queryCopy;
    }
    
    
    /**
     *  Expands the Query using Relevance Feedback
     *
     *  @param results The results of the previous query.
     *  @param docIsRelevant A boolean array representing which query results the user deemed relevant.
     *  @param engine The search engine object
     */
    public void relevanceFeedback ( PostingsList results, boolean[] docIsRelevant, Engine engine ) throws IOException {
        //
        //  YOUR CODE HERE
        //

        HashMap<String, Double> mu_r = new HashMap<String, Double>();
        HashMap<String, Double> mu_nr = new HashMap<String, Double>();
        double relDocCount = 0.0;
        for (int i = 0; i < docIsRelevant.length; i++) {
            relDocCount++;
        }
        double nonrelDocCount = docIsRelevant.length - relDocCount;

        // summing over all document vectors
        for (int i = 0; i < results.size(); i++) {
            int doc_id = results.get(i).docID;
            // get file path
            File idFile = new File( "index/docInfo" );
            FileReader idReader = new FileReader(idFile);
            try (BufferedReader br = new BufferedReader(idReader)) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] idTitle = line.split(";");
                    if (Integer.valueOf(idTitle[1]) != doc_id) {
                        File docFile = new File(idTitle[1]);
                        // read in document
                        Reader reader = new InputStreamReader( new FileInputStream(docFile), StandardCharsets.UTF_8 );
                        Tokenizer tok = new Tokenizer( reader, true, false, true, engine.indexer.patterns_file );
                        while ( tok.hasMoreTokens() ) {
                            String term = tok.nextToken();
                            

                            if (docIsRelevant[i]) { // releveant doc
                                if (mu_r.containsKey(term)) { // coordinate exists
                                    mu_r.put(term, mu_r.get(term)+1.0); // summing over all doc vectors
                                } else { // coordinate doesnt exist
                                    mu_r.put(term, 1.0);
                                }
                                if (!mu_nr.containsKey(term)) {
                                    mu_nr.put(term, 0.0);
                                }
                            }
                            else{
                                if (mu_nr.containsKey(term)) { // coordinate exists
                                    mu_nr.put(term, mu_nr.get(term)+1.0); // summing over all doc vectors
                                } else { // coordinate doesnt exist
                                    mu_nr.put(term, 1.0);
                                }
                                if (!mu_r.containsKey(term)) {
                                    mu_r.put(term, 0.0);
                                }
                            }

                        }
                        reader.close();
                    }
                }
                // file not found
            } catch (IOException e) {
                System.err.println( "ERROR: Document file not found!" );
                e.printStackTrace();
            }
            idReader.close();
            // check docCounts
            if (relDocCount == 0.0 || nonrelDocCount == 0.0) {
                relDocCount = 1.0;
                nonrelDocCount = 1.0;
                
            }
            // Rocchio algorithm!! TODO
            HashMap<String,Double> Q = new HashMap<String, Double>();
            for (Map.Entry<String, Double> muREntry : mu_r.entrySet()) { // to keep the order!!
                String termDim = muREntry.getKey();
                double R = (mu_r.get(termDim)/relDocCount);
                double NR = (mu_nr.get(termDim)/nonrelDocCount);
                Q.put(termDim, (2*R) - NR);
            }














        

            
            

            
        }
    }
}

