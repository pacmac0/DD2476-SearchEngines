/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.io.Serializable;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {

    public int docID;
    public double score = 0;
    // add offset list for positional information
    public ArrayList<Integer> offsets = new ArrayList<>();

    /**
     *  PostingsEntries are compared by their score (only relevant
     *  in ranked retrieval).
     *
     *  The comparison is defined so that entries will be put in 
     *  descending order.
     */
    public int compareTo( PostingsEntry other ) {
       return Double.compare( other.score, score );
    }

    //
    // YOUR CODE HERE
    //
    
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("[");
        str.append(this.docID + "; ");
        str.append(this.score + "; ");
        str.append("[");
        int offsetSize = this.offsets.size();
        for (int i = 0; i < this.offsets.size(); i++) {
            int offset = this.offsets.get(i);
            if (i < (this.offsets.size() - 1)) {
                str.append(offset + ". ");    
            } else {
                str.append(offset);
            }
        }
        str.append("]");
        str.append("]");
        return str.toString();
    }

    // construct
    public PostingsEntry(int docID, double score) {
        this.docID = docID;
        this.score = score;
    }
    public PostingsEntry(int docID) {
        this.docID = docID;
    }
    public PostingsEntry(String string) {
        // [17479; 0.0; [122. 757]]
        // assume perfect case
        String[] split_string = string.split("; ");
        this.docID = Integer.parseInt(split_string[0].replace("[",""));
        this.score = Double.parseDouble(split_string[1]);
        String[] offsets_string = split_string[2].split(". ");
        for (int i = 0; i < offsets_string.length; i++) {
            String offset_str = offsets_string[i];
            if (offsets_string[i].contains("[")) {
                offset_str = offset_str.replace("[","");
            }
            if (offsets_string[i].contains("]")) {
                offset_str = offset_str.replace("]","");
            }
            int offset = Integer.parseInt(offset_str);
            this.offsets.add(offset);
        }
    }
    
    public void addOffset(Integer offset) {
        this.offsets.add(offset);
    }
}

