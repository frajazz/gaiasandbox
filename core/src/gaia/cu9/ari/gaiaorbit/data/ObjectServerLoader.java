package gaia.cu9.ari.gaiaorbit.data;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
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
import gaia.cu9.ari.gaiaorbit.util.tree.LoadStatus;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class ObjectServerLoader implements ISceneGraphNodeProvider {
    /** The loading queue **/
    private static Queue<Object> loadQueue = new ArrayBlockingQueue<Object>(5000);
    /** Load status of the different levels of detail **/
    public static LoadStatus[] lodStatus = new LoadStatus[30];

    /** Daemon thread that gets the data loading requests and serves them **/
    private static DaemonLoader daemon;

    /** Adds an octant to the queue to be loaded **/
    public static void addToQueue(Object object) {
	loadQueue.add(object);
	if (object instanceof OctreeNode<?>)
	    ((OctreeNode<?>) object).setStatus(LoadStatus.QUEUED);
	else if (object instanceof Integer)
	    lodStatus[(Integer) object] = LoadStatus.QUEUED;
	// More than one second, flush
	flushLoadQueue();
    }

    /** Adds a list of octants to the queue to be loaded **/
    public static void addToQueue(OctreeNode<?>... octants) {
	synchronized (loadQueue) {
	    for (OctreeNode<?> octant : octants) {
		if (octant != null && octant.getStatus() == LoadStatus.NOT_LOADED) {
		    loadQueue.add(octant);
		    octant.setStatus(LoadStatus.QUEUED);
		}
	    }
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

    /**
     * Data will be pre-loaded at startup down to this octree depth.
     */
    private static int preloadDepth = 3;

    Longref starid = new Longref(1l);
    Longref errors = new Longref();

    ClientCore cc;
    List<SceneGraphNode> result;

    private static Map<Long, Pair<OctreeNode<SceneGraphNode>, long[]>> nodesMap;
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
	AbstractOctreeWrapper octreeWrapper = null;

	try {
	    EventManager.getInstance().post(Events.POST_NOTIFICATION, this.getClass().getSimpleName(), I18n.bundle.format("notif.objectserver.gettingdata"));

	    visid = GlobalConf.data.VISUALIZATION_ID;

	    /**
	     * CONNECT TO OBJECT SERVER
	     */
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

	    /**
	     * LOAD OCTREE METADATA
	     */
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

	    /**
	     * CREATE OCTREE WRAPPER WITH ROOT NODE
	     */
	    if (GlobalConf.performance.MULTITHREADING) {
		octreeWrapper = new OctreeWrapperConcurrent("Universe", root, GlobalConf.performance.NUMBER_THREADS);
	    } else {
		octreeWrapper = new OctreeWrapper("Universe", root);
	    }
	    octreeWrapper.initialize();

	    /** 
	     * LOAD LOD LEVELS - LOAD PARTICLE DATA
	     */
	    final List<SceneGraphNode> particleList = new ArrayList<SceneGraphNode>(100000);
	    int depthLevel = Math.min(OctreeNode.maxDepth, preloadDepth);
	    for (int level = 0; level <= depthLevel; level++) {
		loadLod(level, visid, errors, starid, octreeWrapper, particleList, true);
	    }

	    // Manually add sun
	    Star sun = new Star(new Vector3d(0, 0, 0), 4.83f, 4.83f, 0.656f, "Sol", starid.num++);
	    sun.initialize();
	    particleList.add(sun);

	    // Find out octant of sun
	    OctreeNode<SceneGraphNode> candidate = root.getBestOctant(sun.pos);
	    if (candidate == null) {
		EventManager.getInstance().post(Events.JAVA_EXCEPTION, new RuntimeException("No octant candidate for the Sun found!"));
	    } else {
		sun.pageId = candidate.pageId;
		// Add objects to octree wrapper node
		octreeWrapper.add(sun, candidate);
		candidate.add(sun);
	    }

	    // Add sun to octant

	} catch (ConnectException e) {
	    EventManager.getInstance().post(Events.POST_NOTIFICATION, I18n.bundle.get("notif.objectserver.notconnect"));
	    EventManager.getInstance().post(Events.JAVA_EXCEPTION, e);
	} catch (IOException e) {
	    EventManager.getInstance().post(Events.JAVA_EXCEPTION, e);
	}

	if (errors.num > 0l)
	    EventManager.getInstance().post(Events.JAVA_EXCEPTION, new RuntimeException(I18n.bundle.format("error.loading.objects", errors)));
	EventManager.getInstance().post(Events.POST_NOTIFICATION, this.getClass().getSimpleName(), I18n.bundle.format("notif.catalog.init", starid.num));

	/**
	 * INITIALIZE DAEMON LOADER THREAD
	 */
	daemon = new DaemonLoader(visid, starid, octreeWrapper);
	daemon.setDaemon(true);
	daemon.setName("daemon-objectserver-loader");
	daemon.setPriority(Thread.MIN_PRIORITY);
	daemon.start();

	// Add octreeWrapper to result list and return
	result.add(octreeWrapper);
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
     * Loads the given level of detail using the given particle list from the visualization identified by visid.
     * @param lod The level of detail to load.
     * @param visid The visualization id.
     * @param errors The errors reference.
     * @param starid The starid reference.
     * @param octreeWrapper The octree wrapper.
     * @param particleList The particle list to load the data to. This will be cleared.
     * @throws IOException 
     */
    public static void loadLod(final Integer lod, String visid, final Longref errors, final Longref starid, final AbstractOctreeWrapper octreeWrapper, final List<SceneGraphNode> particleList, boolean synchronous) throws IOException {
	lodStatus[lod] = LoadStatus.LOADING;
	// Fetch particle data for level 0
	Message msgParticle = new Message("visualization-lod-data?vis-id=" + visid
		+ "&lod-level=" + lod);
	msgParticle.setMessageHandler(new MessageHandler() {

	    @Override
	    public void receivedMessage(Message query, Message reply) {
		System.out.println(new Date() + " Receiving response: " + reply);
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

		// Update model
		for (SceneGraphNode sgn : particleList) {
		    Star s = (Star) sgn;
		    OctreeNode<SceneGraphNode> octant = nodesMap.get(s.pageId).getFirst();
		    synchronized (octant) {
			octant.add(s);
			s.page = octant;
			// Update status
			octant.setStatus(LoadStatus.LOADED);

			// Add objects to octree wrapper node
			octreeWrapper.add(s, octant);
		    }
		}
		particleList.clear();
		// Update status
		lodStatus[lod] = LoadStatus.LOADED;

		System.out.println(new Date() + " Response processed");
	    }

	    @Override
	    public void receivedMessageBlock(Message query, Message reply, MessagePayloadBlock block) {
	    }

	});
	System.out.println(new Date() + " Sending request: " + msgParticle);
	ClientCore.getInstance().sendMessage(msgParticle, synchronous);

    }

    /**
     * Loads the data of the given octant into the given list from the visualization identified by <tt>visid</tt>
     * @param octant The octant to load.
     * @param visid The visualization id.
     * @param errors The errors reference.
     * @param starid The star id reference.
     * @param octreeWrapper The octree wrapper.
     * @param particleList The list to load the data to.
     * @throws IOException
     */
    public static void loadOctant(final OctreeNode<SceneGraphNode> octant, String visid, final Longref errors, final Longref starid, final AbstractOctreeWrapper octreeWrapper, final List<SceneGraphNode> particleList, boolean synchronous) throws IOException {
	octant.setStatus(LoadStatus.LOADING);
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

		// Update model
		synchronized (octant) {
		    // Set objects to octant, and octant to objects
		    if (octant.objects != null) {
			particleList.addAll(octant.objects);
		    }
		    octant.setObjects(particleList);
		    for (SceneGraphNode particle : particleList) {
			((Star) particle).page = octant;
		    }

		    // Update status
		    octant.setStatus(LoadStatus.LOADED);
		}
		// Add objects to octree wrapper node
		octreeWrapper.add(octant.objects, octant);
		particleList.clear();
	    }

	    @Override
	    public void receivedMessageBlock(Message query, Message reply, MessagePayloadBlock block) {
	    }

	});

	ClientCore.getInstance().sendMessage(msgParticle, synchronous);

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
	private AbstractOctreeWrapper octreeWrapper;
	final List<SceneGraphNode> particleList = new ArrayList<SceneGraphNode>(50000);

	public DaemonLoader(String visid, Longref starid, AbstractOctreeWrapper aow) {
	    this.visid = visid;
	    this.starid = starid;
	    this.errors = new Longref(0l);
	    this.octreeWrapper = aow;
	}

	@Override
	public void run() {
	    while (true) {
		while (!loadQueue.isEmpty()) {
		    Object obj = loadQueue.poll();
		    if (obj instanceof OctreeNode) {
			OctreeNode<SceneGraphNode> octant = (OctreeNode<SceneGraphNode>) obj;
			EventManager.getInstance().post(Events.POST_NOTIFICATION, I18n.bundle.format("notif.loadingoctant", octant.pageId));
			try {
			    ObjectServerLoader.loadOctant(octant, visid, errors, starid, octreeWrapper, particleList, false);
			} catch (IOException e) {
			    EventManager.getInstance().post(Events.JAVA_EXCEPTION, e);
			    EventManager.getInstance().post(Events.POST_NOTIFICATION, I18n.bundle.format("notif.loadingoctant.fail", octant.pageId));
			    octant.setStatus(LoadStatus.LOADING_FAILED);
			}
		    } else if (obj instanceof Integer) {
			Integer lod = (Integer) obj;
			EventManager.getInstance().post(Events.POST_NOTIFICATION, I18n.bundle.format("notif.loadinglod", lod));
			try {
			    ObjectServerLoader.loadLod(lod, visid, errors, starid, octreeWrapper, particleList, false);
			} catch (Exception e) {
			    EventManager.getInstance().post(Events.JAVA_EXCEPTION, e);
			    EventManager.getInstance().post(Events.POST_NOTIFICATION, I18n.bundle.format("notif.loadinglod.fail", lod));
			    lodStatus[lod] = LoadStatus.LOADING_FAILED;
			}
		    }
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
