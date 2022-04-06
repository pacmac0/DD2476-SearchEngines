package pagerank;

import java.util.*;
import java.util.stream.*;

import javax.print.attribute.standard.NumberOfDocuments;

import java.io.*;

public class PageRank {

    /**  
     *   Maximal number of documents. We're assuming here that we
     *   don't have more docs than we can keep in main memory.
     */
    final static int MAX_NUMBER_OF_DOCS = 2000000;

    /**
     *   Mapping from document names to document numbers.
     */
    HashMap<String,Integer> docNumber = new HashMap<String,Integer>();

    /**
     *   Mapping from document numbers to document names
     */
    String[] docName = new String[MAX_NUMBER_OF_DOCS];

    /**  
     *   A memory-efficient representation of the transition matrix.
     *   The outlinks are represented as a HashMap, whose keys are 
     *   the numbers of the documents linked from.<p>
     *
     *   The value corresponding to key i is a HashMap whose keys are 
     *   all the numbers of documents j that i links to.<p>
     *
     *   If there are no outlinks from i, then the value corresponding 
     *   key i is null.
     */
    HashMap<Integer,HashMap<Integer,Boolean>> link = new HashMap<Integer,HashMap<Integer,Boolean>>();

    /**
     *   The number of outlinks from each node.
     */
    int[] out = new int[MAX_NUMBER_OF_DOCS];

    /**
     *   The probability that the surfer will be bored, stop
     *   following links, and take a random jump somewhere.
     */
    final static double BORED = 0.15;

    /**
     *   Convergence criterion: Transition probabilities do not 
     *   change more that EPSILON from one iteration to another.
     */
	final static double EPSILON = 0.0001;
	
	/**
	 *   Create inverted link Map
	 */
	//HashMap<Integer,HashMap<Integer,Boolean>> linksToFromMap = new HashMap<Integer,HashMap<Integer,Boolean>>();
	HashMap<Integer, Double> topThirtyRanking = new HashMap<Integer, Double>();
       
    /* --------------------------------------------- */


