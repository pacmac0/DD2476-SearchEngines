/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.util.HashMap;
import java.util.Iterator;

/**
 *  Defines some common data structures and methods that all types of
 *  index should implement.
 */
public interface Index {

    /** Mapping from document identifiers to document names. */
    public HashMap<Integer,String> docNames = new HashMap<Integer,String>();
    
    /** Mapping from document identifier to document length. */
    public HashMap<Integer,Integer> docLengths = new HashMap<Integer,Integer>();

    //keep term count per document docID->term->count TODO make persistent, write to file
    HashMap<Integer, HashMap<String,Integer>> termCount = new HashMap<Integer, HashMap<String,Integer>>(); // how often a term is in a specific document
    HashMap<String,Integer> termInDocCount = new HashMap<String,Integer>(); // how often a term is in corpus

    /** Inserts a token into the index. */
    public void insert( String token, int docID, int offset );

    /** Returns the postings for a given term. */
    public PostingsList getPostings( String token );

    /** This method is called on exit. */
    public void cleanup();

}

