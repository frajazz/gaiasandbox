package gaia.cu9.ari.gaiaorbit.desktop.gui.swing.console;

import java.awt.Color;

public interface ConsoleAPI {
    public void setCharAt(int line, int pos, char chr, Color foregroundColor, Color backgroundColor);

    public void setCharAt(int line, int pos, char chr, Color foregroundColor);

    public void setCharAt(int line, int pos, char chr);

    public char getCharAt(int line, int pos);

    public void setStringAt(int line, int pos, String str, Color foregroundColor, Color backgroundColor);

    public void setStringAt(int line, int pos, String str, Color foregroundColor);

    public void setStringAt(int line, int pos, String str);

    public void setForegroundAt(int line, int pos, Color color);

    public void setBackgroundAt(int line, int pos, Color color);

    public Color getForegroundAt(int line, int pos);

    public Color getBackgroundAt(int line, int pos);

    public void clear();

    public void refresh();

    public ConsoleAPI getConsoleAPI();
}
