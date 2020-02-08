
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.TreeSet;

public class ThreadSafeInvertedIndex extends InvertedIndex {
	private final ReadWriteLock lock;
	
	/**
	 * Creates a new inverted index that supports multi-threading. 
	 */
	public ThreadSafeInvertedIndex() {
		super();
		lock = new ReadWriteLock();
	}
	
	@Override
	public void addWord(String word, String document, int position) {
		lock.lockReadWrite();
		try {
			super.addWord(word, document, position);
		}
		finally {
			lock.unlockReadWrite();
		}
	}
	
	@Override
	public ArrayList<SearchResult> exactSearch(TreeSet<String> queryWords) {
		lock.lockReadOnly();
		try {
			return super.exactSearch(queryWords);
		}
		finally {
			lock.unlockReadOnly();
		}
	}
	
	@Override
	public ArrayList<SearchResult> partialSearch(TreeSet<String> queryWords) {
		lock.lockReadOnly();
		try {
			return super.partialSearch(queryWords);
		}
		finally {
			lock.unlockReadOnly();
		}
	}
	
	@Override
	public void addAll(InvertedIndex local) {
		lock.lockReadWrite();
		try {
			super.addAll(local);
		}
		finally {
			lock.unlockReadWrite();
		}
	}
	
	@Override
	public void output(Path outputfilepath) throws IOException {
		lock.lockReadOnly();
		try {
			super.output(outputfilepath);
		}
		finally {
			lock.unlockReadOnly();
		}
	}
	
	@Override
	public void countWords(Path file) throws IOException {
		lock.lockReadOnly();
		try {
			super.countWords(file);
		}
		finally {
			lock.unlockReadOnly();
		}
	}
	
	@Override
	public boolean contains(String word) {
		lock.lockReadOnly();
		try {
			return super.contains(word);
		}
		finally {
			lock.unlockReadOnly();
		}
	}
	
	@Override
	public boolean contains(String word, String location) {
		lock.lockReadOnly();
		try {
			return super.contains(word, location);
		}
		finally {
			lock.unlockReadOnly();
		}
	}
	
	@Override
	public boolean contains(String word, String location, int position) {
		lock.lockReadOnly();
		try {
			return super.contains(word, location, position);
		}
		finally {
			lock.unlockReadOnly();
		}
	}
	
	@Override
	public int size() {
		lock.lockReadOnly();
		try {
			return super.size();
		}
		finally {
			lock.unlockReadOnly();
		}
	}
	
	@Override
	public int size(String word) {
		lock.lockReadOnly();
		try {
			return super.size(word);
		}
		finally {
			lock.unlockReadOnly();
		}
	}
	
	@Override
	public int size(String word, String location) {
		lock.lockReadOnly();
		try {
			return super.size(word, location);
		}
		finally {
			lock.unlockReadOnly();
		}
	}
	
	@Override
	public String toString() {
		lock.lockReadOnly();
		try {
			return super.toString();
		}
		finally {
			lock.unlockReadOnly();
		}
	}
}
