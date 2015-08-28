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

    /**
     * Converts from property displayName to method displayName by removing the
     * separator dots and capitalising each chunk.
     * Example: model.texture.bump -> ModelTextureBump
     * @param property The property displayName.
     * @return
     */
    public static String propertyToMethodName(String property) {
        String[] parts = property.split("\\.");
        StringBuilder b = new StringBuilder();
        for (String part : parts) {
            b.append(capitalise(part));
        }
        return b.toString();
    }

    /**
     * Returns the given string with the first letter capitalised
     * @param line
     * @return
     */
    public static String capitalise(String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    /**
     * Returns the given string with the first letter capitalised and all the others in lower case.
     * @param line
     * @return
     */
    public static String trueCapitalise(String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1).toLowerCase();
    }
}