    public PageRank( String filename ) {
		int noOfDocs = readDocs( filename );
		//getLinkMap(noOfDocs); // Too computationally heavy
		iterate( noOfDocs, 1000 );
		//mcExperiment(noOfDocs);
		//runMCOnSW(noOfDocs);
		/*
		try {
			readTopThirty();
			int[] Ns = {100,1000,5000,noOfDocs, 2*noOfDocs};
			int[] Ms = {1,2,3,4,10,20,30};

			for (int n : Ns) {
				double sse = topThirtySSE(monteCarloFive(noOfDocs, n));		
				System.out.println(sse);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
		

    }


    /* --------------------------------------------- */


    /**
     *   Reads the documents and fills the data structures. 
     *
     *   @return the number of documents read.
     */
    int readDocs( String filename ) {
	int fileIndex = 0;
	try {
	    System.err.print( "Reading file... " );
	    BufferedReader in = new BufferedReader( new FileReader( filename ));
	    String line;
	    while ((line = in.readLine()) != null && fileIndex<MAX_NUMBER_OF_DOCS ) {
		int index = line.indexOf( ";" );
		String title = line.substring( 0, index );
		Integer fromdoc = docNumber.get( title );
		//  Have we seen this document before?
		if ( fromdoc == null ) {	
		    // This is a previously unseen doc, so add it to the table.
		    fromdoc = fileIndex++;
		    docNumber.put( title, fromdoc );
		    docName[fromdoc] = title;
		}
		// Check all outlinks.
		StringTokenizer tok = new StringTokenizer( line.substring(index+1), "," );
		while ( tok.hasMoreTokens() && fileIndex<MAX_NUMBER_OF_DOCS ) {
		    String otherTitle = tok.nextToken();
		    Integer otherDoc = docNumber.get( otherTitle );
		    if ( otherDoc == null ) {
			// This is a previousy unseen doc, so add it to the table.
			otherDoc = fileIndex++;
			docNumber.put( otherTitle, otherDoc );
			docName[otherDoc] = otherTitle;
		    }
		    // Set the probability to 0 for now, to indicate that there is
		    // a link from fromdoc to otherDoc.
		    if ( link.get(fromdoc) == null ) {
			link.put(fromdoc, new HashMap<Integer,Boolean>());
		    }
		    if ( link.get(fromdoc).get(otherDoc) == null ) {
			link.get(fromdoc).put( otherDoc, true );
			out[fromdoc]++;
		    }
		}
	    }
	    if ( fileIndex >= MAX_NUMBER_OF_DOCS ) {
		System.err.print( "stopped reading since documents table is full. " );
	    }
	    else {
		System.err.print( "done. " );
	    }
	}
	catch ( FileNotFoundException e ) {
	    System.err.println( "File " + filename + " not found!" );
	}
	catch ( IOException e ) {
	    System.err.println( "Error reading file " + filename );
	}
	System.err.println( "Read " + fileIndex + " number of documents" );
	return fileIndex;
    }


	/* --------------------------------------------- */
	
	/**
	 * 
	 * 
	 */
	private void mcExperiment(int numberOfDocs) {
		try {
			readTopThirty();
			int[] Ns = {1000, 5000, 10000, numberOfDocs, 2*numberOfDocs, 4*numberOfDocs};
			int T = 10; //numberOfDocs/1000;
			int M = 1;
			
			for (int N : Ns) {
				double[][] predictions = {
					monteCarloOne(numberOfDocs, N, T),
					monteCarloTwo(numberOfDocs, M, T),
					monteCarloFour(numberOfDocs, M),
					monteCarloFive(numberOfDocs, N),
				};
				// compare SSE
				for (int m = 0; m < 4; m++) {
					double sse = 1000.0;
					try {
						sse = topThirtySSE(predictions[m]);	
					} catch (Exception e) {
						e.printStackTrace();
					}
					System.out.println("MC" + m + " for " + N + " runs has" + " SSE: " + sse);	
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void runMCOnSW(int noOfDocs) {
		// read Svwiki titles
		HashMap<String, String> iDTitleMap = new HashMap<String, String>();
		try{
			iDTitleMap = readTitle("src/main/java/pagerank/svwikiTitles.txt");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		double[] predict = monteCarloOne(noOfDocs, noOfDocs, 15);

		int[] sortedDocNums = IntStream.range(0, predict.length)
						.boxed().sorted((i,j) -> Double.compare(predict[j], predict[i]) )
						.mapToInt(ele -> ele).toArray();
		// print the 30 highest ranked pages(Docs)
		System.out.println("The top 30 ranked Documents are:");
		for (int p = 0; p < 30; p++) {
			int docNum = sortedDocNums[p];
			double pr = predict[docNum];
			String docID = this.docName[docNum];

			System.out.print(iDTitleMap.get(docID));
			System.out.print(", ");
			System.out.print(pr);
			System.out.println();
		}
	}


	private double[] monteCarloOne(int numberOfDocs, int Nruns, int maxSteps) {
		double[] pi = new double[numberOfDocs]; // page ranks of docs
		double[] endpointCount = new double[numberOfDocs]; // count how often walk ended at this doc
		int[] allDocNums = IntStream.range(0, numberOfDocs).toArray();
		Random rand = new Random();

		for (int i = 0; i < Nruns; i++) {
			int currentDoc = allDocNums[rand.nextInt(allDocNums.length)];
			
			for (int t = 0; t < maxSteps; t++) {
				if (Math.random() <= PageRank.BORED) {
					break; // random stop of this run
				} else {
					// choice of random following doc
					if (this.link.get(currentDoc) == null) { // is sink
						currentDoc = allDocNums[rand.nextInt(allDocNums.length)]; // start at any random doc
					} else {
						int[] followers = this.link.get(currentDoc).keySet().stream().mapToInt(Integer::intValue).toArray(); 
						currentDoc = followers[rand.nextInt(followers.length)];	
					}
				}
			}
			endpointCount[currentDoc]++;
		}
		// compute pi (pagerank) from walk counts
		for (int p = 0; p < pi.length; p++) {
			pi[p] = endpointCount[p]/Nruns;
		}
		return pi;
	}

	private double[] monteCarloTwo(int numberOfDocs, int m, int maxSteps) {
		double[] pi = new double[numberOfDocs]; // page ranks of docs
		double[] endpointCount = new double[numberOfDocs]; // count how often walk ended at this doc
		int[] allDocNums = IntStream.range(0, numberOfDocs).toArray();
		Random rand = new Random();

		for (int j = 0; j < m; j++) {
			for (int i = 0; i < (numberOfDocs); i++) {
				int currentDoc = allDocNums[i];
				
				for (int t = 0; t < maxSteps; t++) {
					if (Math.random() <= PageRank.BORED) {
						break; // random stop of this run
					} else {
						// choice of random following doc
						if (this.link.get(currentDoc) == null) {
							currentDoc = allDocNums[rand.nextInt(allDocNums.length)]; // jump to any random doc
						} else {
							int[] followers = this.link.get(currentDoc).keySet().stream().mapToInt(Integer::intValue).toArray(); 
							currentDoc = followers[rand.nextInt(followers.length)];	
						}
					}
				}
				endpointCount[currentDoc]++;
			}	
		}
		// compute pi (pagerank) from walk counts
		for (int p = 0; p < pi.length; p++) {
			pi[p] = endpointCount[p]/(m*numberOfDocs);
		}
		return pi;
	}
	
	private double[] monteCarloFour(int numberOfDocs, int m) {
		double[] pi = new double[numberOfDocs]; // page ranks of docs
		double[] endpointCount = new double[numberOfDocs]; // count how often walk ended at this doc
		Random rand = new Random();
		
		for (int j = 0; j < m; j++) {
			for (int n = 0; n < (numberOfDocs); n++) {
				// still use random stop, prevents inf. loops!!!!!!
				int currentDoc = n;
				endpointCount[currentDoc]++;
				while (true) {
					if (Math.random() <= PageRank.BORED) {
						break; // random stop of this run
					} else {
						// choice of random following doc
						if (this.link.get(currentDoc) == null) { // this seems not to be executed loop: 2147->19600
							break;
						} else {
							int[] followers = this.link.get(currentDoc).keySet().stream().mapToInt(Integer::intValue).toArray(); 
							currentDoc = followers[rand.nextInt(followers.length)];
							endpointCount[currentDoc]++;
						}
					}
				}
			}
		}	
		// compute pi (pagerank) from walk counts
		for (int p = 0; p < pi.length; p++) {
			pi[p] = endpointCount[p]/(DoubleStream.of(endpointCount).sum());
		}
		return pi;
	}

	private double[] monteCarloFive(int numberOfDocs, int Nruns) {
		double[] pi = new double[numberOfDocs]; // page ranks of docs
		double[] endpointCount = new double[numberOfDocs]; // count how often walk ended at this doc
		int[] allDocNums = IntStream.range(0, numberOfDocs).toArray();
		Random rand = new Random();
		
		for (int n = 0; n < Nruns; n++) {
			// still use random stop, prevents inf. loops!!!!!!
			int currentDoc = allDocNums[rand.nextInt(allDocNums.length)];
			endpointCount[currentDoc]++;
			while (true) {
				if (Math.random() <= PageRank.BORED) {
					break; // random stop of this run
				} else {
					// choice of random following doc
					if (this.link.get(currentDoc) == null) { // this seems not to be executed loop: 2147->19600
						break;
					} else {
						int[] followers = this.link.get(currentDoc).keySet().stream().mapToInt(Integer::intValue).toArray(); 
						currentDoc = followers[rand.nextInt(followers.length)];
						endpointCount[currentDoc]++;
					}
				}
			}
		}	
		// compute pi (pagerank) from walk counts
		for (int p = 0; p < pi.length; p++) {
			pi[p] = endpointCount[p]/(DoubleStream.of(endpointCount).sum());
		}
		return pi;
	}

	private double topThirtySSE (double[] predictions) {
		// SSE
		double sse = 0.0;
		for (Map.Entry<Integer, Double> entry : this.topThirtyRanking.entrySet()) {
			int docNum = entry.getKey();
			double ranking = entry.getValue();
			sse += Math.pow(ranking - predictions[docNum], 2);
			//System.out.println("DocID: " + docNum + " pred: " + predictions[docNum] + " real: " + ranking + " CURRENT SSE: " + sse);
		}
		return sse;
	}











    /*
     *   Chooses a probability vector a, and repeatedly computes
     *   aP, aP^2, aP^3... until aP^i = aP^(i+1).
     */
    void iterate( int numberOfDocs, int maxIterations ) {

		// YOUR CODE HERE
		// init
		double[] x = new double[numberOfDocs]; // last iterations version
		double[] xNew = new double[numberOfDocs];
		Arrays.fill(xNew, 0.0);
		xNew[0] = 1.0;
		Arrays.fill(x, 0.0);

		// get sinks
		HashSet<Integer> sinkDocNums = new HashSet<>(); 
		sinkDocNums.addAll(IntStream.rangeClosed(0, numberOfDocs-1).boxed().collect(Collectors.toList()));
		sinkDocNums.removeAll(link.keySet());

		// run for maxiterations or if eppsilon check breaks
		Double c = 1 - PageRank.BORED;
		int iterationCount = 0;
		while ( (iterationCount <= maxIterations) && ( (vectorDistance(x, xNew)) > PageRank.EPSILON)) {
			x = xNew.clone();

			Arrays.fill(xNew, (PageRank.BORED / numberOfDocs));

			for (Map.Entry<Integer,HashMap<Integer,Boolean>>  fromToMap : this.link.entrySet()) {
				Integer from = fromToMap.getKey();
				for (Integer to : fromToMap.getValue().keySet()) {
					xNew[to] += c * (x[from] / this.out[from]);
				}
			}
			// add the constant from the sinks
			for (Integer sinkDoc : sinkDocNums) {
				double sinkConst = x[sinkDoc]/numberOfDocs;
				for (int doc = 0; doc < numberOfDocs; doc++) {
					xNew[doc] += c * sinkConst;
				}
			}
			
			// normalize !!!
			double normalizer = 0.0;
			for(double pr : xNew) {
				normalizer += pr;
			}
			if(normalizer == 0.0) {
				System.out.println("ERROR: normalization term 0!");
				normalizer = 1.0;
			}
			for (int j = 0; j < xNew.length; j++) {
				xNew[j] = xNew[j]/normalizer;
			}
			iterationCount++;
			System.out.println(iterationCount);
		}
		// xNew is the stationary state prbability vector(pageRanks)
		int[] sortedDocNums = IntStream.range(0, xNew.length)
						.boxed().sorted((i,j) -> Double.compare(xNew[j], xNew[i]) )
						.mapToInt(ele -> ele).toArray();
		// print the 30 highest ranked pages(Docs)
		System.out.println("The top 30 ranked Documents are:");
		for (int p = 0; p < 30; p++) {
			int docNum = sortedDocNums[p];
			double pr = xNew[docNum];
			String docID = this.docName[docNum];

			System.out.print(docID);
			System.out.print(", ");
			System.out.print(pr);
			System.out.println();
		}
		// write to file
		try {
			writeRankToFile(xNew);	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private void readTopThirty() throws IOException {
		// read davis to 30
		File file = new File( "src/main/java/pagerank/davis_top_30.txt" );
		FileReader freader = new FileReader(file);
		try (BufferedReader br = new BufferedReader(freader)) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] data = line.split(": ");
				this.topThirtyRanking.put(Integer.valueOf(data[0]), Double.valueOf(data[1]));
			}
		}
		freader.close();
	}

	private HashMap<String, String> readTitle(String filePath) throws IOException {
		HashMap<String, String> numNameMap = new HashMap<String, String>();
		File file = new File( filePath );
        FileReader freader = new FileReader(file);
        try (BufferedReader br = new BufferedReader(freader)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(";");
                numNameMap.put(data[0], data[1]);
            }
        }
        freader.close();
		return numNameMap;
	}

	private void writeRankToFile(double[] prArray) throws IOException {
		HashMap<String, String> mapping = readTitle("src/main/java/pagerank/davisTitles.txt");
		FileOutputStream fout = new FileOutputStream( "src/main/java/pagerank/docRanks" );
		for (int i = 0; i < prArray.length; i++) {
			double pr = prArray[i];
			String docRankEntry = mapping.get(this.docName[i]) + ";" + pr + "\n";
			fout.write(docRankEntry.getBytes());
		}
        fout.close();
	}

	// too resource (memory) exhaustive!
	/*
	private void getLinkMap(int numberOfDocs) {
		// init
		for (int d = 0; d < numberOfDocs; d++) {
			this.linksToFromMap.put(d, new HashMap<Integer,Boolean>());
		}
		// reverse all docs: to->from
		for (int from = 0; from < numberOfDocs; from++) {
			HashMap<Integer,Boolean> mapToEntry = this.link.get(from);
			if (mapToEntry == null) { // entry not in Map => sink found
				for (int ent = 0; ent < numberOfDocs; ent++) {
					this.linksToFromMap.get(ent).put(from, true);
				}
			} else {
				for (Integer to : mapToEntry.keySet()) {
					this.linksToFromMap.get(to).put(from, true);
				}
			}
			System.out.print("\r"+from);
		}
	}
	*/

	private double vectorDistance (double[] v1, double[] v2) {
		double dist = 0.0;
		try {
			for (int i = 0; i < v1.length; i++) {
				dist += Math.abs(v1[i] - v2[i]);
			}
		} catch (Exception ex) {
			System.out.println(ex);
		}
		return dist;
	}

    /* --------------------------------------------- */


    public static void main( String[] args ) {
	if ( args.length != 1 ) {
	    System.err.println( "Please give the name of the link file" );
	}
	else {
	    new PageRank( args[0] );
	}
    }
}
