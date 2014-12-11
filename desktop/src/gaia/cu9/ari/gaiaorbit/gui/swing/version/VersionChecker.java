package gaia.cu9.ari.gaiaorbit.gui.swing.version;

import gaia.cu9.ari.gaiaorbit.gui.swing.callback.Runnable;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class VersionChecker implements Runnable {
    private String stringUrl;

    public VersionChecker(String stringUrl) {
	this.stringUrl = stringUrl;
    }

    @Override
    public Object run() {
	String result = null;
	try {
	    URL url = new URL(stringUrl);
	    InputStream is = url.openStream();
	    /* Now read the retrieved document from the stream. */
	    result = convertStreamToString(is);
	    is.close();

	} catch (MalformedURLException e) {
	    result = e.getLocalizedMessage();
	} catch (IOException e) {
	    result = e.getLocalizedMessage();
	}
	return result;
    }

    static String convertStreamToString(java.io.InputStream is) {
	java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	return s.hasNext() ? s.next() : "";
    }
}
