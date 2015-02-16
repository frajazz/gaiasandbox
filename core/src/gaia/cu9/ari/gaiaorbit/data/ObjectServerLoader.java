package gaia.cu9.ari.gaiaorbit.data;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.scenegraph.Star;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.object.server.ClientCore;
import gaia.cu9.object.server.commands.Message;
import gaia.cu9.object.server.commands.MessageHandler;
import gaia.cu9.object.server.commands.MessagePayloadBlock;
import gaia.cu9.object.server.commands.plugins.ClientIdent;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ObjectServerLoader implements ISceneGraphNodeProvider {
    ClientCore cc;
    List<CelestialBody> result;

    @Override
    public void initialize(Properties properties) {
	result = new ArrayList<CelestialBody>();
	cc = ClientCore.getInstance();
    }

    String rawdata = null;

    @Override
    public List<? extends SceneGraphNode> loadObjects() {
	long starid = 1;
	long errors = 0;
	try {
	    EventManager.getInstance().post(Events.POST_NOTIFICATION, this.getClass().getSimpleName(), I18n.bundle.format("notif.objectserver.gettingdata"));

	    String visid = GlobalConf.data.VISUALIZATION_ID;

	    if (!cc.isConnected()) {
		cc.connect(GlobalConf.data.OBJECT_SERVER_HOSTNAME,
			GlobalConf.data.OBJECT_SERVER_PORT);
		ClientIdent ident = new ClientIdent();
		ident.setAffiliation("ARI");
		ident.setAuthors("Toni Sagristà <tsagrista@ari.uni-heidelberg.de>");
		ident.setClientDescription("Real time, 3D, outreach visualization software");
		ident.setClientDocumentationURL(GlobalConf.WIKI);
		ident.setClientHomepage(GlobalConf.WEBPAGE);
		ident.setClientName(GlobalConf.APPLICATION_NAME);
		ident.setClientVersion(GlobalConf.version.version);
		ident.setClientPlatform(System.getProperty("os.name"));
		ident.setClientIconURL(GlobalConf.ICON_URL);
		cc.executeCommand(ident);
	    }

	    // Get star data
	    Message msg = new Message("visualization-particle-data?vis-id=" + visid
		    + "&include-headers=false");
	    msg.setMessageHandler(new MessageHandler() {
		int blocks = 0;

		@Override
		public void receivedMessage(Message query, Message reply) {
		    StringBuilder data = new StringBuilder();
		    for (MessagePayloadBlock block : reply.getPayload()) {
			data.append((String) block.getPayload());
			System.out.println("Received block " + (++blocks));
		    }
		    rawdata = data.toString();
		}

		@Override
		public void receivedMessageBlock(Message query, Message reply, MessagePayloadBlock block) {

		}

	    });
	    cc.sendMessage(msg);

	    // TODO Get this shit together, this does not look good...
	    do {
		try {
		    Thread.sleep(500);
		} catch (InterruptedException e) {
		    EventManager.getInstance().post(Events.JAVA_EXCEPTION, e);
		}
	    } while (rawdata == null);

	    // Get LoD data
	    //	    msg = new Message("visualization-metadata?vis-id=" + visid
	    //		    + "&lod-level=-1");
	    //	    cc.sendMessage(msg);
	    //	    Collection<MessagePayloadBlock> lod = msg.getPayload();

	    // Parse into list of stars
	    String[] lines = rawdata.split("\n");
	    for (String line : lines) {
		String[] tokens = line.split(";");
		try {
		    double ra = Double.parseDouble(tokens[0]);
		    double dec = Double.parseDouble(tokens[1]);
		    double dist = Double.parseDouble(tokens[2]);

		    float mag = tokens[3].isEmpty() ? 12f : Float.parseFloat(tokens[3]);
		    float bv = Float.parseFloat(tokens[4]);

		    if (mag <= GlobalConf.data.LIMIT_MAG_LOAD) {
			String name = "dummy" + starid;
			Star s = new Star(Coordinates.sphericalToCartesian(ra, dec, dist * Constants.PC_TO_U, new Vector3d()), mag, mag, bv, name, starid++);
			s.initialize();
			result.add(s);
		    }

		} catch (Exception e) {
		    //EventManager.getInstance().post(Events.JAVA_EXCEPTION, new RuntimeException("Error in star " + starid + ": Skipping it"));
		    errors++;
		}
	    }

	    // Disconnect
	    cc.sendMessage("client-disconnect");
	} catch (ConnectException e) {
	    EventManager.getInstance().post(Events.POST_NOTIFICATION, I18n.bundle.get("notif.objectserver.notconnect"));
	    EventManager.getInstance().post(Events.JAVA_EXCEPTION, e);
	} catch (IOException e) {
	    EventManager.getInstance().post(Events.JAVA_EXCEPTION, e);
	}

	// Manually add sun
	Star s = new Star(new Vector3d(0, 0, 0), 4.83f, 4.83f, 0.656f, "Sol", starid++);
	s.initialize();
	result.add(s);

	if (errors > 0)
	    EventManager.getInstance().post(Events.JAVA_EXCEPTION, new RuntimeException(I18n.bundle.format("error.loading.objects", errors)));
	EventManager.getInstance().post(Events.POST_NOTIFICATION, this.getClass().getSimpleName(), I18n.bundle.format("notif.catalog.init", result.size()));

	return result;
    }
}
