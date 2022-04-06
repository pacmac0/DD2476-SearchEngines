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
        public void setWeight( double weight ) {
            this.weight = weight;
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
    public void relevanceFeedback ( PostingsList results, boolean[] docIsRelevant, Engine engine ) {
        //
        //  YOUR CODE HERE
        //
        double relDocCount = 0.0;

        // Rocchio algorithm (ignore irelevant case)
        HashMap<String, Double> termWeights = new HashMap<String, Double>();

        /**
         * Calculate weight for each term in the relevant documents
         */
        for (int i = 0; i < docIsRelevant.length; i++) {
            if (docIsRelevant[i]) {
                relDocCount++;
                int docID = results.get(i).docID;
                HashMap<String, Integer> doc_tfMap = engine.index.termCount.get(docID);
                int docLength = engine.index.docLengths.get(docID);
                for (Map.Entry<String, Integer> tf : doc_tfMap.entrySet()) {
                    String term = tf.getKey();
                    double weight;
                    //double idf;
                    //idf = Math.log(engine.index.docNames.size()/ (double)engine.index.termInDocCount.get(term));
                    weight = beta * (1.0 / relDocCount) * (Double.valueOf(tf.getValue()) / Double.valueOf(docLength)); // beta * scalingFactor * <weight of term in doc>

                    // (beta part)
                    if (!termWeights.containsKey(term)) {
                        termWeights.put(term, weight);
                    } else {
                        weight = weight + termWeights.get(term); // sum all entries up
                        termWeights.put(term, weight);
                    }
                }
            }
        } 
        // term weight updates (alpha part)
        for (Map.Entry<String, Double> q : termWeights.entrySet()) {
            QueryTerm qTerm = new QueryTerm(q.getKey(),1.0);
            if (queryterm.contains(qTerm)) {
                int termIdx = queryterm.indexOf(qTerm);
                queryterm.get(termIdx).setWeight(q.getValue() + alpha); // add by alpha because alpha*q_0(q_0 = 1)
            } else {
                queryterm.add(qTerm);
            }

        }  
    
    
    
    }

    public HashMap<String, Double> readDocsTfFromDocFile (int doc_id, Engine engine) throws IOException {
        HashMap<String, Double> docTF = new HashMap<String, Double>();
        File idFile = new File( "index/docInfo" );
        FileReader idReader = new FileReader(idFile);
        try (BufferedReader br = new BufferedReader(idReader)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] idTitle = line.split(";");
                if (Integer.valueOf(idTitle[0]) == doc_id) {
                    File docFile = new File(idTitle[1]);
                    // read in document
                    Reader reader = new InputStreamReader( new FileInputStream(docFile), StandardCharsets.UTF_8 );
                    Tokenizer tok = new Tokenizer( reader, true, false, true, engine.indexer.patterns_file );
                    while ( tok.hasMoreTokens() ) {
                        String term = tok.nextToken();

                        //TOD finish reading directly from file (maybe not best way, excludes terms that are not in this doc)
                        if (docTF.containsKey(term)) {
                            docTF.put(term, docTF.get(term)+1.0);
                        } else {
                            
                        }

                    }
                    reader.close();
                }
            }
        } catch (IOException e) {
            System.err.println( "ERROR: Document file not found!" );
            e.printStackTrace();
        }
        idReader.close();
        return docTF;
    }
}

