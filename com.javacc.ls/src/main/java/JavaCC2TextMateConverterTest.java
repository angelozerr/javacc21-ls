import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.stream.Stream;

import com.javacc.parser.ParseException;

public class JavaCC2TextMateConverterTest {

	public static void main(String[] args) throws IOException, ParseException {
		JavaCC2TextMateConverter converter = new JavaCC2TextMateConverter();
		StringWriter writer = new StringWriter();
		Path path = Paths.get("src/main/java/JavaCC.javacc");
		String grammarName = "JSON";
		String[] fileTypes = {"json"};
		converter.convert(path.toFile().toString(), getJavaCCContent(path), grammarName, fileTypes, writer);
		System.err.println(writer.toString());
	}

	private static String getJavaCCContent(Path path) {
		return readLineByLineJava8(path);
	}

	public static String convertStreamToString(InputStream is) {
		try (Scanner s = new java.util.Scanner(is)) {
			s.useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
		}
	}

	private static String readLineByLineJava8(Path path) {
		StringBuilder contentBuilder = new StringBuilder();
		try (Stream<String> stream = Files.lines(path, StandardCharsets.UTF_8)) {
			stream.forEach(s -> contentBuilder.append(s).append("\n"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return contentBuilder.toString();
	}
}
