/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, KTH, 2018
 */  

package ir;

import java.io.*;
import java.util.*;
import java.nio.charset.*;
import java.lang.Math;

import java.security.MessageDigest; 
import java.security.NoSuchAlgorithmException;


/*
 *   Implements an inverted index as a hashtable on disk.
 *   
 *   Both the words (the dictionary) and the data (the postings list) are
 *   stored in RandomAccessFiles that permit fast (almost constant-time)
 *   disk seeks. 
 *
 *   When words are read and indexed, they are first put in an ordinary,
 *   main-memory HashMap. When all words are read, the index is committed
 *   to disk.
 */
public class PersistentHashedIndex implements Index {

    /** The directory where the persistent index files are stored. */
    public static final String INDEXDIR = "./index";

    /** The dictionary file name */
    public static final String DICTIONARY_FNAME = "dictionary";

    /** The dictionary file name */
    public static final String DATA_FNAME = "data";

    /** The terms file name */
    public static final String TERMS_FNAME = "terms";

    /** The doc info file name */
    public static final String DOCINFO_FNAME = "docInfo";

    /** The dictionary hash table on disk can fit this many entries. */
    public static final long TABLESIZE = 611953L;

    /** A cleam byte array for init */
    byte[] CLEAN = new byte[20];
    int entryCount = 0;

    /** The dictionary hash table is stored in this file. */
    RandomAccessFile dictionaryFile;

    /** The data (the PostingsLists) are stored in this file. */
    RandomAccessFile dataFile;

    /** Pointer to the first free memory cell in the data file. */
    long free = 0L;

    /** The cache as a main-memory hash map. */
    HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();


    // ===================================================================

    /**
     *   A helper class representing one entry in the dictionary hashtable.
     */ 
    public class Entry {
        //
        //  YOUR CODE HERE
        //
        long ptr;
        int referencedDataSize;
        long collisionIdentifier; //(token + token).hashcode()


        public Entry(long ptr, int size, long collision_identifier) {
            this.ptr = ptr;
            this.referencedDataSize = size;
            this.collisionIdentifier = collision_identifier;
        }
        /*
        public Entry(String entryStr) {
            String[] args = entryStr.split(", ");
            this.ptr = Long.parseLong(args[0]);
            this.referencedDataSize = Integer.parseInt(args[1]);
        }
        public String toString() {
            StringBuilder str = new StringBuilder();
            str.append(this.ptr);
            str.append(", ");
            str.append(this.referencedDataSize);
            return str.toString();
        }
        */
    }


