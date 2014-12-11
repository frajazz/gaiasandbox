package gaia.cu9.ari.gaiaorbit.gui.swing.console;

import java.awt.Color;

public interface ScreenAPI {
    public void setForegroundColor(Color foregroundColor);

    public void setBackgroundColor(Color backgroundColor);

    public Color getForegroundColor();

    public Color getBackgroundColor();

    public int getMaxLines();

    public int getMaxChars();

    public char[][] getCharacters();

    public Color[][] getBackgroundColors();

    public Color[][] getForegroundColors();

    public void clear();

    public void refresh();

    public ScreenAPI getScreenAPI();
}
