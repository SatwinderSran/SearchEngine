import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionIdManager;
import org.eclipse.jetty.server.session.DefaultSessionIdManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class Driver {

	/**
	 * Parse the command-line arguments to build and use an in-memory search
	 * engine from files or the web.
	 * @param args the command-line arguments to parse
	 * @return 0 if everything went well
	 */
	public static void main(String[] args) {
		ArgumentMap map = new ArgumentMap(args);
		int threads = 0;
		String thread = null;
 		InvertedIndex index;
 		SearchResultHelperInterface helper;
 		ThreadSafeInvertedIndex threadSafe = null;
 		WorkQueue queue = null;
 		WebCrawler crawler = null;
 		
 		if(map.hasFlag("-threads") || map.hasFlag("-url") || map.hasFlag("-port")) {
 			if(map.hasValue("-threads")) {
				thread = map.getString("-threads");
			} else {
				thread = "5";
			}
			try {
				threads = Integer.parseInt(thread);
			} catch(NumberFormatException e) {
				System.err.println("Invalid input");
			}
			if(threads <= 0) {
				threads = 5;
			}
			queue = new WorkQueue(threads);
 			threadSafe = new ThreadSafeInvertedIndex();
 			index = threadSafe;
 			helper = new ThreadSafeSearchResultHelper(queue, threadSafe);
 			crawler = new WebCrawler(threadSafe, queue);
 		} else {
 			index = new InvertedIndex();
 			helper = new SearchResultHelper(index);
 		}

 		try {
 			if(map.hasValue("-path")) {
 				Path lanes = map.getPath("-path");
 				if (threadSafe != null) {
 					ThreadSafeInvertedIndexHelper.readDirectory(lanes, threadSafe, queue);
 				}
 				else {
 					if(Files.isDirectory(lanes)) {
						InvertedIndexHelper.readDirectory(lanes, index);
					} else {
						InvertedIndexHelper.readFile(lanes, index);
					}
 				}
 			}
 		} catch(IOException e) {
 			System.out.println("Error reading file or value is missing" + map.getString("-path"));
 		}
 		
 		try {
			if(map.hasFlag("-url")) {
				if(map.hasValue("-url")) {
					crawler.seedCrawl(new URL(map.getString("-url")), Integer.parseInt(map.getString("-limit", "50")));
				}
			}
		} catch (IOException e) {
			System.out.println("error crawling url");
		} catch(NumberFormatException e) {
			System.err.println("Not a number");
		}
 		
 		try {
 			if(map.hasFlag("-index")) {
 				Path path = map.getPath("-index", Paths.get("index.json"));
 				index.output(path);
 			}
 		} catch(IOException e) {
 			System.out.println("Error writing file" + map.getString("-index"));
 		}
 	
 		try {
 			if(map.hasValue("-search")) {
 				if(index.size() > 0) {
 					Path searchLane = map.getPath("-search");
 					if(Files.exists(searchLane)) {
 						helper.traverseQuery(searchLane, map.hasFlag("-exact"));
 					}
 				}
 			}
 		} catch(IOException e) {
 			System.out.println("Error reading query File" + map.hasFlag("-search"));
 		}
 	
 		try {
 			if(map.hasFlag("-results")) {
 				Path path = map.getPath("-results", Paths.get("results.json"));
 				helper.outputSearchResults(path);
 			}
 		} catch(IOException e) {
 			System.out.println("Error writing file" + map.getString("-results"));
 		} 	
 		
 		try {
 			if(map.hasFlag("-locations")) {
 				index.countWords(map.getPath("-locations"));
 			}
 		} catch(IOException e) {
 			System.out.println("Error writing file" + map.getString("-locations"));
 		}
 		
 		if(map.hasFlag("-port")) {
 			Server server;
 			if(map.hasValue("-port")) {
 				server = new Server(Integer.parseInt(map.getString("-port", "8080")));
 				ServletHandler handler = new ServletHandler();
 				handler.addServletWithMapping(new ServletHolder(new SearchServlet(index, queue)), "/");
 				SessionIdManager idmanager = new DefaultSessionIdManager(server);
 			    server.setSessionIdManager(idmanager);
 			    SessionHandler sessionsHandler = new SessionHandler();       
 			    handler.setHandler(sessionsHandler);    
 				try {
 					server.setHandler(handler);
					server.start();
					server.join();
				} catch (Exception e) {
					System.out.println("Error joining server at port" + map.getString("-port","8080"));
				}
 			}
 		}
 		
 		if (queue != null) {
 			queue.shutdown();
 		}
	}
} 

		