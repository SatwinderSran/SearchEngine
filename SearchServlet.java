import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.text.StringEscapeUtils;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;;

@SuppressWarnings("serial")
public class SearchServlet extends HttpServlet {
	InvertedIndex index;
	WorkQueue queue;
	boolean exact;
	
	public SearchServlet(InvertedIndex index, WorkQueue queue) {
		this.index = index;
		this.queue = queue;
		exact = false;
	}
	
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		HttpSession session = request.getSession();
		PrintWriter out = response.getWriter();
		
		if (request.getRequestURI().endsWith("favicon.ico")) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		out.printf("<html>%n");
		out.printf("</head>%n");
		out.printf("<img id='logo' src='http://www.craiglotter.co.za/wp-content/uploads/2010/01/yabigo-logo.png'>");
		out.printf("<h1><em>%s</em></h1>%n", "It's YahooBingGoogle");
		out.printf("<title>%s</title>", "Search Engine");
		out.printf("<body>%n"); 
		out.printf("<h1>%s</h1>", "Query Search"); 
		out.printf("<form>");
		out.printf("<label>%s</label>%n", "Query:");      
		out.printf("<input type=\"text\" name=\"query\" value=\"%s\"> %n", request.getParameter("query")); 
		out.printf("<br/><br>%n");   
		out.printf("<label>Advanced: </label>");
		out.printf("<br>");
		out.printf("Exact\t");
		out.printf("<input type=\"checkbox\" name=\"Exact\" value=\"ON\">");
		out.printf("<br>");
		out.printf("Show History\t");
		out.printf("<input type=\"checkbox\" name=\"showhistory\" value=\"ON\" checked>");
		out.printf("<br>");
		out.printf("Clear History\t");
		out.printf("<input type=\"checkbox\" name=\"clearhistory\" value=\"ON\">");
		out.printf("<br>");
		out.printf("Show URL History\t");
		out.printf("<input type=\"checkbox\" name=\"urlhistory\" value=\"ON\" checked>");
		out.printf("<br>");
		out.printf("Clear URL history\t");
		out.printf("<input type=\"checkbox\" name=\"clearURLhist\" value=\"ON\">");
		out.printf("<br>");
		out.printf("Show Favorites\t");
		out.printf("<input type=\"checkbox\" name=\"showFavs\" value=\"ON\" checked>");
		out.printf("<br>");
		out.printf("Clear Favorites\t");
		out.printf("<input type=\"checkbox\" name=\"clearFavs\" value=\"ON\">");
		out.printf("<br>");
		out.printf("Private\t");
		out.printf("<input type=\"checkbox\" name=\"priv\" value=\"ON\">");
		out.printf("<br>");
		out.printf("Suggested Queries\t");
		out.printf("<input type=\"checkbox\" name=\"suggested\" value=\"ON\" checked>");
		out.printf("<br>");
		out.printf("<label>%s</label>", "New Crawl:");
		out.printf("<input type=\"text\" name=\"newcrawl\" value=\"%s\"> %n", request.getParameter("newcrawl"));
		out.printf("<input type='submit'/>");  
																		
		String query = request.getParameter("query");
		String newCrawl = request.getParameter("newcrawl");
		ArrayList<String> visited = new ArrayList<>();
		ArrayList<String> favorites = new ArrayList<>();
		ArrayList<String> history = new ArrayList<>();
		String exactSearch = request.getParameter("Exact");
		String privateMode = request.getParameter("priv");
		
		if(session!=null && session.getAttribute("VisitedResults")!= null) {
			visited = (ArrayList<String>) session.getAttribute("VisitedResults");
		}
		
		if(session!=null && session.getAttribute("viewhistory")!= null) {
			history = (ArrayList<String>) session.getAttribute("viewhistory");
		}
		
		if(session!=null && session.getAttribute("FavoriteResults")!= null) {
			favorites = (ArrayList<String>) session.getAttribute("FavoriteResults");
		}
		
		session.setAttribute("exactSearching", exactSearch);						  
		if(session!=null && session.getAttribute("exactSearching")!= null) {
			exactSearch = (String) session.getAttribute("exactSearching");
		}
		
		session.setAttribute("privateSearch", privateMode);
		if(session!=null && session.getAttribute("privateSearch")!= null) {
			privateMode = (String) session.getAttribute("privateSearch");
		}
								
