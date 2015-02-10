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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ObjectServerLoader implements ISceneGraphNodeProvider {
    ClientCore cc;
    List<CelestialBody> result;

    @Override
    public void initialize(Properties properties) {
	result = new ArrayList<CelestialBody>();
	cc = new ClientCore();
    }

    String rawdata = null;

    @Override
    public List<? extends SceneGraphNode> loadObjects() {
	try {
	    EventManager.getInstance().post(Events.POST_NOTIFICATION, this.getClass().getSimpleName(), I18n.bundle.format("notif.limitmag", GlobalConf.instance.LIMIT_MAG_LOAD));

	    String visid = GlobalConf.instance.VISUALIZATION_ID;

	    cc.connect(GlobalConf.instance.OBJECT_SERVER_HOSTNAME,
		    GlobalConf.instance.OBJECT_SERVER_PORT);

	    // Identify yourself!
	    Message msg = new Message("client-ident?affiliation=ARI&name="
		    + GlobalConf.APPLICATION_NAME
		    + "&description=Gaia Sandbox outreach software&version="
		    + GlobalConf.instance.VERSION.version
		    + "&authors=tsagrista&homepage=" + GlobalConf.WEBPAGE
		    + "&icon-url=" + GlobalConf.ICON_URL);
	    cc.sendMessage(msg);

	    // Get star data
	    msg = new Message("visualization-particle-data?vis-id=" + visid
		    + "&include-headers=false");
	    msg.setMessageHandler(new MessageHandler() {

		@Override
		public void receivedMessage(Message query, Message reply) {
		    StringBuilder data = new StringBuilder();
		    for (MessagePayloadBlock block : reply.getPayload()) {
			data.append((String) block.getPayload());
		    }
		    rawdata = data.toString();
		}

	    });
	    cc.sendMessage(msg);

	    // TODO Get this shit together
	    do {
		try {
		    Thread.sleep(300);
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
	    long starid = 1;
	    long errors = 0;
	    for (String line : lines) {
		String[] tokens = line.split(";");
		try {
		    double ra = Double.parseDouble(tokens[0]);
		    double dec = Double.parseDouble(tokens[1]);
		    double dist = Double.parseDouble(tokens[2]);

		    float mag = tokens[3].isEmpty() ? 12f : Float.parseFloat(tokens[3]);
		    float bv = Float.parseFloat(tokens[4]);

		    if (mag <= GlobalConf.instance.LIMIT_MAG_LOAD) {
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
	    // Manually add sun
	    Star s = new Star(new Vector3d(0, 0, 0), 4.83f, 4.83f, 0.656f, "Sol", starid++);
	    s.initialize();
	    result.add(s);

	    // Disconnect
	    cc.sendMessage("client-disconnect");

	    if (errors > 0)
		EventManager.getInstance().post(Events.JAVA_EXCEPTION, new RuntimeException(I18n.bundle.format("error.loading.objects", errors)));
	    EventManager.getInstance().post(Events.POST_NOTIFICATION, this.getClass().getSimpleName(), I18n.bundle.format("notif.catalog.init", result.size()));

	} catch (IOException e) {
	    EventManager.getInstance().post(Events.JAVA_EXCEPTION, e);
	}

	return result;
    }
}
