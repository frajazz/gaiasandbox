package gaia.cu9.ari.gaiaorbit.gui.swing.console;

import java.awt.Color;
import java.awt.event.KeyEvent;

import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class TextConsole extends TextScreen implements ConsoleAPI {
    private static final long serialVersionUID = -5400113690343476972L;

    private int currentLine = 0;
    private int currentPos = 0;

    private ScreenAPI screenAPI = null;

    public TextConsole() {
	super();
	currentLine = 0;
	currentPos = 0;
	screenAPI = getScreenAPI();
    }

    public TextConsole(int maxLines,
	    int maxChars,
	    Color foregroundColor,
	    Color backgroundColor,
	    int fontSize) {
	super(maxLines, maxChars, foregroundColor, backgroundColor, fontSize);
	currentLine = 0;
	currentPos = 0;
	screenAPI = getScreenAPI();
    }

    @Override
    public ConsoleAPI getConsoleAPI() {
	return this;
    }

    @Override
    protected boolean onKeyPressed(KeyEvent e) {
	boolean consumed = false;

	if (e.getKeyCode() == KeyEvent.VK_LEFT) {
	    currentPos--;

	    if (currentPos < 0) {
		currentPos = (screenAPI.getMaxChars() - 1);
	    }

	    consumed = true;
	} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
	    currentPos++;

	    if (currentPos == screenAPI.getMaxChars()) {
		currentPos = 0;
	    }

	    consumed = true;
	} else if (e.getKeyCode() == KeyEvent.VK_UP) {
	    currentLine--;

	    if (currentLine < 0) {
		currentLine = (screenAPI.getMaxLines() - 1);
	    }

	    consumed = true;
	} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
	    currentLine++;

	    if (currentLine == screenAPI.getMaxLines()) {
		currentLine = 0;
	    }

	    consumed = true;
	} else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
	    /*
	     * Needs improvement
	     */
	    currentPos--;

	    setCharAt(currentLine, currentPos, ' ');

	    if (currentPos < 0) {
		currentPos = (screenAPI.getMaxChars() - 1);
	    }

	    consumed = true;
	} else if (Character.isLetterOrDigit(e.getKeyChar())
		|| e.getKeyChar() == ' ') {
	    /*
	     * Process normal characters
	     * Needs improvement
	     */

	    setCharAt(currentLine, currentPos, e.getKeyChar());

	    currentPos++;

	    if (currentPos == screenAPI.getMaxChars()) {
		currentPos = 0;
	    }

	    consumed = true;
	}

	screenAPI.refresh();

	return consumed;
    }

    @Override
    public AttributeSet prepareCharacter(int line, int pos) {
	if (line == currentLine && pos == currentPos) {
	    SimpleAttributeSet saset = new SimpleAttributeSet();

	    saset.addAttribute(StyleConstants.Foreground, screenAPI.getBackgroundColors()[line][pos]);
	    saset.addAttribute(StyleConstants.Background, screenAPI.getForegroundColors()[line][pos]);

	    return saset;
	} else {
	    return super.prepareCharacter(line, pos);
	}
    }

    @Override
    public void setCharAt(int line, int pos, char chr, Color foregroundColor, Color backgroundColor) {
	if (line < 0 || line >= screenAPI.getMaxLines()) {
	    throw new IndexOutOfBoundsException("Invalid line: " + line + ".");
	}

	if (pos < 0 || pos >= screenAPI.getMaxChars()) {
	    throw new IndexOutOfBoundsException("Invalid position: " + pos + ".");
	}

	screenAPI.getCharacters()[line][pos] = chr;

	screenAPI.getForegroundColors()[line][pos] = foregroundColor;
	screenAPI.getBackgroundColors()[line][pos] = backgroundColor;
    }

    @Override
    public void setCharAt(int line, int pos, char chr, Color foregroundColor) {
	setCharAt(line, pos, chr, foregroundColor, screenAPI.getBackgroundColor());
    }

    @Override
    public void setCharAt(int line, int pos, char chr) {
	setCharAt(line, pos, chr, screenAPI.getForegroundColor());
    }

    @Override
    public void setStringAt(int line, int pos, String str, Color foregroundColor, Color backgroundColor) {
	for (int i = 0; i < str.length(); i++) {
	    if (pos >= screenAPI.getMaxChars()) {
		pos = 0;
		line++;
	    }

	    setCharAt(line, pos++, str.charAt(i), foregroundColor, backgroundColor);
	}
    }

    @Override
    public void setStringAt(int line, int pos, String str, Color foregroundColor) {
	setStringAt(line, pos, str, foregroundColor, screenAPI.getBackgroundColor());
    }

    @Override
    public void setStringAt(int line, int pos, String str) {
	setStringAt(line, pos, str, screenAPI.getForegroundColor());
    }

    @Override
    public char getCharAt(int line, int pos) {
	return screenAPI.getCharacters()[line][pos];
    }

    @Override
    public Color getBackgroundAt(int line, int pos) {
	return screenAPI.getBackgroundColors()[line][pos];
    }

    @Override
    public Color getForegroundAt(int line, int pos) {
	return screenAPI.getForegroundColors()[line][pos];
    }

    @Override
    public void setBackgroundAt(int line, int pos, Color color) {
	screenAPI.getBackgroundColors()[line][pos] = color;
    }

    @Override
    public void setForegroundAt(int line, int pos, Color color) {
	screenAPI.getForegroundColors()[line][pos] = color;
    }
}
