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

public class SearchResultHelper implements SearchResultHelperInterface {
	private final TreeMap<String, ArrayList<SearchResult>> results;
	private final InvertedIndex index;
	
	/**
	 * builds and stores a list of search words
	 * @param index the InvertedIndex to search through
	 */
	public SearchResultHelper(InvertedIndex index) {
		this.index = index;
		results = new TreeMap<String, ArrayList<SearchResult>>();
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
			String line = null;
			while((line = in.readLine()) != null) {
				searchMatches(line, exact);
			}
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
		if(queryWords.size() != 0 && !results.containsKey(queryLine)) {
			if (exact) {
				results.put(queryLine, index.exactSearch(queryWords));
			} else {
				results.put(queryLine, index.partialSearch(queryWords));
			}
		}
	}
	
	/**
	 * outputs index in JSON format
	 * @param path
	 * @throws IOException
	 */
	public void outputSearchResults(Path path) throws IOException {
			TreeJSONWriter.asQueryTripleNestedObject(results, path);
	}
}
