package gaia.cu9.ari.gaiaorbit.gui.swing.console;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class TextScreen extends JTextPane implements ScreenAPI {
    private static final long serialVersionUID = 1053080839396595112L;

    private static final int DEFAULT_MAX_CHARS = 80;
    private static final int DEFAULT_MAX_LINES = 25;

    private static final int DEFAULT_FONT_SIZE = 20;

    private static final Color DEFAULT_FOREGROUND_COLOR = Color.GREEN;
    private static final Color DEFAULT_BACKGROUND_COLOR = Color.BLACK;

    private int maxChars = DEFAULT_MAX_CHARS;
    private int maxLines = DEFAULT_MAX_LINES;

    private Color foregroundColor = DEFAULT_FOREGROUND_COLOR;
    private Color backgroundColor = DEFAULT_BACKGROUND_COLOR;

    private char[][] characters = null;

    private Color[][] backgroundColors = null;
    private Color[][] foregroundColors = null;

    public TextScreen() {
        this(DEFAULT_MAX_LINES,
                DEFAULT_MAX_CHARS,
                DEFAULT_FOREGROUND_COLOR,
                DEFAULT_BACKGROUND_COLOR,
                DEFAULT_FONT_SIZE);
    }

    public TextScreen(int maxLines,
            int maxChars,
            Color foregroundColor,
            Color backgroundColor,
            int fontSize) {
        this.maxLines = maxLines;
        this.maxChars = maxChars;

        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;

        this.characters = new char[maxLines][maxChars];

        this.foregroundColors = new Color[maxLines][maxChars];
        this.backgroundColors = new Color[maxLines][maxChars];

        Font font = new Font(Font.MONOSPACED, Font.BOLD, fontSize);
        setFont(font);

        FontRenderContext fontRenderContext = new FontRenderContext(null, true, true);

        Rectangle2D stringBounds = font.getStringBounds(new char[] { 'M' }, 0, 1, fontRenderContext);
        setPreferredSize(new Dimension(
                (int) ((getMaxChars() + 0.5) * stringBounds.getWidth()),
                (int) ((getMaxLines() + 1.1) * stringBounds.getHeight())));

        setForeground(getForegroundColor());
        setBackground(getBackgroundColor());
        setCaretColor(getForegroundColor());

        setEditable(false);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        super.paint(g);
    }

    @Override
    public ScreenAPI getScreenAPI() {
        return this;
    }

    protected AttributeSet prepareCharacter(int line, int pos) {
        SimpleAttributeSet saset = new SimpleAttributeSet();

        saset.addAttribute(StyleConstants.Foreground, foregroundColors[line][pos]);
        saset.addAttribute(StyleConstants.Background, backgroundColors[line][pos]);

        return saset;
    }

    protected boolean onKeyPressed(KeyEvent e) {
        boolean consumed = true;

        return consumed;
    }

    @Override
    protected void processKeyEvent(KeyEvent e) {
        if (e.getID() != KeyEvent.KEY_PRESSED) {
            return;
        }

        boolean consumed = onKeyPressed(e);

        if (!consumed) {
            super.processKeyEvent(e);
        }
    }

    public void refresh() {
        setEditable(true);

        setText("");

        StyleContext sc = StyleContext.getDefaultStyleContext();

        for (int line = 0; line < maxLines; line++) {
            for (int pos = 0; pos < maxChars; pos++) {
                AttributeSet aset = prepareCharacter(line, pos);
                aset = sc.addAttributes(aset, aset);

                setCharacterAttributes(aset, false);

                StringBuilder sb = new StringBuilder();
                sb.append(characters[line][pos]);

                if (pos == (maxChars - 1)) {
                    sb.append(System.getProperty("line.separator"));
                }

                replaceSelection(sb.toString());
            }
        }

        setEditable(false);
    }

    @Override
    public void clear() {
        for (int line = 0; line < maxLines; line++) {
            for (int pos = 0; pos < maxChars; pos++) {
                characters[line][pos] = ' ';

                foregroundColors[line][pos] = foregroundColor;
                backgroundColors[line][pos] = backgroundColor;
            }
        }
    }

    @Override
    public Color getForegroundColor() {
        return foregroundColor;
    }

    @Override
    public void setForegroundColor(Color foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    @Override
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    @Override
    public int getMaxLines() {
        return maxLines;
    }

    @Override
    public int getMaxChars() {
        return maxChars;
    }

    @Override
    public char[][] getCharacters() {
        return characters;
    }

    @Override
    public Color[][] getBackgroundColors() {
        return backgroundColors;
    }

    @Override
    public Color[][] getForegroundColors() {
        return foregroundColors;
    }
}
