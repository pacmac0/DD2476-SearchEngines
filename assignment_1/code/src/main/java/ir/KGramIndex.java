/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Dmytro Kalpakchi, 2018
 */

package ir;

import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;


public class KGramIndex {

    /** Mapping from term ids to actual term strings */
    HashMap<Integer,String> id2term = new HashMap<Integer,String>();

    /** Mapping from term strings to term ids */
    HashMap<String,Integer> term2id = new HashMap<String,Integer>();

    /** Index from k-grams to list of term ids that contain the k-gram */
    HashMap<String,List<KGramPostingsEntry>> index = new HashMap<String,List<KGramPostingsEntry>>();

    /** The ID of the last processed term */
    int lastTermID = -1;

    /** Number of symbols to form a K-gram */
    int K = 3;

    public KGramIndex(int k) {
        K = k;
        if (k <= 0) {
            System.err.println("The K-gram index can't be constructed for a negative K value");
            System.exit(1);
        }
    }

    /** Generate the ID for an unknown term */
    private int generateTermID() {
        return ++lastTermID;
    }

    public int getK() {
        return K;
    }


    /**
     *  Get intersection of two kgram postings lists
     */
    private List<KGramPostingsEntry> intersect(List<KGramPostingsEntry> p1, List<KGramPostingsEntry> p2) {
        // 
        // YOUR CODE HERE
        //
        // old intersect is to confusing, try new one
        List<KGramPostingsEntry> result = new ArrayList<KGramPostingsEntry>();
        int x = 0;
        int y = 0;
        if (p1 == null || p2 == null) {
            return null;
        } else {
            while (x < p1.size() && y < p2.size()) {
                if (p1.get(x).tokenID < p2.get(y).tokenID) {
                    x++;
                } else if (p1.get(x).tokenID == p2.get(y).tokenID && !result.contains(p1.get(x))) {
                    result.add(p1.get(x));
                    x++;
                    y++;
                } else {
                    y++;
                }
            }
        }
        return result;
    }


    /** Inserts all k-grams from a token into the index. */
    public void insert( String token ) {
        //
        // YOUR CODE HERE
        //
        // check if token in index
        if (getIDByTerm(token) != null) {
            return;
        }
        // insert token
        int tokenID = generateTermID();
        id2term.put(tokenID, token);
        term2id.put(token, tokenID);
        int tokenKGramCount = token.length() + 3 - this.K;
        KGramPostingsEntry kgPostEntr = new KGramPostingsEntry(tokenID);
        
        String kgram;
        String processingToken = "^" + token + "$";
        for (int i = 0; i < tokenKGramCount; i++) {
            kgram = processingToken.substring(i, i + this.K);

            if (!index.containsKey(kgram)) {
                index.put(kgram, new ArrayList<KGramPostingsEntry>());
            }
            // avoid duplicates
            if (!index.get(kgram).contains(kgPostEntr)) {
                index.get(kgram).add(kgPostEntr);
            }
        }
    }

    /** Get postings for the given k-gram */
    public List<KGramPostingsEntry> getPostings(String kgram) {
        //
        // YOUR CODE HERE
        //
        if (index.containsKey(kgram)) {
            return index.get(kgram);
        } else {
            return null;
        }
    }

    /** Get id of a term */
    public Integer getIDByTerm(String term) {
        return term2id.get(term);
    }

    /** Get a term by the given id */
    public String getTermByID(Integer id) {
        return id2term.get(id);
    }

    // print KGram entries for 3.3
    public void printSpecificEntries() {
        List<KGramPostingsEntry> veEntries = getPostings("ve");
        List<KGramPostingsEntry> thHeIntersect = intersect(getPostings("th"), getPostings("he"));
        
        System.out.println("All " + veEntries.size() + " entries of 've' di-gram");
        for (KGramPostingsEntry kGramPostingsEntry : veEntries) {
            System.out.println(id2term.get(kGramPostingsEntry.tokenID));
        }
        System.out.println("All " + thHeIntersect.size() + " entries of 'th he' intersected di-gram");
        for (KGramPostingsEntry kGramPostingsEntry : thHeIntersect) {
            System.out.println(id2term.get(kGramPostingsEntry.tokenID));
        }
    }