		if((query != null) && (!query.isEmpty())) {							
			if((exactSearch != null) && (exactSearch.equals("ON"))) {
				exact = true;
			} else {
				exact = false;
			}
									
			TreeMap<String, ArrayList<SearchResult>> queryPage = search(query, exact);
			for(String queries: queryPage.keySet()) {
				if(privateMode == null) {
					history.add(queries);
				}
				ArrayList<SearchResult> list = queryPage.get(queries);
				if(list.size() == 0) {
					out.printf("<br>");
					out.printf("No results for this Query");
				} else {
					for(SearchResult result: list) {
						out.printf("<p><a href=" + result.getLocation()+ ">" + result.getLocation() + "</a>");
						out.printf("<br/>");
						if((privateMode == null)) {
							visited.add(result.getLocation());
						}
					}
					out.printf("<br>");
					out.printf("<label>%s</label>%n", "Favorite:");
					out.printf("<input type=\"text\" name=\"Favorite\" value=\"%s\"> %n", request.getParameter("Favorite"));

					String favorite = request.getParameter("Favorite");
					favorite = StringEscapeUtils.escapeHtml4(favorite);
					if((favorite != null) && (!favorite.isEmpty())) {
						favorites.add(favorite);									
					}
				}
			}
		}
		
		if((newCrawl != null) && (!newCrawl.isEmpty())) {	
			try{	
				String newCrawl2 = StringEscapeUtils.escapeHtml4(newCrawl);
				URL url = new URL(newCrawl2);
				ThreadSafeInvertedIndex local = new ThreadSafeInvertedIndex();
				WebCrawler crawler = new WebCrawler(local, queue);
				crawler.seedCrawl(url, 50);
				out.printf("<br>");
				out.printf("Seed is currently being crawled");
				index.addAll(local);	
			} catch (MalformedURLException e) {
				out.printf("<br>");
				out.printf("Invalid link");
			}
		}
		
		if((request.getParameter("showhistory") != null) && request.getParameter("showhistory").equals("ON")) {		
			out.printf("<br><br>");
			out.printf("History");
			out.printf("<br>");
			for(String h: history) {
				out.printf(h);
				out.printf("<br>");
			}
		}
		
		if((request.getParameter("clearhistory") != null) && request.getParameter("clearhistory").equals("ON")) {
			history = new ArrayList<String>();
		}
		
		if((request.getParameter("urlhistory") != null) && request.getParameter("urlhistory").equals("ON")) {
			out.printf("<br>");
			out.printf("URL History");
			out.printf("<br>");
			for(String v: visited) {
				out.printf(v);
				out.printf("<br>");
			}
		}
		
		if((request.getParameter("clearURLhist") != null) && request.getParameter("clearURLhist").equals("ON")) {
			visited = new ArrayList<String>();
		}
		
		if((request.getParameter("showFavs") != null) && request.getParameter("showFavs").equals("ON")) {
			out.printf("<br>");
			out.printf("Favorites");
			out.printf("<br>");
			for(String f: favorites) {
				out.printf(f);
				out.printf("<br>");
			}
		}
		
		if((request.getParameter("clearFavs") != null) && request.getParameter("clearFavs").equals("ON")) {
			favorites = new ArrayList<String>();
		}
		
		if((request.getParameter("suggested") != null) && request.getParameter("suggested").equals("ON")) {
			out.printf("<br>");
			out.printf("Suggested");
			out.printf("<br>");
			if(history.size() <= 5) {
				for(int i = 0; i < history.size(); i++) {
					out.printf(history.get(i));
					out.printf("<br>");
				}
			} else {
				for(int i = history.size()-1; i > (history.size() - 6) ; i--) {
					out.printf(history.get(i));
					out.printf("<br>");
				}
			}
		}
		session.setAttribute("FavoriteResults", favorites);
		session.setAttribute("viewhistory", history);
		session.setAttribute("VisitedResults", visited);										
		out.printf("</form>");
		out.printf("</body>"); 		
		out.printf("<html>%n");	
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
	}
	/**
	 * Searches through inverted index for query
	 * @param query the word to be searched for
	 * @param exact value for partial or exact search
	 * @return
	 */
	private TreeMap<String, ArrayList<SearchResult>> search(String query, Boolean exact) {
		query = StringEscapeUtils.escapeHtml4(query);
		TreeMap<String, ArrayList<SearchResult>> results = new TreeMap<String, ArrayList<SearchResult>>();
		Stemmer cleaner = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
		String[] stemmedwords = TextParser.parse(query);
		TreeSet<String> queryWords = new TreeSet<String>();
		for(String stems : stemmedwords) {
			queryWords.add(cleaner.stem(stems).toString());
		}
		String queryLine = String.join(" ", queryWords);
		synchronized(this) {
			if(queryWords.size() != 0 && !results.containsKey(queryLine)) {
				ArrayList<SearchResult> current;
				if(exact) {
					current = index.exactSearch(queryWords);
				} else {
					current = index.partialSearch(queryWords);
				}
				synchronized(this) {
					results.put(queryLine, current);
				}
			}
		}
		return results;
	}	
}
