package gaia.cu9.ari.gaiaorbit.interfce;

public class TextUtils {
    public static CharSequence limitWidth(CharSequence text, float width, float letterWidth) {
	int lettersPerLine = (int) (width / letterWidth);
	StringBuilder out = new StringBuilder();
	int currentLine = 0;
	for (int i = 0; i < text.length(); i++) {
	    char c = text.charAt(i);
	    if (c == ' ' && Math.abs(currentLine - lettersPerLine) <= 5) {
		c = '\n';
		currentLine = 0;
	    } else if (c == '\n') {
		currentLine = 0;
	    } else {
		currentLine++;
	    }
	    out.append(c);
	}

	return out;
    }
}
