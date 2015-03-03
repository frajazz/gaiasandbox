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
import gaia.cu9.ari.gaiaorbit.util.parse.Parser;
import gaia.cu9.ari.gaiaorbit.util.tree.LoadStatus;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;
import gaia.cu9.object.server.ClientCore;
import gaia.cu9.object.server.commands.ICommand;
import gaia.cu9.object.server.commands.ICommand.CommandState;
import gaia.cu9.object.server.commands.ICommandListener;
import gaia.cu9.object.server.commands.Message;
import gaia.cu9.object.server.commands.MessageHandler;
import gaia.cu9.object.server.commands.MessagePayloadBlock;
import gaia.cu9.object.server.commands.plugins.ClientIdent;
import gaia.cu9.object.server.commands.plugins.VisualizationPage;
import gaia.cu9.object.server.utils.BufferedInputStreamReader;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

public class ObjectServerLoader implements ISceneGraphNodeProvider {
    /** The octant loading queue **/
    private static Queue<OctreeNode<?>> octantQueue = new ArrayBlockingQueue<OctreeNode<?>>(40000);
    /** The lod loading queue **/
    private static Queue<Integer> lodQueue = new ArrayBlockingQueue<Integer>(20);
    /** Load status of the different levels of detail **/
    public static LoadStatus[] lodStatus = new LoadStatus[30];

    /** Daemon thread that gets the data loading requests and serves them **/
    private static DaemonLoader daemon;

    /** Adds a lod to the queue to be loaded **/
    public static void addToQueue(Integer object) {
	lodQueue.add(object);
	lodStatus[(Integer) object] = LoadStatus.QUEUED;

	// Force flush
	flushLoadQueue(true);
    }

    /** Adds an octant to the queue to be loaded **/
    public static void addToQueue(OctreeNode<?> object) {
	octantQueue.add(object);
	((OctreeNode<?>) object).setStatus(LoadStatus.QUEUED);
    }

    /** Adds a list of octants to the queue to be loaded **/
    public static void addToQueue(OctreeNode<?>... octants) {
	for (OctreeNode<?> octant : octants) {
	    if (octant != null && octant.getStatus() == LoadStatus.NOT_LOADED) {
		octantQueue.add(octant);
		octant.setStatus(LoadStatus.QUEUED);
	    }
	}
    }

    public static void flushLoadQueue() {
	flushLoadQueue(false);
    }

    /**
     * Tells the loader to start loading the octants in the queue.
     */
    public static void flushLoadQueue(boolean force) {
	if (!daemon.awake && !octantQueue.isEmpty()) {
	    daemon.interrupt();
	}
    }

    /**
     * Data will be pre-loaded at startup down to this octree depth.
     */
    private static int preloadDepth = 6;

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
	    int depthLevel = Math.min(OctreeNode.maxDepth, preloadDepth);
	    for (int level = 0; level <= depthLevel; level++) {
		loadLod(level, visid, errors, starid, octreeWrapper, true);
	    }

	    // Manually add sun
	    Star sun = new Star(new Vector3d(0, 0, 0), 4.83f, 4.83f, 0.656f, "Sol", starid.num++);
	    sun.initialize();

	    // Find out octant of sun
	    OctreeNode<SceneGraphNode> candidate = root.getBestOctant(sun.pos);
	    if (candidate == null) {
		EventManager.getInstance().post(Events.JAVA_EXCEPTION, new RuntimeException("No octant candidate for the Sun found!"));
	    } else {
		sun.pageId = candidate.pageId;
		sun.page = candidate;
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

	/**
	 * INITIALIZE TIMER TO FLUSH THE QUEUE AT REGULAR INTERVALS
	 */
	Timer timer = new Timer(true);
	timer.schedule(new TimerTask() {
	    @Override
	    public void run() {
		flushLoadQueue();
	    }

	}, 1000, 2000);

	// Add octreeWrapper to result list and return
	result.add(octreeWrapper);
	return result;
    }

