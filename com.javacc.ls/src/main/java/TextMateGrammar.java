import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TextMateGrammar {

	private final String name;

	private final String[] fileTypes;

	private final String scopeName;

	private final List<Pattern> patterns;

	private final Map<String, Pattern> repository;

	public TextMateGrammar(String name, String[] fileTypes) {
		this.name = name;
		this.fileTypes = fileTypes;
		this.scopeName = "source." + getExtension();
		this.patterns = new ArrayList<>();
		this.repository = new LinkedHashMap<>();
	}

	public String getName() {
		return name;
	}

	public String[] getFileTypes() {
		return fileTypes;
	}

	public String getScopeName() {
		return scopeName;
	}

	public String getExtension() {
		return fileTypes[0];
	}

	public List<Pattern> getPatterns() {
		return patterns;
	}

	public Map<String, Pattern> getRepository() {
		return repository;
	}
}
