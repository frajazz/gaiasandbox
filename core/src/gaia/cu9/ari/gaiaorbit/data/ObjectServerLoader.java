package gaia.cu9.ari.gaiaorbit.data;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.scenegraph.AbstractPositionEntity;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.OctreeWrapper;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.scenegraph.Star;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Pair;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;
import gaia.cu9.object.server.ClientCore;
import gaia.cu9.object.server.commands.Message;
import gaia.cu9.object.server.commands.MessageHandler;
import gaia.cu9.object.server.commands.MessagePayloadBlock;
import gaia.cu9.object.server.commands.plugins.ClientIdent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ObjectServerLoader implements ISceneGraphNodeProvider {
    ClientCore cc;
    List<SceneGraphNode> result;
    List<CelestialBody> particleList;
    Map<Long, Pair<OctreeNode<AbstractPositionEntity>, long[]>> nodesMap;
    OctreeNode<AbstractPositionEntity> root;
    Long starid = 1l;
    Long errors = 0l;

    @Override
    public void initialize(Properties properties) {
	result = Collections.synchronizedList(new ArrayList<SceneGraphNode>());
	particleList = Collections.synchronizedList(new ArrayList<CelestialBody>());
	nodesMap = Collections.synchronizedMap(new HashMap<Long, Pair<OctreeNode<AbstractPositionEntity>, long[]>>());
	cc = ClientCore.getInstance();
    }

    @Override
    public List<? extends SceneGraphNode> loadObjects() {
	errors = 0l;

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
		cc.executeCommand(ident, true);
	    }

	    // Get Octree data
	    Message msgMetadata = new Message("visualization-metadata?vis-id=" + visid
		    + "&lod-level=-1");
	    msgMetadata.setMessageHandler(new MessageHandler() {

		@Override
		public void receivedMessage(Message query, Message reply) {
		    int maxdepth = 0;
		    for (MessagePayloadBlock block : reply.getPayload()) {
			String data = (String) block.getPayload();
			BufferedReader reader = new BufferedReader(new StringReader(data));
			try {
			    String line = null;
			    while ((line = reader.readLine()) != null) {
				String[] tokens = line.split(";");
				long pageId = Long.parseLong(tokens[0]);

				String[] xyz = tokens[1].split(",");
				double x = Double.parseDouble(xyz[0]) * Constants.PC_TO_U;
				double y = Double.parseDouble(xyz[1]) * Constants.PC_TO_U;
				double z = Double.parseDouble(xyz[2]) * Constants.PC_TO_U;

				String[] hsxyz = tokens[2].split(",");
				double hsx = Double.parseDouble(hsxyz[0]) * Constants.PC_TO_U;
				double hsy = Double.parseDouble(hsxyz[1]) * Constants.PC_TO_U;
				double hsz = Double.parseDouble(hsxyz[2]) * Constants.PC_TO_U;

				int nObjects = Integer.parseInt(tokens[3]);
				int ownObjects = Integer.parseInt(tokens[4]);
				int childrenCount = Integer.parseInt(tokens[5]);

				long[] childrenIds = new long[8];
				String[] childrenIdsStr = tokens[6].split(",");
				for (int i = 0; i < 8; i++) {
				    childrenIds[i] = Long.parseLong(childrenIdsStr[i]);
				}
				int depth = Integer.parseInt(tokens[7]);
				maxdepth = Math.max(maxdepth, depth);

				OctreeNode<AbstractPositionEntity> node = new OctreeNode<AbstractPositionEntity>(pageId, y, z, x, hsy, hsz, hsx, childrenCount, nObjects, ownObjects, depth);
				nodesMap.put(pageId, new Pair<OctreeNode<AbstractPositionEntity>, long[]>(node, childrenIds));

				if (depth == 0) {
				    root = node;
				}

			    }
			} catch (IOException e) {
			    EventManager.getInstance().post(Events.JAVA_EXCEPTION, e);
			}
		    }
		    // Set max depth. Multiply by two to stay in the first half of hue in HSL color space.
		    OctreeNode.maxDepth = maxdepth;
		    // All data has arrived
		    if (root != null) {
			root.resolveChildren(nodesMap);
		    } else {
			EventManager.getInstance().post(Events.JAVA_EXCEPTION, new RuntimeException("No root node in visualization-metadata"));
		    }

		}

		@Override
		public void receivedMessageBlock(Message query, Message reply, MessagePayloadBlock block) {
		    // TODO Auto-generated method stub

		}

	    });
	    cc.sendMessage(msgMetadata, true);

	    // Fetch particle data
	    for (int level = 0; level <= OctreeNode.maxDepth; level++) {
		Message msgParticle = new Message("visualization-lod-data?vis-id=" + visid
			+ "&lod-level=" + level);
		msgParticle.setMessageHandler(new MessageHandler() {

		    @Override
		    public void receivedMessage(Message query, Message reply) {
			for (MessagePayloadBlock block : reply.getPayload()) {
			    String data = (String) block.getPayload();
			    BufferedReader reader = new BufferedReader(new StringReader(data));
			    try {
				String line = null;
				while ((line = reader.readLine()) != null) {
				    Star star = parseLine(line);
				    if (star != null)
					particleList.add(star);
				}
			    } catch (IOException e) {
				EventManager.getInstance().post(Events.JAVA_EXCEPTION, e);
			    }
			}
		    }

		    @Override
		    public void receivedMessageBlock(Message query, Message reply, MessagePayloadBlock block) {
		    }

		});
		cc.sendMessage(msgParticle, true);
	    }
	    // Manually add sun
	    Star s = new Star(new Vector3d(0, 0, 0), 4.83f, 4.83f, 0.656f, "Sol", starid++);
	    s.initialize();
	    particleList.add(s);

	    // Insert stars in Octree
	    for (CelestialBody cb : particleList) {
		s = (Star) cb;
		nodesMap.get(s.pageid).getFirst().add(s);
	    }

	    // Add octree wrapper to result
	    OctreeWrapper otw = new OctreeWrapper("Universe", root);
	    otw.initialize();
	    result.add(otw);

	} catch (ConnectException e) {
	    EventManager.getInstance().post(Events.POST_NOTIFICATION, I18n.bundle.get("notif.objectserver.notconnect"));
	    EventManager.getInstance().post(Events.JAVA_EXCEPTION, e);
	} catch (IOException e) {
	    EventManager.getInstance().post(Events.JAVA_EXCEPTION, e);
	}

	if (errors > 0)
	    EventManager.getInstance().post(Events.JAVA_EXCEPTION, new RuntimeException(I18n.bundle.format("error.loading.objects", errors)));
	EventManager.getInstance().post(Events.POST_NOTIFICATION, this.getClass().getSimpleName(), I18n.bundle.format("notif.catalog.init", particleList.size()));

	return result;
    }

    private Star parseLine(String line) {
	String[] tokens = line.split(";");
	try {
	    double x = Double.parseDouble(tokens[0]) * Constants.PC_TO_U;
	    double y = Double.parseDouble(tokens[1]) * Constants.PC_TO_U;
	    double z = Double.parseDouble(tokens[2]) * Constants.PC_TO_U;

	    // Magnitude in virtual particles (type=92) must depend on number of particles contained
	    float mag = (tokens[3].equalsIgnoreCase("null") || tokens[3].isEmpty()) ? 12f : Float.parseFloat(tokens[3]);
	    // Color in virtual particles should be that of the sun - yellowish
	    float bv = (tokens[4].equalsIgnoreCase("null") || tokens[4].isEmpty()) ? 0.656f : Float.parseFloat(tokens[4]);

	    int particleCount = Integer.parseInt(tokens[7]);
	    long pageid = Long.parseLong(tokens[8]);
	    int type = Integer.parseInt(tokens[9]);

	    if (type == 92) {
		// Virtual particle!

	    }

	    if (mag <= GlobalConf.data.LIMIT_MAG_LOAD) {
		String name = type == 92 ? ("virtual" + starid) : ("dummy" + starid);
		Star s = new Star(new Vector3d(y, z, x), mag, mag, bv, name, starid++);
		s.pageid = pageid;
		s.particleCount = particleCount;
		s.type = type;
		s.initialize();
		return s;
	    }

	} catch (Exception e) {
	    //EventManager.getInstance().post(Events.JAVA_EXCEPTION, new RuntimeException("Error in star " + starid + ": Skipping it"));
	    errors++;
	}
	return null;
    }
}