    // ==================================================================

    
    /**
     *  Constructor. Opens the dictionary file and the data file.
     *  If these files don't exist, they will be created. 
     */
    public PersistentHashedIndex() {
        try {
            dictionaryFile = new RandomAccessFile( INDEXDIR + "/" + DICTIONARY_FNAME, "rw" );
            dataFile = new RandomAccessFile( INDEXDIR + "/" + DATA_FNAME, "rw" );
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        try {
            readDocInfo();
        } catch ( FileNotFoundException e ) {
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    /**
     *  Writes data to the data file at a specified place.
     *
     *  @return The number of bytes written.
     */ 
    int writeData( String dataString, long ptr ) {
        try {
            dataFile.seek( ptr ); 
            byte[] data = dataString.getBytes();
            dataFile.write( data );
            return data.length;
        } catch ( IOException e ) {
            e.printStackTrace();
            return -1;
        }
    }


    /**
     *  Reads data from the data file
     */ 
    String readData( long ptr, int size ) {
        try {
            dataFile.seek( ptr );
            byte[] data = new byte[size];
            dataFile.readFully( data );
            return new String(data);
        } catch ( IOException e ) {
            e.printStackTrace();
            return null;
        }
    }


    // ==================================================================
    //
    //  Reading and writing to the dictionary file.

    /*
     *  Writes an entry to the dictionary hash table file. 
     *
     *  @param entry The key of this entry is assumed to have a fixed length
     *  @param ptr   The place in the dictionary file to store the entry
     */
    void writeEntry( Entry entry, long ptr ) {
        //
        //  YOUR CODE HERE
        //
        try {
            dictionaryFile.seek( ptr );
            dictionaryFile.writeLong(entry.collisionIdentifier);
            dictionaryFile.writeLong(entry.ptr);
            dictionaryFile.writeInt(entry.referencedDataSize);
            System.out.print("\r"+entryCount);
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    // get hash
    // https://www.geeksforgeeks.org/sha-256-hash-in-java/
    static synchronized byte[] getHash(String data) {
        try {
            return MessageDigest.getInstance("SHA-1").digest(data.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            // sha-1 exists
            byte[] empty = new byte[0];
            return empty;
        }
    }

    /**
     *  Reads an entry from the dictionary file.
     *
     *  @param ptr The place in the dictionary file where to start reading.
     */
    Entry readEntry( long ptr ) {   
        //
        //  REPLACE THE STATEMENT BELOW WITH YOUR CODE 
        //
        // each entry has a size of 20 long + int + long
        try {
            dictionaryFile.seek( ptr );
            long colDet = dictionaryFile.readLong();
            long entry_ptr = dictionaryFile.readLong();
            int size = dictionaryFile.readInt();
            Entry entry = new Entry(entry_ptr, size, colDet);
            return entry;
        } catch ( IOException e ) {
            e.printStackTrace();
            return null;
        }
    }


    // ==================================================================

    /**
     *  Writes the document names and document lengths to file.
     *
     * @throws IOException  { exception_description }
     */
    private void writeDocInfo() throws IOException {
        FileOutputStream fout = new FileOutputStream( INDEXDIR + "/docInfo" );
        for (Map.Entry<Integer,String> entry : docNames.entrySet()) {
            Integer key = entry.getKey();
            String docInfoEntry = key + ";" + entry.getValue() + ";" + docLengths.get(key) + "\n";
            fout.write(docInfoEntry.getBytes());
        }
        fout.close();
    }


    /**
     *  Reads the document names and document lengths from file, and
     *  put them in the appropriate data structures.
     *
     * @throws     IOException  { exception_description }
     */
    private void readDocInfo() throws IOException {
        File file = new File( INDEXDIR + "/docInfo" );
        FileReader freader = new FileReader(file);
        try (BufferedReader br = new BufferedReader(freader)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(";");
                docNames.put(new Integer(data[0]), data[1]);
                docLengths.put(new Integer(data[0]), new Integer(data[2]));
            }
        }
        freader.close();
    }


    /**
     *  Write the index to files.
     */
    public void writeIndex() {
        int collisions = 0;
        try {
            // Write the 'docNames' and 'docLengths' hash maps to a file
            writeDocInfo();

            // Write the dictionary and the postings list

            // 
            //  YOUR CODE HERE
            //
            // clean memmory in file range
            dictionaryFile.seek(0);
            while (dictionaryFile.getFilePointer() <= TABLESIZE * 20) {
                dictionaryFile.write(CLEAN);
            }
            long current_data_file_position = 0;            
            for(Map.Entry<String, PostingsList> entry : index.entrySet()) {
                entryCount++;
                String token = entry.getKey();
                PostingsList tokenDocList = entry.getValue();
                // write string of tokenDocList to available memory location in file
                String docList_string = tokenDocList.toString();
                int dataLength = writeData( docList_string, current_data_file_position );
                long collisionDet = Math.abs((token + token).hashCode() % TABLESIZE);
                // create new dictionary entry and write it to dict file
                Entry dictEntry = new Entry(current_data_file_position, dataLength, collisionDet);
                // update next position in data_file
                current_data_file_position = current_data_file_position + dataLength;

                    // get hash of token as ptr, limit dictAddr to list space 0-611953L
                long dictAddr = Math.abs(token.hashCode() % TABLESIZE) * 20;
                // map dictionary address to entry block of size 20
                // dictAddr = dictAddr - (dictAddr % 20);

                // check if position is taken (collision)
                byte[] pos = new byte[20];
                dictionaryFile.seek( dictAddr );
                dictionaryFile.readFully(pos);
                boolean condition = Arrays.equals(pos, CLEAN);
                if (!condition) {
                    collisions++;
                    while (!condition) {
                        // System.out.println("position not clean");

                        long control_hash_inFile = readEntry(dictAddr).collisionIdentifier;
                        long hash_inEntry = dictEntry.collisionIdentifier;
                        boolean control_condition = control_hash_inFile != hash_inEntry;
                        if (control_condition) {
                            // System.out.println("Collision detected => position different");

                            // collision detected
                            /*
                            if (dictAddr+20 > TABLESIZE * 20) { // if end on TABLE start at beginning
                                dictAddr = 0;
                            } else {
                                dictAddr += 20;    
                            }
                            */
                            dictAddr += 20;
                            dictionaryFile.readFully(pos);
                            condition = Arrays.equals(pos, CLEAN);
                        } else {
                            // same entry (controle condition fits)
                            break;
                        }
                    }
                    writeEntry(dictEntry, dictAddr);
                } else {
                    // System.out.println("position free, writing...");
                    // dictionaryFile.seek(dictAddr);
                    writeEntry(dictEntry, dictAddr);
                }
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        System.err.println( collisions + " collisions." );
    }


    // ==================================================================


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
        //
        //  REPLACE THE STATEMENT BELOW WITH YOUR CODE
        //
        // try out retreving the entry it
        long readAddr = Math.abs(token.hashCode() % TABLESIZE) * 20;
        // readAddr = readAddr - (readAddr % 20);
        Entry retDictEnt = readEntry(readAddr);
        
        // check for collision
        while (retDictEnt.collisionIdentifier != Math.abs((token+token).hashCode() % TABLESIZE)) {
            // read next entry and check condition
            readAddr = readAddr + 20;
            /*
            if (readAddr > TABLESIZE * 20) {
                System.out.println("Check table from start");
                readAddr = 0;
            }
            */
            retDictEnt = readEntry(readAddr);
        }
        String ret_docList_string = readData(retDictEnt.ptr, retDictEnt.referencedDataSize);
        PostingsList reconstructed_PL = new PostingsList(ret_docList_string);
        return reconstructed_PL;
    }


    /**
     *  Inserts this token in the main-memory hashtable.
     */
    public void insert( String token, int docID, int offset ) {
        //
        //  YOUR CODE HERE
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
     *  Write index to file after indexing is done.
     */
    public void cleanup() {
        System.err.println( index.keySet().size() + " unique words" );
        System.err.print( "Writing index to disk..." );
        writeIndex();
        System.err.println( "done!" );
    }
}
