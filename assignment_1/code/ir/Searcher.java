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
import java.util.Iterator;
import java.util.ListIterator;
import java.util.ArrayList;
import java.lang.Math;
import java.util.Collections;

/**
 *  Searches an index for results of a query.
 */
public class Searcher {

    /** The index to be searched by this Searcher. */
    Index index;

    /** The k-gram index to be searched by this Searcher */
    KGramIndex kgIndex;
    
    HashMap<Integer, Double> pageRanks = new HashMap<Integer, Double>();
    HashMap<Integer, Double> euclideanDocLength = new HashMap<Integer, Double>();
    double[] COMBINEDRANKING = {1.0, 250.0}; // {tf_idf, pagerank}

    /** Constructor */
    public Searcher( Index index, KGramIndex kgIndex ) {
        this.index = index;
        this.kgIndex = kgIndex;
        try {
            readPageRankingInfo();    
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    private PostingsList intersect(PostingsList p1, PostingsList p2) {
        PostingsList result = new PostingsList();

        //iterate over both lists
        ListIterator<PostingsEntry> Pl1Iter = p1.getList().listIterator();
        ListIterator<PostingsEntry> Pl2Iter = p2.getList().listIterator();

        // init values
        PostingsEntry pl1Value = null;
        PostingsEntry pl2Value = null;
        if (Pl1Iter.hasNext()) {
            pl1Value = Pl1Iter.next();
        } else {
            return result;
        }

        if (Pl2Iter.hasNext()) {
            pl2Value = Pl2Iter.next();
        } else {
            return result;
        }
        
        while (true) { // Pl1Iter.hasNext() && Pl2Iter.hasNext() change to always true and breaks in the if statements to include run of last element!!!!
            if (pl1Value.docID == pl2Value.docID) {
                // should a new entry object be created? (reference errors in case of manipulation) How does it in Java work
                PostingsEntry resEntry = new PostingsEntry(pl1Value.docID, pl1Value.score);
                result.add(resEntry);
                
                if (Pl1Iter.hasNext()) {
                    pl1Value = Pl1Iter.next();
                } else {
                    break;
                }
                if (Pl2Iter.hasNext()) {
                    pl2Value = Pl2Iter.next();
                } else {
                    break;
                }
            }else if (pl1Value.docID < pl2Value.docID) {
                if (Pl1Iter.hasNext()) {
                    pl1Value = Pl1Iter.next();
                } else {
                    break;
                }
            }else {
                if (Pl2Iter.hasNext()) {
                    pl2Value = Pl2Iter.next();
                } else {
                    break;
                }
            }
        }
        return result;
    }

    private PostingsList positionalIntersect(PostingsList p1, PostingsList p2) {
        PostingsList result = new PostingsList();

        //iterate over both lists
        ListIterator<PostingsEntry> Pl1Iter = p1.getList().listIterator();
        ListIterator<PostingsEntry> Pl2Iter = p2.getList().listIterator();

        // init values
        PostingsEntry pl1Value = null;
        PostingsEntry pl2Value = null;
        if (Pl1Iter.hasNext()) {
            pl1Value = Pl1Iter.next();
        } else {
            return result;
        }
        if (Pl2Iter.hasNext()) {
            pl2Value = Pl2Iter.next();
        } else {
            return result;
        }
        
        // while not at any lists end (checked inside while loop)
        while (true) {
            if (pl1Value.docID == pl2Value.docID) {
                //iterate over both offset lists
                ListIterator<Integer> offsets1Iter = pl1Value.offsets.listIterator();
                ListIterator<Integer> offsets2Iter = pl2Value.offsets.listIterator();

                // init offset values to compare
                Integer offsets1Value = null;
                Integer offsets2Value = null;
                if (offsets1Iter.hasNext()) {
                    offsets1Value = offsets1Iter.next();
                } else {
                    break;
                }
                if (offsets2Iter.hasNext()) {
                    offsets2Value = offsets2Iter.next();    
                } else {
                    break;
                }
                // check offset order
                while(true) {
                    int tokenDist = offsets2Value - offsets1Value;
                    if (tokenDist == 1) { // offset distance equals k = 1 (don't take abs value (would make order irelevant))
                        //searched token follow each other (with respect to order)
                        
                        // now check if entry is already added to the results
                        PostingsEntry entry_check = result.find(pl1Value.docID);
                        if(entry_check == null) {
                            PostingsEntry new_entry = new PostingsEntry(pl1Value.docID);
                            result.add(new_entry, offsets2Value); // add entry to results
                        } else {
                            entry_check.addOffset(offsets2Value);
                        }
                        if (offsets1Iter.hasNext()) {
                            offsets1Value = offsets1Iter.next();
                        } else {
                            break;
                        }
                        if (offsets2Iter.hasNext()) {
                            offsets2Value = offsets2Iter.next();
                        } else {
                            break;
                        }
                    } else if(offsets1Value < offsets2Value) {
                        if (offsets1Iter.hasNext()) {
                            offsets1Value = offsets1Iter.next();    
                        } else {
                            break;
                        }
                    } else { // (offsets1Value > offsets2Value)
                        if (offsets2Iter.hasNext()) {
                            offsets2Value = offsets2Iter.next();    
                        } else {
                            break;
                        }
                    }
                }
                // exited while for offset compare, now increment document iterator
                if (Pl1Iter.hasNext()) {
                    pl1Value = Pl1Iter.next();
                } else {
                    break;
                }
                if (Pl2Iter.hasNext()) {
                    pl2Value = Pl2Iter.next();
                } else {
                    break;
                }

            }else if (pl1Value.docID < pl2Value.docID) {
                if (Pl1Iter.hasNext()) {
                    pl1Value = Pl1Iter.next();
                } else {
                    break;
                }
            }else {
                if (Pl2Iter.hasNext()) {
                    pl2Value = Pl2Iter.next();
                } else {
                    break;
                }
            }
        }
        return result;
    }

    private PostingsList rankedUnion(PostingsList p1, PostingsList p2) {
        PostingsList result  = new PostingsList();
        for (PostingsEntry entry : p1.getList()) {
            if (p2.getDocIDMap().containsKey(entry.docID)) { // entry in both lists
                entry.score += p2.getDocIDMap().get(entry.docID).score;
                result.add(entry);
            } else {
                result.add(entry);
            }
        }
        // add all values of p2 that are not contained in p1
        for (PostingsEntry entry2 : p2.getList()) {
            if (!result.getDocIDMap().containsKey(entry2.docID)) { // entry already in result
                result.add(entry2);
            }
        }
        return result;
    }



    /**
     *  Searches the index for postings matching the query.
     *  @return A postings list representing the result of the query.
     */
    public PostingsList search( Query query, QueryType queryType, RankingType rankingType, NormalizationType normalizationType ) { 
        //
        //  REPLACE THE STATEMENT BELOW WITH YOUR CODE
        //
        if (query.queryterm.size() > 1 && queryType == QueryType.INTERSECTION_QUERY) {
            // 1.3 intersect search
            //get postings per token (had to increase efficency so tried to use Arrays instead of ArrayLists)
            PostingsList[] tokenDocList = new PostingsList[query.queryterm.size()];
            for (int i=0; i<query.queryterm.size(); i++) {
                String token = query.queryterm.get(i).term;
                tokenDocList[i] = this.index.getPostings(token);
            }
            // intersect individual token results
            PostingsList queryResult = tokenDocList[0];
            for (PostingsList postingsList : tokenDocList) {
                queryResult = this.intersect(postingsList, queryResult);
            }
            return queryResult;
        } 
        else if (query.queryterm.size() > 1 && queryType == QueryType.PHRASE_QUERY) {
            // 1.4
            //get postings per token (had to increase efficency so tried to use Arrays instead of ArrayLists)
            PostingsList[] tokenDocList = new PostingsList[query.queryterm.size()];
            for (int i=0; i<query.queryterm.size(); i++) {
                String token = query.queryterm.get(i).term;
                tokenDocList[i] = this.index.getPostings(token);
            }
            // intersect individual token results
            PostingsList queryResult = tokenDocList[0];
            for (int i = 1; i < tokenDocList.length; i++) { // Important start at 1, comparing token against it self gives problems with offset distance!!!
                queryResult = this.positionalIntersect(queryResult, tokenDocList[i]);
            }
            return queryResult;
        }
        else if (queryType == QueryType.RANKED_QUERY) {
            // no query given
            if (query.queryterm.get(0).term == "") {
                return null;
            }







            
            if ((rankingType == RankingType.TF_IDF) && (query.queryterm.size() == 1)) {
                // 2.1 tf_idf score
                PostingsList queryResult = this.index.getPostings(query.queryterm.get(0).term);
                tfIdfScore(queryResult, normalizationType, query.queryterm.size(), query.queryterm.get(0).weight);
                Collections.sort(queryResult.getList());
                return queryResult;
            } 
            else if ((rankingType == RankingType.TF_IDF) && (query.queryterm.size() > 1)) {
                // 2.2 tf_idf score
                PostingsList queryResult = this.index.getPostings(query.queryterm.get(0).term);
                tfIdfScore(queryResult, normalizationType, query.queryterm.size(), query.queryterm.get(0).weight);
                // implement union not intersect
                for (int i=1; i<query.queryterm.size(); i++) { // exclude first term as it is already used in init
                    String token = query.queryterm.get(i).term;
                    double tokenWeight = query.queryterm.get(i).weight;
                    PostingsList termsPostingslist = this.index.getPostings(token);
                    tfIdfScore(termsPostingslist, normalizationType, query.queryterm.size(), tokenWeight);
                    queryResult = this.rankedUnion(queryResult, termsPostingslist);
                }
                Collections.sort(queryResult.getList());
                return queryResult;
                
            } 
            else if (rankingType == RankingType.PAGERANK) {
                // 2.5
                PostingsList queryResult = this.index.getPostings(query.queryterm.get(0).term);
                prScore(queryResult);
                if (query.queryterm.size() > 1) { // ranked multi term search
                    // implement union not intersect
                    for (int i=1; i<query.queryterm.size(); i++) { // exclude first term as it is already used in init
                        String token = query.queryterm.get(i).term;
                        PostingsList termsPostingslist = this.index.getPostings(token);
                        prScore(termsPostingslist);
                        queryResult = this.rankedUnion(queryResult, termsPostingslist);
                    }   
                }
                Collections.sort(queryResult.getList());
                return queryResult;
            } else if (rankingType == RankingType.COMBINATION) {
                // 2.5
                PostingsList queryResult = this.index.getPostings(query.queryterm.get(0).term);
                combinedScore(queryResult, normalizationType, query.queryterm.size(), COMBINEDRANKING[0], COMBINEDRANKING[1]);
                if (query.queryterm.size() > 1) { // ranked multi term search
                    // implement union not intersect
                    for (int i=1; i<query.queryterm.size(); i++) { // exclude first term as it is already used in init
                        String token = query.queryterm.get(i).term;
                        PostingsList termsPostingslist = this.index.getPostings(token);
                        combinedScore(termsPostingslist, normalizationType, query.queryterm.size(), COMBINEDRANKING[0], COMBINEDRANKING[1]);
                        queryResult = this.rankedUnion(queryResult, termsPostingslist);
                    }
                }
                Collections.sort(queryResult.getList());
                return queryResult;
            } else {
              System.err.println("Unsuported ranking type");
              return new PostingsList();
            }
        }
        else {
            // 1.2
            String term = query.queryterm.get(0).term;
            if (term == "") {
                return null;
            } else {
                return this.index.getPostings(query.queryterm.get(0).term);
            }
            
        }
    }

    // scoring methods
    private void tfIdfScore(PostingsList postingsList, NormalizationType normalizationType, double queryL, double queryWeight) {
        for (PostingsEntry entry : postingsList.getList()) {
            double normalizationTerm = 1.0;
            double queryLength = queryL;
            if (normalizationType == NormalizationType.NUMBER_OF_WORDS) {
                normalizationTerm = this.index.docLengths.get(entry.docID);
            } else if (normalizationType == NormalizationType.EUCLIDEAN) {
                normalizationTerm = this.euclideanDocLength.get(entry.docID);
                queryLength = Math.sqrt(queryLength);
            } else {
                // None
                normalizationTerm = 1.0;
            }
            
            double tf = entry.offsets.size();
            double df_t = postingsList.size();
            double idf = Math.log((double)index.docLengths.size()/(double)df_t);

            // term weight of query is assumed to be 1!!!
            double queryPart = (queryWeight * idf) / queryLength;
            double docPart = (tf * idf) / normalizationTerm;
            double cosSimilarity = queryPart * docPart;
            
            entry.score = cosSimilarity;
        } 
    }

    private void prScore(PostingsList postingsList) {
        for (PostingsEntry entry : postingsList.getList()) {
            double score = this.pageRanks.get(entry.docID);
            entry.score = score;
        }
    }

    private void combinedScore(PostingsList postingsList, NormalizationType normalizationType, double queryLength, double w1, double w2) {
        for (PostingsEntry entry : postingsList.getList()) {
            double normalizationTerm = 1.0;
            if (normalizationType == NormalizationType.NUMBER_OF_WORDS) {
                normalizationTerm = this.index.docLengths.get(entry.docID);
            } else if (normalizationType == NormalizationType.EUCLIDEAN) {
                normalizationTerm = this.euclideanDocLength.get(entry.docID);
            } else {
                // None
                normalizationTerm = 1.0;
            }
            double pageRankScore = 0.0;
            if (this.pageRanks.get(entry.docID) == null) {
                pageRankScore = 0.0;
            }else {
                pageRankScore = this.pageRanks.get(entry.docID);
            }

            double tf = entry.offsets.size();
            double df_t = postingsList.size();
            double idf = Math.log((double)index.docLengths.size()/(double)df_t);
            // term weight of query is assumed to be 1!!!
            double queryPart = 1.0; // (1.0 * idf) / queryLength;
            double docPart = 0.0;
            if (normalizationTerm != 0.0) {
                docPart = (tf * idf) / normalizationTerm;   
            }
            
            double cosSimilarity = queryPart * docPart;
            double score = w1 * cosSimilarity + w2 * pageRankScore;
            entry.score = score;
        }
    }

    private void readPageRankingInfo() throws IOException {
        HashMap<String, Double> titleRankMap = new HashMap<String, Double>();
		File rankFile = new File( "index/pageRanks" );
        FileReader rankReader = new FileReader(rankFile);
        try (BufferedReader br = new BufferedReader(rankReader)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(";");
                titleRankMap.put(data[0],Double.valueOf(data[1]));
            }
        }
        rankReader.close();

        File idFile = new File( "index/docInfo" );
        FileReader idReader = new FileReader(idFile);
        try (BufferedReader br = new BufferedReader(idReader)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] idTitle = line.split(";");
                String[] path = idTitle[1].split("/");
                String fileName = path[path.length-1];
                int docId = Integer.valueOf(idTitle[0]);
                this.pageRanks.put(docId, titleRankMap.get(fileName));
            }
        }
        idReader.close();

        File docLengthFile = new File( "index/euclideanDocumentLength" );
        FileReader docLengthReader = new FileReader(docLengthFile);
        try (BufferedReader br = new BufferedReader(docLengthReader)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(";");
                this.euclideanDocLength.put(Integer.valueOf(data[0]), Double.valueOf(data[1]));
            }
        }
        docLengthReader.close();
    }
}