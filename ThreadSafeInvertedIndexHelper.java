import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ThreadSafeInvertedIndexHelper {
	
	public static void readDirectory(Path path, ThreadSafeInvertedIndex index, WorkQueue queue) throws IOException {
 		readDirectoryHelper(path, index, queue);
 		queue.finish();
 	}
	/**
 	 * Reads through a directory recursively and a text file and then calls the runnable
 	 * @param path the file used
 	 * @param index the InvertedIndex to add to
 	 * @param queue the WorkQueue
 	 * @throws IOException
 	 */
	public static void readDirectoryHelper(Path path, ThreadSafeInvertedIndex index, WorkQueue queue) throws IOException{
		if(Files.isDirectory(path)) {
			try (DirectoryStream<Path> listing = Files.newDirectoryStream(path)) {
				for (Path file : listing) {
					if (Files.isDirectory(file)) {
						readDirectory(file, index, queue);
					} 
					else if(InvertedIndexHelper.isTextFile(file)) {
						queue.execute(new FileMinion(file, index));
					}
				}
			} 
		}
		else {
			queue.execute(new FileMinion(path, index));
		}
	}
	
	private static class FileMinion implements Runnable {
		private Path files;
		private ThreadSafeInvertedIndex index;
		
		public FileMinion(Path files, ThreadSafeInvertedIndex index) {
			this.files = files;
			this.index = index;
		}
		
		@Override
		public void run() {
			try {
				InvertedIndex local = new InvertedIndex();
				InvertedIndexHelper.readFile(files, local);
				index.addAll(local);	
			} catch(IOException e) {
				System.out.println("Error adding words to the index");
			}
		}
	}
	
}
