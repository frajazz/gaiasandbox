#################################################################
GAIA SANDBOX APPLICATION README FILE
#################################################################

This is the README file for the Gaia Sandbox

This file contains the following sections:

1 - Installation instructions and requirements
2 - Configuration instructions
3 - Running and operating instructions
4 - Copyright and licensing information
5 - Contact information for the distributor or programmer
6 - Known bugs
7 - Troubleshooting
8 - Credits and acknowledgements
9 - Changelog



######################################################
#  1 - INSTALLATION INSTRUCTIONS AND REQUIREMENTS    #
######################################################

1.1 REQUIREMENTS

Operating System ----
The Gaia Sandbox application runs on Windows, MacOS and
Linux. So far it has only been tested to work on various
versions of Ubuntu, Mint and Debian. It has also been
tested in Windows 7 and MacOS 10.6 (Snow leopard) and
10.10 (Yosemite).

Java ----
In order to run this software you will need the Java
Runtime Environment (JRE) 7+ installed in your system.
It will not run with previous versions, so please,
update if you have to.
We recommend using the Oracle HotSpot JVM
(http://www.oracle.com/technetwork/java/javase/downloads/index.html),
as it is the only one we test, but it may also run
in IBM's JVM or in the OpenJDK JVM.

OpenGL ----
Also, you will need an operating system/graphics card that support
at least OpenGL 2.0.

Hardware ----
We have not tested the application on many different
machines, but it runs well on dated graphic cards
(~GeForce 210) and average CPUs (i5). Specific 
system requirements are yet to be determined.

1.2 INSTALLATION

The Gaia Sandbox application does not require a 'formal'
installation, as it is ready to be executed out of the
box. Just uncompress the package anywhere in your drive
and you are good to go.



######################################################
#  2 - CONFIGURATION INSTRUCTIONS                    #
######################################################

2.1 CONFIGURATION GUI
The configuration of the application can be done almost
entirely using the graphical interface that pops up
when the program is run. It should be pretty self-
explanatory, but here is a description of the most important.

2.1.1 RESOLUTION AND MODE

You can find the 'Resolution and mode' configuration
under the 'Graphics' tab. There you can switch between
full screen mode and windowed mode. In the case of full screen,
you can choose the resolution from a list of
supported resolutions in a drop down menu. If you choose
windowed mode, you can enter the resolution you want. You can
also choose whether the window should be resizable or not.
In order to switch from full screen mode to windowed
mode during the execution, use the key F11.

2.1.2 ANTIALIASING

In the 'Graphics' tab you can also find the antialiasing 
configuration. Applying antialiasing removes the
jagged edges of the scene and makes it look better. However, 
it does not come free of cost, and usually has a penalty
on the frames per second (FPS).
There are four main options, described below.

2.1.2.1 NO ANTIALIASING
If you choose this no antialiasing will be applied, and
therefore you will probably see jagged edges. This has no
penalty on either the CPU or the GPU.
If want you enable antialiasing with 'override application settings'
in your graphics card driver configuration program, you can
leave the application antialiasing setting to off.  

2.1.2.2 FXAA - FAST APPROXIMATE ANTIALIASING
This is a post-processing antialiasing which is very fast
and produces reasonably good results. It has some impact on the
FPS depending on how fast your graphics card is.
As it is a post-processing effect, this will work also when
you take screenshots or output the frames.
You can find a description of FXAA here:
http://en.wikipedia.org/wiki/Fast_approximate_anti-aliasing

2.1.2.2 NFAA - NORMAL FIELD ANTIALIASING
This is yet another post-processing antialiasing technique. It is
based on generating a normal map to detect the edges for later 
smoothing.
It may look better on some devices and the penalty in FPS is 
small. It will also work for the screenshots and frame outputs.

2.1.2.3 MSAA - MULTI SAMPLE ANTIALIASING
This is implemented by the graphics card and may not always be
available. You can choose the number of samples (from 2 to 16, from
worse to better) and it has a bigger cost on FPS than the 
post-processing options. It also looks better.
However, this being reliant on a special multisample frame buffer 
in the graphics card makes it not available for screenshots and
frame outputs.

2.1.3 VSYNC - VERTICAL SYNCHRONIZATION
This option limits the frames per second to match your monitor's
refresh rate and prevent screen tearing. It is recommended
to leave it enabled unless you want to test how many FPS you
can get or you want to fry your card.

2.1.3 PERFORMANCE AND MULTITHREADING
In the 'Performance' tab you can enable and disable multithreading.
This allows the program to use more than one CPUs for the
processing. If you have a multi-core CPU it is a good idea to
enable multithreading and set the number of threads to 'let the
program decide'. If you have other requirements, you can also limit
the number of threads to leave some free space for other applications.

2.1.4 CONTROLS
You can see the key associations in the 'Controls' tab. Right now
these are not editable, but they will be in the future.

2.1.5 SCREENSHOT CONFIGURATION
You can take screenshots anytime when the application is running by 
pressing F5. The resolution (size) of these screenshots does not need to be
the same as the running resolution, and that is why the 'Screenshots'
tab lets you specify a resolution. You can also specify a default
save folder where the screenshots will be stored.

2.1.6 FRAME OUTPUT
There is a feature in the Gaia Sandbox that enables the output
of every frame as an image. This is useful to produce videos. In order to
configure the frame output system, use the 'Frame output' tab. There
you can select the output folder, the image prefix name, the output
image resolution (size) and the target frames per second. When the
program is in frame output mode, it does not run in real time but it 
adjusts the internal clock to produce as many frames per second
as specified here. You have to take it into account when you later
use your favourite video encoder (ffmpeg) to convert the frame
images into a video.

2.1.7 CHECK FOR NEW VERSION
You can always check for a new version by clicking on this button.
By default, the application checks for a new version if more than 
five days have passed since the last check. If a new version
is found, you will see the notice here together with a link to
the download.

2.1.8 DO NOT SHOW AGAIN!
If you do not want this configuration dialog to be displayed again
when you launch the Gaia Sandbox, tick this checkbox and
you are good to go.


2.2 CONFIGURATION FILE
There is a configuration file which stores most of
the properties explained in the previous section and some
more. This section is devoted to these properties
that are not represented in the GUI but are still
configurable.
The configuration file is located in conf/global.properties. Here
are some of the properties found in this file that are not
represented in the GUI.

-graphics.render.time-
This property gets a boolean (true|false) and indicates whether
the timestamp is to be added to screenshots and frames.

-data.sg.file-
This points to the scene graph file containing the root of the 
data loading. This will be soon deprecated.

-data.limit.mag-
This contains the magnitude limit above which stars will
not be loaded.

-program.tutorial-
This gets a boolean (true|false) indicating whether the tutorial
script should be automatically run at startup.

-program.tutorial.script-
This points to the tutorial script file.

-program.debuginfo-
If this property is set to true, some debug information will be 
shown at the top right of the window. This contains information
such as the number of stars rendered as a quad, the number of stars
rendered as a point or the frames per second.

-program.ui.theme-
Specifies the GUI theme. Two themes are available: birght and dark.
	
	

######################################################
#  3 - RUNNING AND OPERATING INSTRUCTIONS            #
######################################################

3.1 RUNNING THE PROGRAM

In order to run the program follow the instructions of your operating
system.

3.1.1 LINUX
In order to run the application on Linux, open the terminal, give execution
permissions to the run.sh file and then run it.

	> cd path_to_gaiasandbox_folder/
	> chmod +x run.sh
	> run.sh
	
Alternatively you can run the jar file directly, specifying the configuration
file.

	> java -Dproperties.file=conf/global.properties -jar gaiasandbox.jar

3.1.2 WINDOWS
In order to run the application on Windows, open a terminal window (write
'cmd' in the start menu search box) and run the run.bat file.

	> cd path_to_gaiasandbox_folder\
	> run.bat
	
Alternatively you can run the jar file directly, specifying the configuration
file.

	> java -Dproperties.file=conf/global.properties -jar gaiasandbox.jar

3.1.3 MAC OS
To run the application on MacOS systems, run the jar file specifying the
configuration file.

	> java -Dproperties.file=conf/global.properties -jar gaiasandbox.jar

3.2 OPERATING INSTRUCTIONS

3.2.1 USER INTERFACE
The Gaia Sandbox application has an on-screen user interface designed to be
easy to use. It is divided into five sections, Time, Camera, Objects, Type
visibility, Lighting and Gaia scan.

3.2.1.1 TIME
You can play and pause the simulation using the PLAY/PAUSE button in the
OPTIONS window to the left. You can also use SPACE to play and pause the time.
You can also change the pace, which is the simulation time to real time ratio, expressed in [h/sec].
If the pace is 2.1, then one second of real time translates to two 
hours of simulation time. Use '[' and ']' to divide by 2 and double 
the value of the time pace.
Finally, the current simulation date is given in the bottom box of
the Time group.

3.2.1.2 CAMERA
In the camera options pane on the left you can select the type of camera. 
This can also be done by using the Numpad 0-4 keys.
There are three camera modes: Free mode, where the camera is not linked
to any object and its velocity is exponential with respect to the distance
to the origin (Sun), Focus mode, where the camera is linked to a focus
object and it rotates and rolls with respect to it, and Gaia FOV, where the
camera simulates either of the fields of view of Gaia, or both.
Additionally, there are a number of sliders for you to control
different parameters of the camera:

-Field of view: Controls the field of view angle of the camera. The bigger
it is, the larger the portion of the scene represented.
-Camera speed: Controls the longitudinal speed of the camera.
-Rotation speed: Controls the transversal speed of the camera, how fast it
rotates around an object.
-Turn speed: Controls the turning speed of the camera.

Finally, you can lock the camera to the focus when in focus mode. Doing so
links the reference system of the camera to that of the object and thus
it moves with it.

3.2.1.3 OBJECTS
There is a list of focus objects that can be selected from
the interface. When an object is selected the camera automatically centers
it in the view and you can rotate around it or zoom in and out.
Objects can also be selected by double-clicking on them directly in the view or
by using the search box provided above the list. You can also invoke a search dialog
by pressing CTRL+F.

3.2.1.4 TYPE VISIBILITY
Most graphical elements can be turned off and on using these
toggles. For example you can remove the stars from the display by
clicking on the 'stars' toggle. The object types available are 
the following:

-Stars
-Planets
-Moons
-Satellites, the spacecrafts
-Asteroids
-Labels, all the text labels
-Equatorial grid
-Ecliptic grid
-Galactic grid
-Orbits, the orbit lines
-Atmospheres, the atmospheres of planets
-Constellations, the constellation lines
-Boundaries, the constellation boundaries
-Milky way
-Others

3.2.1.5 LIGHTING
Here are a few options to control the lighting of the scene:

-Star brightness: Controls the brightness of stars.
-Ambient light: Controls the amount of ambient light. This only
affects the models such as the planets or satellites.
-Bloom effect: Adds a nice bloom effect to the scene. Some
people can't stand it, so it is better to leave this at the 
user's consideration.
-Camera lens: Enable or disable the camera lens effect. This 
has some impact on the performance, specially if you have a 
below-the-average graphics card.

3.2.1.6 GAIA SCAN
You can also enable the real time computation of Gaia observation. To
do so, tick the 'Enable Gaia scan' checkbox. Keep in mind that this computation 
is done by interpolating the scan path and calculating what stars fall
into Gaia's both fields of view, so if you set the time pace very
high it is going to take a toll on the frames per second.
Also, you can choose to colour the stars observed by Gaia according
to the number of observations, where purple is 1 and red is 75. To do so,
tick the 'Colour observed stars' checkbox.
Finally, you can decide to only display the stars that have been observed
by Gaia at least once. To do so, tick the 'Show only observed stars'
checkbox.

3.2.2 RUNNING SCRIPTS
In order to run python scripts, click on the 'Run script...' button at the
bottom of the GUI window. A new window will pop up allowing you to select
the script you want to run. Once you have selected it, the script will be
checked for errors. If no errors were found, you will be notified in the
box below and you'll be able to run the script right away by clicking on
the 'Run' button. If the script contains errors, you will be notified
in the box below and you will not be able to run the script until these
errors are dealt with.

3.2.3 CONFIGURATION WINDOW
You can launch the preferences window any time during the execution
of the program. To do so, click on the 'Preferences' button at
the bottom of the GUI window. For a detailed description of the
configuration options refer to chapter 2 - Configuration Instructions.


3.2.4 CONTROLS
This section describes the controls of the Gaia Sandbox.

3.2.2.1 KEYBOARD CONTROLS

Numpad 0-4			Change camera mode
		0 -	Free camera
		1 -	Focus camera
		2 -	Gaia FoV 1 camera
		3 -	Gaia FoV 2 camera
		4 -	Gaia FoV 1 and 2 camera
		
SPACE				Toggle simulation play/pause
F5					Take screenshot
F11					Toggle fullscreen/windowed mode
L-CTRL+F				Search dialog
ESCAPE				Quit application
-					Decrease limiting magnitude
+					Increase limiting magnitude
[					Divide time pace by two
]					Double time pace
*					Reset limiting magnitude
B					Toggle constellation boundaries
C					Toggle constellation lines
E					Toggle ecliptic grid
G					Toggle galactic grid
L					Toggle labels
M					Toggle moons
O					Toggle orbits
P					Toggle planets
Q					Toggle equatorial grid
S					Toggle stars
T					Toggle satellites


3.2.2.2 MOUSE CONTROLS

L-MOUSE DOUBLE CLICK		Select object as focus
L-MOUSE + DRAG				Pitch and yaw (FREE mode) or rotate around foucs (FOCUS mode)
L-SHIFT + L-MOUSE + DRAG	Camera roll
L-CTRL + L-MOUSE + DRAG		Deviate camera line of sight from focus
M-MOUSE + DRAG or WHEEL		Forward/backward movement

######################################################
#  4 - COPYRIGHT AND LICENSING INFORMATION           #
######################################################

This software is published and distributed under the LGPL
(Lesser General Public License) license. You can find the full license
text in the LICENSE.txt file or visiting 
https://www.gnu.org/licenses/lgpl-3.0-standalone.html



######################################################
#  5 - CONTACT INFORMATION                           #
######################################################

The main webpage of the project is 
http://www.zah.uni-heidelberg.de/gaia2/outreach/gaiasandbox. There you can find 
the latest versions and the latest information on the Gaia Sandbox. You can also
visit our Github account to inspect the code, check the wiki or report bugs:
https://github.com/ari-zah/gaiasandbox

5.1 MAIN DESIGNER AND DEVELOPER
Toni Sagrista Selles
E-mail: tsagrista@ari.uni-heidelberg.de
Personal webpage: www.tonisagrista.com


5.2 CONTRIBUTORS
Dr. Stefan Jordan
E-mail: jordan@ari.uni-heidelberg.de
Personal webpage: www.stefan-jordan.de



######################################################
#  6 - KNOWN BUGS, ISSUES and TODOs                  #
######################################################




######################################################
#  7 - TROUBLESHOOTING                               #
######################################################

TODO



######################################################
#  8 - CREDITS AND ACKNOWLEDGEMENTS                  #
######################################################

The author would like to acknowledge the following people, or the
people behind the following technologies/resources:

-The DLR (http://www.dlr.de/) for financing this project.
-Dr. Martin Altmann for providing the Gaia orbit data.
-Libgdx - http://libgdx.badlogicgames.com
-libgdx-contribs-postprocessing - https://github.com/manuelbua/libgdx-contribs/tree/master/postprocessing
-HYG catalog - http://www.astronexus.com/hyg
-Several online resources without which this would have not been possible. 
-WebLaF - http://weblookandfeel.com/



######################################################
#  9 - CHANGELOG                                     #
######################################################

version 0.703b - 11/12/2014
-First public beta
-Huge update, about everything has been modified

version 0.505b - ???
-Position model of Earth with respect to Sun uses real Sun's longitude.
-Distance of Earth to Sun worked out using astronomical algorithms by Meeus.
-Position of Moon worked out using astronomical algorithms by Meeus.
-Fixed distance field font bug.
-Fixed bug where time could not expand outside the time boundaries
of the orbit file.
-Added all planets using VSOP87.
-Updated to libgdx-1.0.0.
-Major source refactoring to fit planets in.
-Use of extended viewport, window is resizable in windowed mode.


version 0.504b - 16/04/2014
-Fixed Sun distance in FOCUS mode.
-Log messages hidden by default.
-Smooth shading for all models except mw.
-Normals removed from mw model. Transparency tweaked.
-Initial camera position moved to Gaia.

version 0.503b - 15/04/2014
-Added distance to focus.
-Fixed Java8 library issue.
-Fixed font shader for MacOS.

version 0.502b - 14/04/2014
-First public version.

