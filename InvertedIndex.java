import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class InvertedIndex {
		
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> index;
	private final TreeMap<String, Integer> totalWords;
	
	/**
	 * Initializes the inverted index.
	 */
	public InvertedIndex() {
		this.index = new TreeMap<String, TreeMap<String, TreeSet<Integer>>>();
		this.totalWords = new TreeMap<String, Integer>();
	}
		
	/**
	 * adds word to the inverted index at specific position
	 * @param word the words being added
	 * @param document the file the word is in
	 * @param position the positioning of the word in the document
	 */
	public void addWord(String word, String document, int position) {
		index.putIfAbsent(word, new TreeMap<>());
 		index.get(word).putIfAbsent(document, new TreeSet<>());
 		if(index.get(word).get(document).add(position)) {
 			totalWords.put(document, totalWords.getOrDefault(document, 0) + 1);
 		}
	}
	
	/**
	 * performs an exact search on a word in the inverted index.
	 * @param queryWords the words in the query
	 * @return an ArrayList of the searchresults from the SearchResult class 
	 */
	public ArrayList<SearchResult> exactSearch(TreeSet<String> queryWords) {
		ArrayList<SearchResult> finalResult = new ArrayList<SearchResult>();
		HashMap<String, SearchResult> searchResults = new HashMap<>();
		for(String word : queryWords) {
			if (index.containsKey(word)) {
				searchStorer(word, searchResults, finalResult);
			}
		}		
		Collections.sort(finalResult);
		return finalResult;
	}

	/**
	 * performs an partial search on a word in the inverted index.
	 * @param queryWords the words in the query
	 * @return an ArrayList of the searchresults from the SearchResult class 
	 */
	public ArrayList<SearchResult> partialSearch(TreeSet<String> queryWords) {
		ArrayList<SearchResult> finalResult = new ArrayList<SearchResult>();
		HashMap<String, SearchResult> searchResults = new HashMap<>();
		for(String word : queryWords) {
			for(String words: index.tailMap(word).keySet()) {
				if(words.startsWith(word)) {
					searchStorer(words, searchResults,finalResult);
				} else {
					break;
				}
			}
		}
		Collections.sort(finalResult);
		return finalResult;
	}
	
	/**
	 * checks if location exists and updates count and score
	 * @param queryWord the words for the searchresult
	 * @param searchResult TreeMap of location and searchResult
	 */
	private void searchStorer(String queryWord, HashMap<String, SearchResult> searchResult, ArrayList<SearchResult> finalResults) {	
		for(String location : index.get(queryWord).keySet()) {	
			int count = index.get(queryWord).get(location).size();
			if(searchResult.containsKey(location)) {
				searchResult.get(location).updateCount(count);
			} else {
				
				SearchResult result = new SearchResult(location, count, totalWords.get(location));
				searchResult.put(location, result);	
				finalResults.add(result);
			}
		}	
	}
	
	/**
	 * adds all words and locations from InvertedIndex local to InvertedIndex index
	 * @param local the InvertedIndex to add into index
	 */
	public void addAll(InvertedIndex local) {
		for(String word: local.index.keySet()) {
			if(this.index.containsKey(word)) {
				for(String path: local.index.get(word).keySet()) {
					TreeSet<Integer> integers = local.index.get(word).get(path);
					if(index.get(word).containsKey(path)) {
						this.index.get(word).get(path).addAll(integers);
					} else {
						this.index.get(word).put(path, integers);
					}
				}	
			} else {
				this.index.put(word, local.index.get(word));
			}
		}
	
		for(String locations: local.totalWords.keySet()) {
			if(!this.totalWords.containsKey(locations)) {
				this.totalWords.put(locations, local.totalWords.get(locations));
			} else {
				Integer locationCount = this.totalWords.get(locations);
				locationCount += local.totalWords.get(locations);
				this.totalWords.put(locations, locationCount);
			}
		}
	}
	
	/**
	 * outputs the InvertedIndex index
	 * @param path the outfilepath
	 * @throws IOException
	 */
	public void output(Path outputfilepath) throws IOException {
		TreeJSONWriter.asTripleNestedObject(index, outputfilepath);		
	}
	
	/**
	 * outputs totalWords in JSON format
	 * @param path the outfilepath
	 * @throws IOException
	 */
	public void countWords(Path file) throws IOException {
		Files.createFile(file);
		TreeJSONWriter.asObject(totalWords, file);	
	}
	
	/**
	 * Tests if index contains word
	 * @param word  the word in document
	 * @return true if word is in inverted index "index"
	 */
	public boolean contains(String word) {
		return index != null && index.containsKey(word);
	}
	
	/**
	 * Tests if index contains location
	 * @param word the word in document
	 * @param location the document
	 * @return true if location is in inverted index "index"
	 */
	public boolean contains(String word, String location) {
		TreeMap<String, TreeSet<Integer>> documents = index.get(word);
		return documents != null && documents.containsKey(location);
	}
	
	/**
	 * Tests if index contains position
	 * @param word the word in document
	 * @param location the document
	 * @param position the positioning of the word
	 * @return true if position is in inverted index "index"
	 */
	public boolean contains(String word, String location, int position) {
		TreeMap<String, TreeSet<Integer>> documents = index.get(word);
		TreeSet<Integer> positions = documents.get(location);
		return contains(word, location) && positions != null && positions.contains(position);
	}
	
	/**
	 * returns number of words stored in the index
	 * @return number of words
	 */
	public int size() {
		if(index != null) {
			return index.size();
		}
		return 0;
	}
	
	/**
	 * returns number of documents stored in the index
	 * @param word the word in the document
	 * @return number of document
	 */
	public int size(String word) {
		TreeMap<String, TreeSet<Integer>> documents = index.get(word);
		if(documents != null) {
			return documents.size();
		}
		return 0;
	}
	
	/**
	 * returns number of positions of a word stored in the index
	 * @param word the word in document
	 * @param location the document
	 * @return number of positions
	 */
	public int size(String word, String location) {
		TreeMap<String, TreeSet<Integer>> documents = index.get(word);
		TreeSet<Integer> positions = documents.get(location);
		if(contains(word, location) && positions != null) {
			return positions.size();
		}
		return 0;
	}
	
	/**
	 * returns a string representation of inverted index "index"
	 */
	public String toString() {
		return index.toString();
	}	
}				