    // wildcard query processing
    public Query handleWildcardQueryTerm(String token) {
        Query extendedQuery = new Query();
        // use k-gram data-flow just like during indexing
        List<KGramPostingsEntry> postings = null;

        String newToken = "^" + token + "$";
        int tokenKGramCount = token.length() + 3 - getK();
        String kgram;
        for (int i = 0; i < tokenKGramCount; i++) {
            kgram = newToken.substring(i, i + getK());
            // handle seperate cases *tring, st*ing, strin*
            if (kgram.contains("*")) {
                i++;
                continue;
            }

            if (postings == null) {
                postings = getPostings(kgram);
            } else {
                postings = intersect(postings, getPostings(kgram));
            }
        }

        // finally check with regex for unwanted matches
        //System.out.println("Following terms were found for token: " + newToken);
        String regexToken = token.replace("*", ".*");
        for (int i = 0; i < postings.size(); i++) {
            String term = getTermByID(postings.get(i).tokenID);

            if (Pattern.matches(regexToken, term)) {
                extendedQuery.addToken(term);
                //System.out.println(term);
            }
        }
        return extendedQuery;
    }

    private static HashMap<String,String> decodeArgs( String[] args ) {
        HashMap<String,String> decodedArgs = new HashMap<String,String>();
        int i=0, j=0;
        while ( i < args.length ) {
            if ( "-p".equals( args[i] )) {
                i++;
                if ( i < args.length ) {
                    decodedArgs.put("patterns_file", args[i++]);
                }
            } else if ( "-f".equals( args[i] )) {
                i++;
                if ( i < args.length ) {
                    decodedArgs.put("file", args[i++]);
                }
            } else if ( "-k".equals( args[i] )) {
                i++;
                if ( i < args.length ) {
                    decodedArgs.put("k", args[i++]);
                }
            } else if ( "-kg".equals( args[i] )) {
                i++;
                if ( i < args.length ) {
                    decodedArgs.put("kgram", args[i++]);
                }
            } else {
                System.err.println( "Unknown option: " + args[i] );
                break;
            }
        }
        return decodedArgs;
    }

    public static void main(String[] arguments) throws FileNotFoundException, IOException {
        HashMap<String,String> args = decodeArgs(arguments);

        int k = Integer.parseInt(args.getOrDefault("k", "3"));
        KGramIndex kgIndex = new KGramIndex(k);

        File f = new File(args.get("file"));
        Reader reader = new InputStreamReader( new FileInputStream(f), StandardCharsets.UTF_8 );
        Tokenizer tok = new Tokenizer( reader, true, false, true, args.get("patterns_file") );
        while ( tok.hasMoreTokens() ) {
            String token = tok.nextToken();
            kgIndex.insert(token);
        }

        String[] kgrams = args.get("kgram").split(" ");
        List<KGramPostingsEntry> postings = null;
        for (String kgram : kgrams) {
            if (kgram.length() != k) {
                System.err.println("Cannot search k-gram index: " + kgram.length() + "-gram provided instead of " + k + "-gram");
                System.exit(1);
            }

            if (postings == null) {
                postings = kgIndex.getPostings(kgram);
            } else {
                postings = kgIndex.intersect(postings, kgIndex.getPostings(kgram));
            }
        }
        if (postings == null) {
            System.err.println("Found 0 posting(s)");
        } else {
            int resNum = postings.size();
            System.err.println("Found " + resNum + " posting(s)");
            if (resNum > 10) {
                System.err.println("The first 10 of them are:");
                resNum = 10;
            }
        }
    }
}
