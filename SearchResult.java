public class SearchResult implements Comparable<SearchResult> {
	private final String location;
	private int count;
	private double score;
	private final int total;
	
	/**
	 * initializes the SearchResult object
	 * @param location the document
	 * @param count the amount of times the word is in a document
	 * @param score the count/total
	 */
	public SearchResult(String location, int count, int total) {
		this.location = location;
		this.count = count;
		this.total = total;
		score = (double) count / total;
	}
	
	/**
	 * gets the location
	 * @return location the document
	 */
	public String getLocation() {
		return location;	
	}
	
	/**
	 * gets the count
	 * @return count the total number of times a word appears
	 */
	public int getCount() {
		return count;
	}
	
	/**
	 * gets the score
	 * @return score the count/total words
	 */
	public double getScore() {
		return score;
	}
	
	/**
	 * sets the count
	 * @param counts the count
	 */
	public void setCount(int counts) {
		this.count = counts;
		score = (double) count / total;
	}
	
	/**
	 * updates count
	 * @param count the int to be added on to the current count
	 */
	public void updateCount(int count) {
		this.count += count;
		score = (double) this.count / this.total;
	}
	
	@Override
	public int compareTo(SearchResult other) {
		
		int result = Double.compare(other.score, this.score);
		if(result != 0) {
			return result;
		} else {
			int count = Integer.compare(other.count,this.count);
			if(count != 0) {
				return count;
			} else {
				return this.location.compareToIgnoreCase(other.getLocation());
			}	
		}
	}
}