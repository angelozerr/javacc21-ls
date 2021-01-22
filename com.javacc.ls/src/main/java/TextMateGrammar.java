import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TextMateGrammar {

	private final String name;
	
	private final List<Pattern> patterns;
	
	private final Map<String, Pattern> repository;
	
	public TextMateGrammar(String name) {
		this.name = name;
		this.patterns = new ArrayList<>();
		this.repository = new LinkedHashMap<>();
	}
	
	public String getName() {
		return name;
	}
	
	public List<Pattern> getPatterns() {
		return patterns;
	}
	
	public Map<String, Pattern> getRepository() {
		return repository;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}
}
