import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

public class ThreadSafeSearchResultHelper implements SearchResultHelperInterface {
	private final TreeMap<String, ArrayList<SearchResult>> search;
	private final WorkQueue queue;
	private final ThreadSafeInvertedIndex index;
	
	/**
	 * multi-threaded builder of search results
	 * @param queue the WorkQueue used to multi-thread
	 * @param index the InvertedIndex to search through
	 */
	public ThreadSafeSearchResultHelper(WorkQueue queue, ThreadSafeInvertedIndex index) {
		search = new TreeMap<String, ArrayList<SearchResult>>();
		this.queue = queue;
		this.index = index;
	}
	
	/**
	 * reads through the query file and splits the lines as
	 * queries and then performs either exact or partial search 
	 * @param path the file used
	 * @param boolean decides whether exact or partial search will be acted
	 * @param InvertedIndex the index to perform exact or partial search on
	 * @throws IOException
	 */
	public void traverseQuery(Path queryPath, boolean exact) throws IOException {
		try(BufferedReader in = Files.newBufferedReader(queryPath, StandardCharsets.UTF_8);) {
			String line;
			while((line = in.readLine()) != null) {
				queue.execute(new SearchMinion(line, exact));
			}
			queue.finish();
		}
	}
	
	/**
	 * performs either exact or partial search 
	 * @param line the query or queries
	 * @param boolean decides whether exact or partial search will be acted
	 * @param InvertedIndex the index to perform exact or partial search on
	 * @param results the data structure used to store location, searchresult for queries
	 * @throws IOException
	 */
	public void searchMatches(String line, boolean exact) {
		Stemmer cleaner = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
		String[] stemmedwords = TextParser.parse(line);
		TreeSet<String> queryWords = new TreeSet<String>();
		for(String stems : stemmedwords) {
			queryWords.add(cleaner.stem(stems).toString());
		}
		String queryLine = String.join(" ", queryWords);
		synchronized(this) {
			if(queryWords.isEmpty() || search.containsKey(queryLine)) {
 				return;
 			}
		}
		ArrayList<SearchResult> current;
		if(exact) {
			current = index.exactSearch(queryWords);
		} else {
			current = index.partialSearch(queryWords);
		}
		synchronized(this) {
			search.put(queryLine, current);
		}
	}
	
	/**
	 * outputs index in JSON format
	 * @param path
	 * @throws IOException
	 */
	public synchronized void outputSearchResults(Path outputfilepath) throws IOException {
		Files.createFile(outputfilepath);
		TreeJSONWriter.asQueryTripleNestedObject(search, outputfilepath);
	}
	
	private class SearchMinion implements Runnable {
		private String line;
		private boolean exact;
		
		public SearchMinion(String line, boolean exact) {
			this.line = line;
			this.exact = exact;
		}
		
		@Override
		public void run() {
			try {
				searchMatches(line, exact);
			} catch(Exception e) {
				System.out.println("Error runing task for: " + Thread.currentThread());
			}
		}
	}
}

