import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

public class TreeJSONWriter {

	/**
	 * Writes several tab <code>\t</code> symbols using the provided
	 * {@link Writer}.
	 * @param times  the number of times to write the tab symbol
	 * @param writer the writer to use
	 * @throws IOException if the writer encounters any issues
	 */
	public static void indent(int times, Writer writer) throws IOException {
		for (int i = 0; i < times; i++) {
			writer.write('\t');
		}
	}

	/**
	 * Writes the element surrounded by quotes using the provided {@link Writer}.
	 *
	 * @param element the element to quote
	 * @param writer  the writer to use
	 * @throws IOException if the writer encounters any issues
	 */
	public static void quote(String element, Writer writer) throws IOException {
		writer.write('"');
		writer.write(element);
		writer.write('"');
	}

	/**
	 * Returns the set of elements formatted as a pretty JSON array of numbers.
	 *
	 * @param elements the elements to convert to JSON
	 * @return {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asArray(TreeSet, Writer, int)
	 */
	public static String asArray(TreeSet<Integer> elements) {
		try {
			StringWriter writer = new StringWriter();
			asArray(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the set of elements formatted as a pretty JSON array of numbers to
	 * the specified file.
	 *
	 * @param elements the elements to convert to JSON
	 * @param path     the path to the file write to output
	 * @throws IOException if the writer encounters any issues
	 */
	public static void asArray(TreeSet<Integer> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path,
				StandardCharsets.UTF_8)) {
			asArray(elements, writer, 0);
		}
	}

	/**
	 * Writes the set of elements formatted as a pretty JSON array of numbers
	 * using the provided {@link Writer} and indentation level.
	 *
	 * @param elements the elements to convert to JSON
	 * @param writer   the writer to use
	 * @param level    the initial indentation level
	 * @throws IOException if the writer encounters any issues
	 *
	 * @see Writer#write(String)
	 * @see Writer#append(CharSequence)
	 *
	 * @see System#lineSeparator()
	 *
	 * @see #indent(int, Writer)
	 */
	public static void asArray(TreeSet<Integer> elements, Writer writer,
			int level) throws IOException {
		writer.write('[');
		writer.write(System.lineSeparator());
		if(elements.size() > 0) {
			for(Integer element : elements.headSet(elements.last())) {
				indent(level + 1, writer);
				writer.write(element.toString());
				writer.write(",");
				writer.write(System.lineSeparator());
			}
			indent(level + 1, writer);
			writer.write(elements.last().toString());
			writer.write(System.lineSeparator());
			indent(level, writer);
		} else if(elements.size() == 0){
			indent(level, writer);
		}
		writer.write(']');
	}

	/**
	 * Returns the map of elements formatted as a pretty JSON object.
	 *
	 * @param elements the elements to convert to JSON
	 * @return {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asObject(TreeMap, Writer, int)
	 */
	public static String asObject(TreeMap<String, Integer> elements) {
		try {
			StringWriter writer = new StringWriter();
			asObject(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the map of elements formatted as a pretty JSON object to
	 * the specified file.
	 *
	 * @param elements the elements to convert to JSON
	 * @param path     the path to the file write to output
	 * @throws IOException if the writer encounters any issues
	 *
	 * @see #asObject(TreeMap, Writer, int)
	 */
	public static void asObject(TreeMap<String, Integer> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path,
				StandardCharsets.UTF_8)) {
			asObject(elements, writer, 0);
		}
	}

	/**
	 * Writes the map of elements as a pretty JSON object using the provided
	 * {@link Writer} and indentation level.
	 *
	 * @param elements the elements to convert to JSON
	 * @param writer   the writer to use
	 * @param level    the initial indentation level
	 * @throws IOException if the writer encounters any issues
	 *
	 * @see Writer#write(String)
	 * @see Writer#append(CharSequence)
	 *
	 * @see System#lineSeparator()
	 *
	 * @see #indent(int, Writer)
	 * @see #quote(String, Writer)
	 */
	public static void asObject(TreeMap<String, Integer> elements, Writer writer,
			int level) throws IOException {
		writer.write('{');
		writer.write(System.lineSeparator());
		
		if(elements.size() > 0){
			for(String key : elements.headMap(elements.lastKey()).keySet()) {
				indent(level + 1, writer);
				quote(key, writer);
				writer.write(": ");
				writer.write(elements.get(key).toString());
				writer.write(",");
				writer.write(System.lineSeparator());
			}
			indent(level + 1, writer);
			quote(elements.lastKey(), writer);
			writer.write(": ");
			writer.write(elements.get(elements.lastKey()).toString());
			writer.write(System.lineSeparator());
		}
		writer.write('}');
	}

	/**
	 * Returns the nested map of elements formatted as a nested pretty JSON object.
	 *
	 * @param elements the elements to convert to JSON
	 * @return {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asNestedObject(TreeMap, Writer, int)
	 */
	public static String asNestedObject(TreeMap<String, TreeSet<Integer>> elements) {
		try {
			StringWriter writer = new StringWriter();
			asNestedObject(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the nested map of elements formatted as a nested pretty JSON object
	 * to the specified file.
	 *
	 * @param elements the elements to convert to JSON
	 * @param path     the path to the file write to output
	 * @throws IOException if the writer encounters any issues
	 *
	 * @see #asNestedObject(TreeMap, Writer, int)
	 */
	public static void asNestedObject(TreeMap<String, TreeSet<Integer>> elements,
			Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path,
				StandardCharsets.UTF_8)) {
			asNestedObject(elements, writer, 0);
		}
	}

	/**
	 * Writes the nested map of elements as a nested pretty JSON object using the
	 * provided {@link Writer} and indentation level.
	 *
	 * @param elements the elements to convert to JSON
	 * @param writer   the writer to use
	 * @param level    the initial indentation level
	 * @throws IOException if the writer encounters any issues
	 *
	 * @see Writer#write(String)
	 * @see Writer#append(CharSequence)
	 *
	 * @see System#lineSeparator()
	 *
	 * @see #indent(int, Writer)
	 * @see #quote(String, Writer)
	 *
	 * @see #asArray(TreeSet, Writer, int)
	 */
	public static void asNestedObject(TreeMap<String, TreeSet<Integer>> elements,
			Writer writer, int level) throws IOException {
		writer.write('{');
		writer.write(System.lineSeparator());
		if(elements.size() > 0){
			for(String element : elements.headMap(elements.lastKey()).keySet()) {
				indent(level + 1, writer);
				quote(element, writer);
				writer.write(": ");
				asArray(elements.get(element), writer, level+1);
				writer.write(',');
				writer.write(System.lineSeparator());	
			}
			indent(level + 1, writer);
			quote(elements.lastKey().toString(), writer);
			writer.write(": ");
			asArray(elements.get(elements.lastKey()), writer, level+1);
			writer.write(System.lineSeparator());
			indent(level, writer);
		}
		writer.write('}');
	}
			
	/**
	 * Returns the nested map of elements formatted as a nested pretty JSON object.
	 */
	public static String asTripleNestedObject(TreeMap<String, TreeMap<String, TreeSet<Integer>>> invIndex) {
		try {
			StringWriter writer = new StringWriter();
			asTripleNestedObject(invIndex, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the nested map of elements formatted as a nested pretty JSON object
	 * to the specified file.
	 *
	 * @param elements the elements to convert to JSON
	 * @param path     the path to the file write to output
	 * @throws IOException if the writer encounters any issues
	 *
	 * @see #asNestedObject(TreeMap, Writer, int)
	 */
	public static void asTripleNestedObject(TreeMap<String, TreeMap<String, TreeSet<Integer>>> invIndex,
			Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path,
				StandardCharsets.UTF_8)) {
			asTripleNestedObject(invIndex, writer, 0);
		}
	}
	/**
	 * Writes the nested map of elements as a nested pretty JSON object using the
	 * provided {@link Writer} and indentation level.
	 *
	 * @param invIndex contains the elements to convert to JSON
	 * @param writer   the writer to use
	 * @param level    the initial indentation level
	 * @throws IOException if the writer encounters any issues
	 */
	public static void asTripleNestedObject(TreeMap<String, TreeMap<String, TreeSet<Integer>>> invIndex,
			Writer writer, int level) throws IOException {
		writer.write('{');
		writer.write(System.lineSeparator());
		if(invIndex.size() > 0){
			for(String elementing : invIndex.headMap(invIndex.lastKey()).keySet()) {
				indent(level + 1, writer);
				quote(elementing, writer);
				writer.write(": ");
				asNestedObject(invIndex.get(elementing), writer, level+1);
				writer.write(',');
				writer.write(System.lineSeparator());
			}
			indent(level + 1, writer);
			quote(invIndex.lastKey().toString(), writer);
			writer.write(": ");
			asNestedObject(invIndex.get(invIndex.lastKey()), writer, level+1);
			writer.write(System.lineSeparator());
		}
		writer.write('}');
	}
	
	/**
	 * Returns the nested map of elements formatted as a nested pretty JSON object.
	 */
	public static String asQueryTripleNestedObject(TreeMap<String, ArrayList<SearchResult>> falds) {
		// THIS METHOD IS PROVIDED FOR YOU. DO NOT MODIFY.
		try {
			StringWriter writer = new StringWriter();
			asQueryTripleNestedObject(falds, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the nested map of elements formatted as a nested pretty JSON object
	 * to the specified file.
	 *
	 * @param elements the elements to convert to JSON
	 * @param path     the path to the file write to output
	 * @throws IOException if the writer encounters any issues
	 *
	 * @see #asNestedObject(TreeMap, Writer, int)
	 */
	public static void asQueryTripleNestedObject(TreeMap<String, ArrayList<SearchResult>> falds,
			Path path) throws IOException {
		// THIS METHOD IS PROVIDED FOR YOU. DO NOT MODIFY.
		try (BufferedWriter writer = Files.newBufferedWriter(path,
				StandardCharsets.UTF_8)) {
			asQueryTripleNestedObject(falds, writer, 0);
		}
	}
	/**
	 * Writes the nested map of elements as a nested pretty JSON object using the
	 * provided {@link Writer} and indentation level.
	 *
	 * @param invIndex contains the elements to convert to JSON
	 * @param writer   the writer to use
	 * @param level    the initial indentation level
	 * @throws IOException if the writer encounters any issues
	 */
	public static void asQueryTripleNestedObject(TreeMap<String, ArrayList<SearchResult>> queryList,
			Writer writer, int level) throws IOException {
		writer.write('[');
		if(queryList.size() > 0){
			int index = 0;
			for(String elementing : queryList.keySet()) {
				int index2 = 0;
				writer.write(System.lineSeparator());
				indent(level + 1, writer);
				writer.write('{');
				writer.write(System.lineSeparator());
				indent(level + 2, writer);
				writer.write("\"queries\": " + "\"" + elementing.toString().replace(",", "").replace("[", "").replace("]", "") + "\","); 
				writer.write(System.lineSeparator());
				indent(level + 2, writer);
				writer.write("\"results\": [");
				ArrayList<SearchResult> jacks = queryList.get(elementing);
				for(SearchResult jack: jacks) {
					DecimalFormat FORMATTER = new DecimalFormat("0.000000");
					writer.write(System.lineSeparator());
					indent(level + 3, writer);
					writer.write("{");
					writer.write(System.lineSeparator());
					indent(level + 4, writer);
					writer.write("\"where\": " + "\"" + jack.getLocation() + "\",");
					writer.write(System.lineSeparator());
					indent(level + 4, writer);
					writer.write("\"count\": " + jack.getCount() + ",");
					writer.write(System.lineSeparator());
					indent(level + 4, writer);
					writer.write("\"score\": " + FORMATTER.format(jack.getScore()));
					writer.write(System.lineSeparator());
					indent(level + 3, writer);
					if(index2 < jacks.size() - 1) {
						writer.write("},");
					} else {
						writer.write('}');
					}
					index2++;
				}
				writer.write(System.lineSeparator());
				indent(level + 2, writer);
				writer.write(']');
				writer.write(System.lineSeparator());
				indent(level + 1, writer);
				if(index < queryList.size() - 1) {
					writer.write("},");
				}
				else {
					writer.write('}');
				}		
				index++;
			}
		}
		writer.write(System.lineSeparator());
		indent(level, writer);
		writer.write(']');
	}
			
	public static void main(String[] args) {

		TreeSet<Integer> test = new TreeSet<>();
		test.add(3);
		test.add(11);
		test.add(-2);

		System.out.println(asArray(test));
	}
}
