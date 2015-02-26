package gaia.cu9.ari.gaiaorbit.data;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.scenegraph.Star;
import gaia.cu9.ari.gaiaorbit.scenegraph.octreewrapper.AbstractOctreeWrapper;
import gaia.cu9.ari.gaiaorbit.scenegraph.octreewrapper.OctreeWrapper;
import gaia.cu9.ari.gaiaorbit.scenegraph.octreewrapper.OctreeWrapperConcurrent;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Pair;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode.OctantStatus;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class ObjectServerLoader implements ISceneGraphNodeProvider {
    /** The loading queue **/
    private static Queue<OctreeNode<?>> loadQueue = new ArrayBlockingQueue<OctreeNode<?>>(5000);

    /** Daemon thread that gets the data loading requests and serves them **/
    private static DaemonLoader daemon;

    /** Adds an octant to the queue to be loaded **/
    public static void addToQueue(OctreeNode<?> octant) {
	synchronized (loadQueue) {
	    loadQueue.add(octant);
	    octant.setStatus(OctantStatus.QUEUED);
	    // More than one second, flush
	    flushLoadQueue();
	}
    }

    /**
     * Tells the loader to start loading the octants in the queue.
     */
    public static void flushLoadQueue() {
	if (daemon != null && !daemon.awake && !loadQueue.isEmpty()) {
	    daemon.interrupt();
	}
    }

    private static int preloadDepth = 1;

    Longref starid = new Longref(1l);
    Longref errors = new Longref();

    ClientCore cc;
    List<SceneGraphNode> result;

    Map<Long, Pair<OctreeNode<SceneGraphNode>, long[]>> nodesMap;
    OctreeNode<SceneGraphNode> root;

    @Override
    public void initialize(Properties properties) {
	result = new ArrayList<SceneGraphNode>();
	nodesMap = new HashMap<Long, Pair<OctreeNode<SceneGraphNode>, long[]>>();
	cc = ClientCore.getInstance();
    }

    @Override
    public List<? extends SceneGraphNode> loadObjects() {
	errors.num = 0l;
	String visid = null;
	AbstractOctreeWrapper otw = null;
	final List<CelestialBody> particleList = new ArrayList<CelestialBody>(10000);
	try {
	    EventManager.getInstance().post(Events.POST_NOTIFICATION, this.getClass().getSimpleName(), I18n.bundle.format("notif.objectserver.gettingdata"));

	    visid = GlobalConf.data.VISUALIZATION_ID;

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

				OctreeNode<SceneGraphNode> node = new OctreeNode<SceneGraphNode>(pageId, y, z, x, hsy, hsz, hsx, childrenCount, nObjects, ownObjects, depth);
				nodesMap.put(pageId, new Pair<OctreeNode<SceneGraphNode>, long[]>(node, childrenIds));

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
		}

	    });
	    cc.sendMessage(msgMetadata, true);

	    int depthLevel = Math.min(OctreeNode.maxDepth, preloadDepth);
	    for (int level = 0; level <= depthLevel; level++) {
		// Fetch particle data for level 0
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
				    Star star = parseLine(line, errors, starid);
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
	    Star s = new Star(new Vector3d(0, 0, 0), 4.83f, 4.83f, 0.656f, "Sol", starid.num++);
	    s.initialize();
	    particleList.add(s);

	    // Find out octant of sun
	    OctreeNode<SceneGraphNode> candidate = root.getBestOctant(s.pos);
	    if (candidate == null) {
		EventManager.getInstance().post(Events.JAVA_EXCEPTION, new RuntimeException("No octant candidate for the Sun found!"));
	    } else {
		s.pageId = candidate.pageId;
	    }

	    // Insert stars in Octree
	    for (CelestialBody cb : particleList) {
		s = (Star) cb;
		OctreeNode<SceneGraphNode> octant = nodesMap.get(s.pageId).getFirst();
		octant.add(s);
	    }
	    // Level 0 is loaded
	    root.setStatus(OctantStatus.LOADED, depthLevel);

	    // Add octree wrapper to result
	    if (GlobalConf.performance.MULTITHREADING) {
		otw = new OctreeWrapperConcurrent("Universe", root, GlobalConf.performance.NUMBER_THREADS);
	    } else {
		otw = new OctreeWrapper("Universe", root);
	    }
	    otw.initialize();
	    result.add(otw);

	} catch (ConnectException e) {
	    EventManager.getInstance().post(Events.POST_NOTIFICATION, I18n.bundle.get("notif.objectserver.notconnect"));
	    EventManager.getInstance().post(Events.JAVA_EXCEPTION, e);
	} catch (IOException e) {
	    EventManager.getInstance().post(Events.JAVA_EXCEPTION, e);
	}

	if (errors.num > 0l)
	    EventManager.getInstance().post(Events.JAVA_EXCEPTION, new RuntimeException(I18n.bundle.format("error.loading.objects", errors)));
	EventManager.getInstance().post(Events.POST_NOTIFICATION, this.getClass().getSimpleName(), I18n.bundle.format("notif.catalog.init", particleList.size()));

	/**
	 * INITIALIZE DAEMON LOADER THREAD
	 */
	daemon = new DaemonLoader(visid, starid, otw);
	daemon.setDaemon(true);
	daemon.setName("daemon-objectserver-loader");
	daemon.start();

	return result;
    }

    private static Star parseLine(String line, Longref errors, Longref starid) {
	String[] tokens = line.split(";");
	try {
	    double x = Double.parseDouble(tokens[0]) * Constants.PC_TO_U;
	    double y = Double.parseDouble(tokens[1]) * Constants.PC_TO_U;
	    double z = Double.parseDouble(tokens[2]) * Constants.PC_TO_U;

	    // Magnitude in virtual particles (type=92) must depend on number of particles contained
	    float mag = (tokens[3].equalsIgnoreCase("null") || tokens[3].isEmpty()) ? 4f : Float.parseFloat(tokens[3]);
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
		Star s = new Star(new Vector3d(y, z, x), mag, mag, bv, name, starid.num++);
		s.pageId = pageid;
		s.particleCount = particleCount;
		s.type = type;
		s.initialize();
		return s;
	    }

	} catch (Exception e) {
	    //EventManager.getInstance().post(Events.JAVA_EXCEPTION, new RuntimeException("Error in star " + starid + ": Skipping it"));
	    errors.num++;
	}
	return null;
    }

    /**
     * The daemon loader thread.
     * @author Toni Sagrista
     *
     */
    private static class DaemonLoader extends Thread {
	public boolean awake = false;

	/** The ID of the visualization **/
	private String visid;
	private Longref errors;
	private Longref starid;
	private ClientCore cc;
	private AbstractOctreeWrapper aow;
	private String globalPauseName;

	public DaemonLoader(String visid, Longref starid, AbstractOctreeWrapper aow) {
	    this.visid = visid;
	    this.starid = starid;
	    this.errors = new Longref(0l);
	    this.aow = aow;
	    this.cc = ClientCore.getInstance();
	    this.globalPauseName = I18n.bundle.get("notif.globalpause");
	}

	@Override
	public void run() {
	    while (true) {
		synchronized (loadQueue) {
		    EventManager.getInstance().post(Events.CAMERA_STOP);
		    // Disable input
		    EventManager.getInstance().post(Events.INPUT_ENABLED_CMD, false);
		    // Stop program
		    EventManager.getInstance().post(Events.TOGGLE_UPDATEPAUSE, globalPauseName);
		    // Load data in queue
		    while (!loadQueue.isEmpty()) {
			OctreeNode<SceneGraphNode> octant = (OctreeNode<SceneGraphNode>) loadQueue.poll();
			EventManager.getInstance().post(Events.POST_NOTIFICATION, I18n.bundle.format("notif.loadingoctant", octant.pageId));
			try {
			    final List<SceneGraphNode> particleList = new ArrayList<SceneGraphNode>(50);

			    octant.setStatus(OctantStatus.LOADING);
			    Message msgParticle = new Message("visualization-page?vis-id=" + visid
				    + "&page-id=" + octant.pageId);
			    msgParticle.setMessageHandler(new MessageHandler() {

				@Override
				public void receivedMessage(Message query, Message reply) {
				    for (MessagePayloadBlock block : reply.getPayload()) {
					String data = (String) block.getPayload();
					BufferedReader reader = new BufferedReader(new StringReader(data));
					try {
					    String line = null;
					    while ((line = reader.readLine()) != null) {
						Star star = parseLine(line, errors, starid);
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

			    // Set objects to octant
			    if (octant.objects != null) {
				particleList.addAll(octant.objects);
			    }
			    octant.setObjects(particleList);
			    // Add objects to octree wrapper node
			    aow.add(octant.objects, octant);
			    octant.setStatus(OctantStatus.LOADED);

			} catch (IOException e) {
			    EventManager.getInstance().post(Events.JAVA_EXCEPTION, e);
			    EventManager.getInstance().post(Events.POST_NOTIFICATION, I18n.bundle.format("notif.loadingoctant.fail", octant.pageId));
			    octant.setStatus(OctantStatus.LOADING_FAILED);
			}
		    }

		    // Resume program
		    EventManager.getInstance().post(Events.TOGGLE_UPDATEPAUSE, globalPauseName);
		    // Enable input
		    EventManager.getInstance().post(Events.INPUT_ENABLED_CMD, true);

		}

		// Sleep until new data comes
		try {
		    awake = false;
		    Thread.sleep(Long.MAX_VALUE - 8);
		} catch (InterruptedException e) {
		    // New data!
		    awake = true;
		}
	    }
	}
    }

    private static class Longref {
	public long num;

	public Longref() {
	}

	public Longref(long num) {
	    this.num = num;
	}

	@Override
	public String toString() {
	    return String.valueOf(num);
	}
    }
}
