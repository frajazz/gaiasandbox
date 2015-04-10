![Gaia Sandbox](http://www.zah.uni-heidelberg.de/fileadmin/user_upload/gaia/gaiasandbox/img/splash-s.jpg)
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
##  2. Running instructions                          #
######################################################

###2.1 Running the Gaia Sandbox

In order to run the program follow the instructions of your operating
system below.

####2.1.1 Linux
Open the terminal, untar and uncompress the downloaded archive,
give execution permissions to the run.sh file if necessary and then run it.
	
```
mkdir gaiasandbox/
tar zxvf gaiasandbox-[version].tgz -C gaiasandbox/
cd gaiasandbox/
chmod +x run.sh
run.sh
```

####2.1.2 Windows
In order to run the application on Windows, open a terminal window (write
'cmd' in the start menu search box) and run the run.bat file.
	
```
cd path_to_gaiasandbox_folder
run.bat
```

####2.1.3 MacOS
To run the application on MacOS systems, follow the same procedure
described in section *3.1.1 - Linux* section.

###2.2 Running from code

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
##  3. Documentation and help                        #
######################################################

In order to get the full up-to-date documentation, visit our Wiki 
pages in GitHub: 

[https://github.com/ari-zah/gaiasandbox/wiki](https://github.com/ari-zah/gaiasandbox/wiki)
	
There you will find a thorough documentation of all the features of the
Gaia Sandbox. If you need to load your data into the sandbox or just 
find out how to enable the 3D mode to experience it on your 3DTV, think no further, 
the [Gaia Sandbox wiki](https://github.com/ari-zah/gaiasandbox/wiki) is the way to go.



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
- Bitrock's [InstallBuilder](http://installbuilder.bitrock.com/) for providing a free open source license to their awesome InstallBuilder 9.
- And several online resources without which this would have not been possible

If you think I missed someone, please let me know.

