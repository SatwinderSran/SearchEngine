import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.TreeMap;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class NewCrawlServlet extends HttpServlet {
	private InvertedIndex index;
	private WorkQueue queue;
	
	public NewCrawlServlet(InvertedIndex index, WorkQueue queue) {
		super();
		this.index = index;
		this.queue = queue;
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		
		if (request.getRequestURI().endsWith("favicon.ico")) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		PrintWriter out = response.getWriter();
		String query = request.getParameter("query");
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		String newCrawl = request.getParameter("newcrawl");
		
		if((newCrawl != null)) {
			URL url = new URL(newCrawl);
			ThreadSafeInvertedIndex local = new ThreadSafeInvertedIndex();
			WebCrawler crawler = new WebCrawler(local, queue );
			crawler.seedCrawl(url, 50);
			out.printf("Seed is currently being crawled");
			index.addAll(local);
				
			
		}
	}
}