    private static Star parseLine(String line, Longref errors, Longref starid) {
	String[] tokens = line.split(";");
	try {
	    double x = Parser.parseDouble(tokens[0]) * Constants.PC_TO_U;
	    double y = Parser.parseDouble(tokens[1]) * Constants.PC_TO_U;
	    double z = Parser.parseDouble(tokens[2]) * Constants.PC_TO_U;

	    // Magnitude in virtual particles (type=92) must depend on number of particles contained
	    float mag = (float) ((tokens[3].equalsIgnoreCase("null") || tokens[3].isEmpty()) ? 4d : Parser.parseDouble(tokens[3]));
	    // Color in virtual particles should be that of the sun - yellowish
	    float bv = (float) ((tokens[4].equalsIgnoreCase("null") || tokens[4].isEmpty()) ? 0.656d : Parser.parseDouble(tokens[4]));

	    int particleCount = Parser.parseInt(tokens[7]);
	    long pageid = Parser.parseLong(tokens[8]);
	    int type = Parser.parseInt(tokens[9]);

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
    public static void loadLod(final Integer lod, String visid, final Longref errors, final Longref starid, final AbstractOctreeWrapper octreeWrapper, boolean synchronous) throws IOException {
	lodStatus[lod] = LoadStatus.LOADING;
	final List<SceneGraphNode> particleList = new ArrayList<SceneGraphNode>(500);
	// Fetch particle data for level 0
	Message message = new Message("visualization-lod-data?vis-id=" + visid
		+ "&lod-level=" + lod);
	message.setMessageHandler(new MessageHandler() {

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
		    block.__clearPayload();
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
		// Update status
		lodStatus[lod] = LoadStatus.LOADED;

	    }

	    @Override
	    public void receivedMessageBlock(Message query, Message reply, MessagePayloadBlock block) {
	    }

	});
	ClientCore.getInstance().sendMessage(message, synchronous);

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
    public static void loadOctant(final OctreeNode<SceneGraphNode> octant, String visid, final Longref errors, final Longref starid, final AbstractOctreeWrapper octreeWrapper, boolean synchronous) throws IOException {
	final List<SceneGraphNode> particleList = new ArrayList<SceneGraphNode>(500);
	octant.setStatus(LoadStatus.LOADING);

	VisualizationPage visPage = new VisualizationPage(visid, octant.pageId);
	visPage.addListener(new ICommandListener() {

	    @Override
	    public void notifyStateChange(ICommand command, CommandState state) {
		Message reply = command.getMessagePair().getReply();
		for (MessagePayloadBlock block : reply.getPayload()) {
		    byte[] payload = block.getPayloadAsByteArray();
		    ByteArrayInputStream bais = new ByteArrayInputStream(payload);
		    BufferedInputStreamReader sr = new BufferedInputStreamReader(bais, "UTF-8");
		    try {
			String line = null;
			while ((line = sr.readLine()) != null) {
			    Star star = parseLine(line, errors, starid);
			    if (star != null)
				particleList.add(star);
			}
			sr.close();
		    } catch (IOException e) {
			EventManager.getInstance().post(Events.JAVA_EXCEPTION, e);
		    }

		    block.__clearPayload();
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

	    }

	    @Override
	    public void notifyBlockReceived(ICommand command, CommandState state, MessagePayloadBlock block) {
	    }

	});

	ClientCore.getInstance().executeCommand(visPage, synchronous);

    }

    /**
     * Loads the objects of the given octants using the given list from the visualization identified by <tt>visid</tt>
     * @param octants The map of <pageId, octant> holding the octants to load.
     * @param visid The visualization id.
     * @param errors The errors reference.
     * @param starid The star id reference.
     * @param octreeWrapper The octree wrapper.
     * @param particleList The list to load the data to.
     * @throws IOException
     */
    public static void loadOctants(final Map<Long, OctreeNode<SceneGraphNode>> octants, String visid, final Longref errors, final Longref starid, final AbstractOctreeWrapper octreeWrapper, boolean synchronous) throws IOException {
	final List<SceneGraphNode> particleList = new ArrayList<SceneGraphNode>(500);

	VisualizationPage visPage = new VisualizationPage(visid, octants.keySet());
	visPage.addListener(new ICommandListener() {

	    @Override
	    public void notifyStateChange(ICommand command, CommandState state) {

	    }

	    @Override
	    public void notifyBlockReceived(ICommand command, CommandState state, MessagePayloadBlock block) {
		byte[] payload = block.getPayloadAsByteArray();
		ByteArrayInputStream bais = new ByteArrayInputStream(payload);
		BufferedInputStreamReader sr = new BufferedInputStreamReader(bais, "UTF-8");
		try {
		    String line = null;
		    while ((line = sr.readLine()) != null) {
			Star star = parseLine(line, errors, starid);
			if (star != null)
			    particleList.add(star);
		    }
		    sr.close();
		} catch (IOException e) {
		    EventManager.getInstance().post(Events.JAVA_EXCEPTION, e);
		}

		block.__clearPayload();

		for (SceneGraphNode star : particleList) {
		    OctreeNode<SceneGraphNode> octant = octants.get(((Star) star).pageId);
		    if (octant != null) {
			// Update model
			synchronized (octant) {
			    // Set objects to octant, and octant to objects
			    octant.add(star);
			    ((Star) star).page = octant;

			    // Update status
			    octant.setStatus(LoadStatus.LOADED);
			    octreeWrapper.add(star, octant);
			}
		    }
		}
		particleList.clear();

	    }

	});

	ClientCore.getInstance().executeCommand(visPage, synchronous);
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
	private Map<Long, OctreeNode<SceneGraphNode>> toLoad;

	public DaemonLoader(String visid, Longref starid, AbstractOctreeWrapper aow) {
	    this.visid = visid;
	    this.starid = starid;
	    this.errors = new Longref(0l);
	    this.octreeWrapper = aow;
	    this.toLoad = new HashMap<Long, OctreeNode<SceneGraphNode>>();
	}

	@Override
	public void run() {
	    while (true) {
		/** ----------- PROCESS OCTANTS ----------- **/
		while (!octantQueue.isEmpty()) {
		    toLoad.clear();
		    while (octantQueue.peek() != null) {
			OctreeNode<SceneGraphNode> octant = (OctreeNode<SceneGraphNode>) octantQueue.poll();
			toLoad.put(octant.pageId, octant);
		    }

		    // Load octants if any
		    if (!toLoad.isEmpty()) {
			EventManager.getInstance().post(Events.POST_NOTIFICATION, I18n.bundle.format("notif.loadingoctants", toLoad.size()));
			try {
			    ObjectServerLoader.loadOctants(toLoad, visid, errors, starid, octreeWrapper, false);
			} catch (Exception e) {
			    EventManager.getInstance().post(Events.JAVA_EXCEPTION, e);
			    EventManager.getInstance().post(Events.POST_NOTIFICATION, I18n.bundle.get("notif.loadingoctants.fail"));
			}
		    }
		}

		/** ----------- PROCESS LODS ----------- **/
		while (!lodQueue.isEmpty()) {
		    Integer lod = (Integer) (lodQueue.poll());
		    EventManager.getInstance().post(Events.POST_NOTIFICATION, I18n.bundle.format("notif.loadinglod", lod));
		    try {
			ObjectServerLoader.loadLod(lod, visid, errors, starid, octreeWrapper, false);
		    } catch (Exception e) {
			EventManager.getInstance().post(Events.JAVA_EXCEPTION, e);
			EventManager.getInstance().post(Events.POST_NOTIFICATION, I18n.bundle.format("notif.loadinglod.fail", lod));
			lodStatus[lod] = LoadStatus.LOADING_FAILED;
		    }
		}

		/** ----------- SLEEP UNTIL INTERRUPTED ----------- **/
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
