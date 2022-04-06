/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.io.*;
import java.util.*;
import java.nio.charset.*;


/**
 *   Processes a directory structure and indexes all PDF and text files.
 */
public class Indexer {

    /** The index to be built up by this Indexer. */
    Index index;

    /** K-gram index to be built up by this Indexer */
    KGramIndex kgIndex;

    /** The next docID to be generated. */
    private int lastDocID = 0;

    /** The patterns matching non-standard words (e-mail addresses, etc.) */
    String patterns_file;

    /* ----------------------------------------------- */


    /** Constructor */
    public Indexer( Index index, KGramIndex kgIndex, String patterns_file ) {
        this.index = index;
        this.kgIndex = kgIndex;
        this.patterns_file = patterns_file;
    }


    /** Generates a new document identifier as an integer. */
    private int generateDocID() {
        return lastDocID++;
    }



    /**
     *  Tokenizes and indexes the file @code{f}. If <code>f</code> is a directory,
     *  all its files and subdirectories are recursively processed.
     */
    public void processFiles( File f, boolean is_indexing ) {
        // do not try to index fs that cannot be read
        if (is_indexing) {
            if ( f.canRead() ) {
                if ( f.isDirectory() ) {
                    String[] fs = f.list();
                    // an IO error could occur
                    if ( fs != null ) {
                        for ( int i=0; i<fs.length; i++ ) {
                            processFiles( new File( f, fs[i] ), is_indexing );
                        }
                    }
                } else {
                    // First register the document and get a docID
                    int docID = generateDocID();
                    if ( docID%1000 == 0 ) System.err.println( "Indexed " + docID + " files" );
                    try {
                        Reader reader = new InputStreamReader( new FileInputStream(f), StandardCharsets.UTF_8 );
                        Tokenizer tok = new Tokenizer( reader, true, false, true, patterns_file );
                        int offset = 0;
                        while ( tok.hasMoreTokens() ) {
                            String token = tok.nextToken();
                            insertIntoIndex( docID, token, offset++ );
                        }
                        index.docNames.put( docID, f.getPath() );
                        index.docLengths.put( docID, offset );
                        reader.close();
                    } catch ( IOException e ) {
                        System.err.println( "Warning: IOException during indexing." );
                    }
                }
            }
        } // finished indexing
    }


    /* ----------------------------------------------- */


    /**
     *  Indexes one token.
     */
    public void insertIntoIndex( int docID, String token, int offset ) {
        // update term counter
        if (index.termCount.containsKey(docID)) {
            // document registered
            if (index.termCount.get(docID).containsKey(token)) {
                // token already registered with document
                int count = index.termCount.get(docID).get(token);
                index.termCount.get(docID).put( token, count++);
            } else {
                // token not yet in document/ first appearance of token in document
                index.termCount.get(docID).put( token, 1);
                
                if (index.termInDocCount.containsKey(token)) {
                    index.termInDocCount.put(token, index.termInDocCount.get(token)+1);    
                } else {
                    index.termInDocCount.put(token, 1);
                }
            }
        } else {
            // new entry for document; first word case
            HashMap<String,Integer> termCountMap = new HashMap<String,Integer>();
            termCountMap.put(token, 1);
            index.termCount.put(docID, termCountMap);
            if (index.termInDocCount.containsKey(token)) {
                index.termInDocCount.put(token, index.termInDocCount.get(token)+1);    
            } else {
                index.termInDocCount.put(token, 1);
            }
        }
        /*_____________________________________*/
        index.insert( token, docID, offset );
        if (kgIndex != null)
            kgIndex.insert(token);
    }

    // get idf's of words from 2.3
    public void getSpecificIdfs() {
        List<String> wordList = Arrays.asList("redirect","davis","food", "coop","residence","hall", "movein", "recycling", "drive");
        double N = index.docLengths.size();
        for (String word : wordList) {
            Integer df_t = index.termInDocCount.get(word);
            if (df_t != null) {
                System.out.println(word + ": " + Math.log(N/(double)df_t));    
            }
            
        }
    }

    // write the document euclidean length to a file, docID;eucLength
    public void computeDocLength() throws IOException {
        FileOutputStream fout = new FileOutputStream( "index/euclideanDocumentLength" );
        for (Map.Entry<Integer, HashMap<String, Integer>> docIDEntry : index.termCount.entrySet()) { // for each docID
            int docID = docIDEntry.getKey();
            double euclideanLength = 0.0;
            for (Map.Entry<String, Integer> termCount : docIDEntry.getValue().entrySet()) {
                String term = termCount.getKey();
                double tf = termCount.getValue();
                double idf_td = ( Math.log(((double)Index.docLengths.size())/index.termInDocCount.get(term)) );
                euclideanLength += Math.pow((double)tf * idf_td, 2);
            }
            euclideanLength = Math.sqrt(euclideanLength);
            String docLengthEntry = docID + ";" + euclideanLength + "\n";
            fout.write(docLengthEntry.getBytes());
            System.out.print("\r"+docID);
        }
        fout.close();
    }
}

