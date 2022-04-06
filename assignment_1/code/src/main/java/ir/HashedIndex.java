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
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {


    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();


    /**
     *  Inserts this token in the hashtable.
     */
    public void insert( String token, int docID, int offset ) {
        //
        // YOUR CODE HERE
        //

        if (index.containsKey(token)) {
            // add docID to postingslist
            PostingsList tokens_postingsList = index.get(token);
            // check if entry (Document) is already contained
            PostingsEntry check_entry = tokens_postingsList.find(docID);
            if (check_entry == null) {
                // does not exist already, so new entry gets created and added to tokens list of entries (words list of documents)
                PostingsEntry new_entry = new PostingsEntry(docID, 0);
                new_entry.addOffset(offset);
                tokens_postingsList.add(new_entry);
            } else {
                // doc already connected to token, add offset to entry
                check_entry.addOffset(offset);
            }
        } else {
            // create new list with docID as item and add to hashmap
            PostingsList new_postingsList = new PostingsList();
            PostingsEntry new_entry = new PostingsEntry(docID);
            new_entry.addOffset(offset);
            new_postingsList.add(new_entry);
            index.put(token, new_postingsList);
        }
    }


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
        //
        // REPLACE THE STATEMENT BELOW WITH YOUR CODE
        //
        if (index.containsKey(token)) {
            return index.get(token);
        }
        return null;
    }


    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
        
    }
}
