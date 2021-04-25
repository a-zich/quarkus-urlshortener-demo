package eu.zich.demo.quarkusurlshortener.constants;

public class Constants {

	// using this regexp will also check that id is valid
	public static final String ID_WITH6LETTERS_PATH = "/{id:\\w{6}}";
	public static final String REGEXP_6LETTERS = "\\w{6}";

	private Constants() {
		// making SonarCube happy
	}
}
