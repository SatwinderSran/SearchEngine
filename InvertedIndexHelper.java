import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

public class InvertedIndexHelper {
	
	/**
	 * reads through the directory recursively to make sure path leads to 
	 * a file and not a sub-directory
	 * @param path the file used
	 * @throws IOException
	 */
	public static void readDirectory(Path path, InvertedIndex index) throws IOException{
		if(Files.isDirectory(path)) {
			try(DirectoryStream<Path> listing = Files.newDirectoryStream(path)) {
				for(Path file : listing) {
					if(Files.isDirectory(file)) {
						readDirectory(file, index);
					} 
					else if(isTextFile(file)) {
						readFile(file, index);
					}
				}
			} 
		}	
	}
	
	/**
	 * Checks if the file ends with the .txt or .text extension (case-insensitive).
	 * @param path the file
	 * @return true if file has proper extension
	 */
	public static boolean isTextFile(Path path) {
		String fileName = path.getFileName().toString().toLowerCase();
		return ((Files.isReadable(path) && (fileName.endsWith(".text")) || fileName.endsWith(".txt")));
	}
	
	/**
	 * reads and stem files while displaying position
	 * adds stemmed words to an inverted index
	 * @param path the file
	 * @throws IOException
	 */
	public static void readFile(Path path, InvertedIndex index) throws IOException {
		int position = 1;
		try(BufferedReader in = Files.newBufferedReader(path, StandardCharsets.UTF_8);) {
			String line = null;
			String pathName = path.toString();
			Stemmer cleaner = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
			while((line = in.readLine()) != null) {
				for(String stemmedWord : TextParser.parse(line)) {
					index.addWord(cleaner.stem(stemmedWord).toString(), pathName, position++);
				}
			}
		} 
	}
}
