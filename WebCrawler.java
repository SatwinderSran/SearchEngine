import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

public class WebCrawler {
	private final ThreadSafeInvertedIndex index;
	private HashSet<String> urls;
	private final WorkQueue minions;
	private int limit;
	
	/**
	 * Initializes index, minions, and urls
	 * @param index the InvertedIndex
	 * @param minions the WorkQueue
	 */
	public WebCrawler(ThreadSafeInvertedIndex index, WorkQueue minions) {
		this.index = index;
		urls = new HashSet<String>();
		this.minions = minions;
	}
	/**
	 * Initializes seed and limit. Called in driver
	 * @param seed the original URL containing all links
	 * @param limit the total links to be parsed
	 */
	public void seedCrawl(URL seed, int limit) {
		this.limit = limit;
		checkCrawl(seed.toString());
		minions.finish();
	}
	/**
	 * Adds new URLs to the HashSet urls and then create a new work minion for 
	 * the url
	 * @param url the url to be processed
	 */
	public void checkCrawl(String url) {
		if(!urls.contains(url)) {
			urls.add(url);
			minions.execute(new CrawlMinion(url));
		}
	}
	/**
	 * Adds words into the index from the html which is already stripped.
	 * @param url the location
	 * @param html the text locating the words
	 * @param index the inverted index to add to
	 */
	public void addURLWords(String url, String html, InvertedIndex index)
	{
		int position = 1;
		Stemmer cleaner = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
		String[] stemmedwords = TextParser.parse(html);
		for(String stems : stemmedwords) {
			index.addWord(cleaner.stem(stems).toString(), url, position++);
		}
	}
	
	private class CrawlMinion implements Runnable
	{
		private String url;
		public CrawlMinion(String url)
		{
			this.url = url;
		}

		@Override
		public void run() {
			try {
				InvertedIndex local = new InvertedIndex();
				String html = HTMLFetcher.fetchHTML(url, 3);
				String html2 = HTMLCleaner.stripHTML(html);
				addURLWords(url, html2, local);
				index.addAll(local);
				URL url2 = new URL(url);
				ArrayList<URL> links = LinkParser.listLinks(url2, html);
				for(URL absolute : links) {
					if(urls.size() < limit) {
						checkCrawl(absolute.toString());
					}
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}		
	}
	
}
