import java.io.IOException;
import java.nio.file.Path;

/**
 * Provides a connection and structure for single-threaded and multi-threaded SearchResultHelper 
 */
public interface SearchResultHelperInterface {
	public void traverseQuery(Path queryPath, boolean exact) throws IOException;
	
	public void searchMatches(String line, boolean exact);
	
	public void outputSearchResults(Path path) throws IOException ;
	
}
