![Gaia Sandbox](http://www.zah.uni-heidelberg.de/fileadmin/user_upload/gaia/gaiasandbox/img/banner-gs.jpg)
-------------------------
--------------------------

[![Circle CI](https://circleci.com/gh/ari-zah/gaiasandbox/tree/master.svg?style=svg)](https://circleci.com/gh/ari-zah/gaiasandbox/tree/master)

The **Gaia Sandbox** is a real-time, 3D, astronomy visualisation software that
runs on Windows, Linux and MacOS. It is developed in the framework of
[ESA](http://www.esa.int/ESA)'s [Gaia mission](http://sci.esa.int/gaia) to chart about 1 billion stars of our Galaxy.
To get the latest up-to-date and most complete information,
visit our **wiki** pages in <https://github.com/ari-zah/gaiasandbox/wiki>.

This file contains the following sections:

1. Installation instructions and requirements
2. Configuration instructions
3. Running instructions
4. Copyright and licensing information
5. Contact information
6. Credits and acknowledgements



######################################################
##  1. Installation instructions and requirements    #
######################################################

###1.1 Requirements

- **Operating System** - 
The Gaia Sandbox application runs on Windows, MacOS and
Linux.

- **Java** - 
In order to run this software you will need the Java
Runtime Environment (JRE) 7+ installed in your system.
We recommend using the Oracle HotSpot JVM
(http://www.oracle.com/technetwork/java/javase/downloads/index.html).

- **OpenGL** - 
Also, you will need an operating system/graphics card that supports
at least OpenGL 2.0.

- **Hardware** - 
Specific system requirements are yet to be determined.

###1.2 Installation (package)

The Gaia Sandbox application does not require a 'formal'
installation, as it is ready to be executed out of the box. Just uncompress the package anywhere in your drive
and you are good to go.



######################################################
##  2. Configuration instructions                    #
######################################################

###2.1 Configuration interface
The configuration of the application can be done almost
entirely using the graphical interface.

####2.1.1 Resolution and mode

You can find the `Resolution and mode` configuration
under the `Graphics` tab. There you can switch between
full screen mode and windowed mode. 
In order to switch from full screen mode to windowed
mode during the execution, use the key `F11`.

#####2.1.1.1 Antialiasing

In the `Graphics` tab you can also find the antialiasing 
configuration. Applying antialiasing removes the
jagged edges of the scene and makes it look better.
There are four main options, namely No-Antialiasing, FXAA,
NFAA and MSAA.

#####2.1.1.2 VSYNC - Vertical synchronization
This option limits the frames per second to match your monitor's
refresh rate and prevent screen tearing.

####2.1.2 Interface
You can select your favourite langauge in the tab `Interface`.
The list of available languages is expected to grow. Also, there
are a number of visual themes available.

####2.1.3 Performance and multithreading
In the `Performance` tab you can enable and disable multithreading.
This allows the program to use more than one CPU.

####2.1.4 Controls
You can see the key associations in the `Controls` tab. Right now
these are not editable, but they will be in the future.

####2.1.5 Screenshot configuration
You can take screenshots anytime when the application is running by 
pressing `F5`. The resolution (size) and save path of these screenshots can
be modified using this tab.

####2.1.6 Frame output
There is a feature in the Gaia Sandbox that enables the output
of every frame as an image. This is useful to produce videos. In order to
configure the frame output system, use the `Frame output` tab. There
you can select the output folder, the image prefix name, the output
image resolution (size) and the target frames per second. 
You have to take it into account when you later
use your favourite video encoder [ffmpeg](https://www.ffmpeg.org/) to convert the frame
images into videos.

####2.1.7 Check for new version
You can always check for a new version by clicking on this button.
By default, the application checks for a new version if more than 
five days have passed since the last check.

####2.1.8 Do not show that again!
If you do not want this configuration dialog to be displayed again.


###2.2 Configuration file
The configuration file is located in `conf/global.properties`. Below
are some of the properties found in this file that are not
represented in the GUI.

- **graphics.render.time** - 
This property gets a boolean (`true`|`false`) and indicates whether
the timestamp is to be added to screenshots and frames.

- **data.sg.file** - 
This points to the scene graph file containing the root of the 
data loading. This will be soon deprecated.

- **data.limit.mag** - 
This contains the magnitude limit above which stars will
not be loaded.

- **program.tutorial** - 
This gets a boolean (`true`|`false`) indicating whether the tutorial
script should be automatically run at startup.

- **program.tutorial.script** - 
This points to the tutorial script file.

- **program.debuginfo** - 
If this property is set to true, information on the number of stars
rendered as a quad, the number of stars rendered as a point and
the frames per second will be shown at the top-right.

- **program.ui.theme** - 
Specifies the GUI theme. Two themes are available: `bright` and `dark`.
	
	

######################################################
##  3. Running instructions                          #
######################################################

###3.1 Running the Gaia Sandbox

In order to run the program follow the instructions of your operating
system below.

####3.1.1 Linux
Open the terminal, untar and uncompress the downloaded archive,
give execution permissions to the run.sh file if necessary and then run it.
	
```
mkdir gaiasandbox/
tar zxvf gaiasandbox-[version].tgz gaiasandbox/ -C gaiasandbox/
cd gaiasandbox/
chmod +x run.sh
run.sh
```

####3.1.2 Windows
In order to run the application on Windows, open a terminal window (write
'cmd' in the start menu search box) and run the run.bat file.
	
```
cd path_to_gaiasandbox_folder
run.bat
```

####3.1.3 MacOS
To run the application on MacOS systems, follow the same procedure
described in section *3.1.1 - Linux* section.

###3.2 Running from code

In order to run from code you will need [ant}(http://ant.apache.org/)
and [ivy](http://ant.apache.org/ivy/). You can probably get
ant and ivy from your Linux distribution package manager. For example, in Ubuntu 
press `Ctrl+Alt+T` and type in:
```
sudo apt-get install ant ivy
```
First, clone the [GitHub](https://github.com/ari-zah/gaiasandbox) repository:
```
cd $GIT_FOLDER
git clone https://github.com/ari-zah/gaiasandbox.git
```
Then, run the following commands to compile and run:
```
cd $GIT_FOLDER/gaiasandbox
ant compile
ant run
```
Et voilà! The Gaia Sandbox is running in your machine.
	

######################################################
##  4. Copyright and licensing information           #
######################################################

This software is published and distributed under the LGPL
(Lesser General Public License) license. You can find the full license
text here https://github.com/ari-zah/gaiasandbox/blob/master/LICENSE
or visiting https://www.gnu.org/licenses/lgpl-3.0-standalone.html



######################################################
##  5. Contact information                           #
######################################################

The main webpage of the project is 
http://www.zah.uni-heidelberg.de/gaia2/outreach/gaiasandbox. There you can find 
the latest versions and the latest information on the Gaia Sandbox. You can also
visit our Github account to inspect the code, check the wiki or report bugs:
https://github.com/ari-zah/gaiasandbox

###5.1 Main designer and developer
- **Toni Sagrista Selles**
	- **E-mail**: tsagrista@ari.uni-heidelberg.de
	- **Personal webpage**: www.tonisagrista.com


###5.2 Contributors
- **Dr. Stefan Jordan**
	- **E-mail**: jordan@ari.uni-heidelberg.de
	- **Personal webpage**: www.stefan-jordan.de




######################################################
##  6. Credits and acknowledgements                  #
######################################################

The author would like to acknowledge the following people, or the
people behind the following technologies/resources:

- The [DLR](http://www.dlr.de/) for financing this project
- The [BWT](http://www.bmwi.de/), Bundesministerium für Wirtschaft und Technologie, also supporting this project
- My institution, [ARI](http://www.ari.uni-heidelberg.de)/[ZAH](http://www.zah.uni-heidelberg.de/)
- Dr. Martin Altmann for providing the Gaia orbit data
- [Libgdx](http://libgdx.badlogicgames.com)
- [libgdx-contribs-postprocessing](https://github.com/manuelbua/libgdx-contribs/tree/master/postprocessing)
- [HYG catalog](http://www.astronexus.com/hyg)
- [WebLaF](http://weblookandfeel.com/)
- Mark Taylor's [STIL](http://www.star.bristol.ac.uk/~mbt/stil/) library
- The [Jython Project](http://www.jython.org/)
- And several online resources without which this would have not been possible

If you think I missed someone, please let me know.

