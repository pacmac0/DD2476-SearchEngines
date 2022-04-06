/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.lang.Math;

public class PostingsList {
    
    /** The postings list */
    private ArrayList<PostingsEntry> list = new ArrayList<>();
    private HashMap<Integer,PostingsEntry> docIDmap = new HashMap<Integer,PostingsEntry>();

    /** Number of postings in this list. */
    public int size() {
    return list.size();
    }

    /** Returns the ith posting. */
    public PostingsEntry get( int i ) {
    return list.get( i );
    }

    // 
    //  YOUR CODE HERE
    //
    // constructor not needed

    // getter for list needed, because list is private (which conceptual advantage has this?)
    public ArrayList<PostingsEntry> getList() {
        return list;
    }
    public HashMap<Integer,PostingsEntry> getDocIDMap() {
        return docIDmap;
    }

    public String toString() {
        return String.format("%s", this.list.toString());
    }

    public PostingsList() {
    }
    // construct from string
    public PostingsList(String string) {
        // assume perfect case
        // [[215; 0.0; [21]], [3541; 0.0; [129]], [15118; 0.0; [5893]], [15656; 0.0; [1194]]]
        String[] split_string = string.split(", ");
        for (int i = 0; i < split_string.length; i++) {
            PostingsEntry new_entry = new PostingsEntry(split_string[i]);
            this.add(new_entry);
        }
    }

    public void add(PostingsEntry entry, Integer offset) {
        entry.addOffset(offset);
        list.add(entry);
        docIDmap.put(entry.docID, entry); 
    }

    public void add(PostingsEntry entry) {
        list.add(entry);
        docIDmap.put(entry.docID, entry); 
    }

    public static PostingsList merge (PostingsList pl1, PostingsList pl2) {
        for (PostingsEntry postingsEntry : pl2.getList()) {
            if (!pl1.docIDmap.containsKey(postingsEntry.docID)) { // avoid duplicates
                pl1.add(postingsEntry);
                pl1.docIDmap.put(postingsEntry.docID, postingsEntry);
            } else { 
                // merge offset lists
                PostingsEntry pl1Entry = pl1.docIDmap.get(postingsEntry.docID);
                for (Integer offset : postingsEntry.offsets) {
                    if (!pl1Entry.offsets.contains(offset)) {
                        pl1Entry.addOffset(offset);    
                    }    
                }
                Collections.sort(pl1Entry.offsets);
            }
        }
        return pl1;
    }

    public PostingsEntry find(int searched_docID) {
        // arrayList seems inefficient to search through, why not also use a hashMap as PostingsList?
        /*
        for (PostingsEntry postingsEntry : list) {
            if (postingsEntry.docID == searched_docID) {
                // found
                return postingsEntry;
            }
        } */
        if(docIDmap.containsKey(searched_docID))
            return docIDmap.get(searched_docID);
        else
            return null;
    }
}

