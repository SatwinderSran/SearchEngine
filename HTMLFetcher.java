import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

public class HTMLFetcher {

	/**
	 * Given a map of headers (as returned either by {@link URLConnection#getHeaderFields()}
	 * or by {@link HttpsFetcher#fetchURL(URL)}, determines if the content type of the
	 * response is HTML.
	 *
	 * @param headers map of HTTP headers
	 * @return true if the content type is html
	 *
	 * @see URLConnection#getHeaderFields()
	 * @see HttpsFetcher#fetchURL(URL)
	 */
	public static boolean isHTML(Map<String, List<String>> headers) {
		if(headers.containsKey("Content-Type")) {
			List<String> heads=headers.get("Content-Type");
			for(String heading: heads) {
				if(heading.contains("text/html")) {
					return true;
				}
			}
		} 
		return false;
	}

	public static void main(String[] args) {
		
	}
	/**
	 * Given a map of headers (as returned either by {@link URLConnection#getHeaderFields()}
	 * or by {@link HttpsFetcher#fetchURL(URL)}, returns the status code as an int value.
	 * Returns -1 if any issues encountered.
	 *
	 * @param headers map of HTTP headers
	 * @return status code or -1 if unable to determine
	 *
	 * @see URLConnection#getHeaderFields()
	 * @see HttpsFetcher#fetchURL(URL)
	 */
	public static int getStatusCode(Map<String, List<String>> headers) {
		for(String k : headers.keySet())
			System.out.println(k + " \t "+headers.get(k));
		
		if(headers.containsKey(null)) {
			List<String> heads=headers.get(null);
			for(String heading: heads) {
				if(heading.contains("HTTP/1.1")) {
					String[] parts = heading.split(" ");
					return Integer.parseInt(parts[1]);
				}
			}
		} 
		return -1;
	}

	/**
	 * Given a map of headers (as returned either by {@link URLConnection#getHeaderFields()}
	 * or by {@link HttpsFetcher#fetchURL(URL)}, returns whether the status code
	 * represents a redirect response *and* the location header is properly included.
	 *
	 * @param headers map of HTTP headers
	 * @return true if the HTTP status code is a redirect and the location header is non-empty
	 *
	 * @see URLConnection#getHeaderFields()
	 * @see HttpsFetcher#fetchURL(URL)
	 */
	public static boolean isRedirect(Map<String, List<String>> headers) {
		
		if(!headers.containsKey("Location")) {
				return false;
		} else {
			return true;
		}		
	}
	//for(String k : headers.keySet())
	//	System.out.println(k + " \t "+headers.get(k));
	/**
	 * Uses {@link HttpsFetcher#fetchURL(URL)} to fetch the headers and content of the
	 * specified url. If the response was HTML, returns the HTML as a single {@link String}.
	 * If the response was a redirect and the value of redirects is greater than 0, will
	 * return the result of the redirect (decrementing the number of allowed redirects).
	 * Otherwise, will return {@code null}.
	 *
	 * @param url the url to fetch and return as html
	 * @param redirects the number of times to follow a redirect response
	 * @return the html as a single String if the response code was ok, otherwise null
	 * @throws IOException
	 *
	 * @see #isHTML(Map)
	 * @see #getStatusCode(Map)
	 * @see #isRedirect(Map)
	 */
	public static String fetchHTML(URL url, int redirects) throws IOException {
		Map<String, List<String>> results = HttpsFetcher.fetchURL(url);
		if(isHTML(results) && getStatusCode(results) == 200) {
			List<String> html = results.get("Content");
			String jack =String.join("\n", html);
			return jack;
		}
		else if(isRedirect(results)) {	
			while(isRedirect(results) && redirects > 0) {
				String locations = results.get("Location").toString();
				return fetchHTML(locations.substring(1, locations.length()-1) ,redirects-1);	
			}
		}
		return null;
	}
	

	/**
	 * @see #fetchHTML(URL, int)
	 */
	public static String fetchHTML(String url) throws IOException {
		return fetchHTML(new URL(url), 0);
	}

	/**
	 * @see #fetchHTML(URL, int)
	 */
	public static String fetchHTML(String url, int redirects) throws IOException {
		return fetchHTML(new URL(url), redirects);
	}

	/**
	 * @see #fetchHTML(URL, int)
	 */
	public static String fetchHTML(URL url) throws IOException {
		return fetchHTML(url, 0);
	}

}
