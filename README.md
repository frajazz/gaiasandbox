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

###1.2 Installation and uninstallation

Depending on your system and your personal preferences the installation
procedure may vary. Below is a description of the various installation methods
available.

####1.2.1 Windows

Two windows installers are available for 32 and 64-bit systems.

- `gaiasandbox_windows_[version].exe` - 32 bit installer.
- `gaiasandbox_windows-x64_[version].exe` - 64 bit installer.

To install the Gaia Sandbox, just double click on the installer and 
then follow the on-screen instructions. You will need to choose the 
directory where the application is to be installed.

In order to **uninstall** the application you can use the Windows Control Panel or
you can use the provided uninstaller in the Gaia Sandbox folder.

####1.2.2 Linux

We provide 3 packages for linux systems. `deb`, `rpm` and a linux installer.

#####1.2.2.1 DEB

This is the package for Debian-based distros (Debian, Ubuntu, Mint, SteamOS, etc.).
Download the `gaiasandbox_linux_[version].deb` file and run the
following command. You will need root privileges to install a `deb` package in 
your system.

```
sudo dpkg -i gaiasandbox_linux_[version].deb
```

This will install the application in the `/opt/gaiasandbox/` folder
and it will create the necessary shortcuts.

In order to **uninstall**, just type:

```
sudo apt-get remove gaiasandbox
```

#####1.2.2.2 RPM

This is the package for RPM-based distributions (Red Hat, Fedora, Mandriva, SUSE, CentOS, etc.)
Download the `gaiasandbox_linux_[version].rpm` file and run the
following command. You will need root privileges to install a `rpm` package in 
your system.

```
sudo yum install gaiasandbox_linux_[version].rpm
```

This will install the application in the `/opt/gaiasandbox/` folder
and it will create the necessary shortcuts.

In order to **uninstall**, just type:

```
sudo yum remove gaiasandbox-x86
```

#####1.2.2.3 Linux installer

We also provide a Linux installer which will trigger a graphical interface
where you can choose the installation location and some other settings.
Download the file `gaiasandbox_unix_[version].sh` to your disk.
Then run the following to start the installation.

```
./gaiasandbox_unix_[version].sh
```

Follow the on-screen instructions to proceed with the installation.

In order to **uninstall**, just run the `uninstall` file in the
installation folder.

####1.2.3 OS X - Mac

For OS X we provide a `gaiasandbox_macos_0_704b_d327966.dmg` file
which is installed by unpacking into the Applications folder. Once unpacked, the
installer will come up, just follow its instructions. 

####1.2.4 Compressed (TGZ) package

A `gaiasandbox-[version].tgz` package file is also provided. It will work
in all systems but you need to unpack it yourself and create the desired
shortcuts.
In **Windows**, use an archiver software (7zip, iZArc, etc.) to unpack it.

In **Linux** and **OS X**, you can use:
```
tar zxvf gaiasandbox-[version].tgz -C gaiasandbox/
```




######################################################
##  2. Running instructions                          #
######################################################

###2.1 Running the Gaia Sandbox

In order to run the program just click on the shortcut
provided in your operating system.

###2.2 Running from code

In order to run from code you will need [ant](http://ant.apache.org/)
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
- [PgsLookAndFeel](http://www.pagosoft.com/projects/pgslookandfeel/)
- Mark Taylor's [STIL](http://www.star.bristol.ac.uk/~mbt/stil/) library
- The [Jython Project](http://www.jython.org/)
- Tom Patterson ([www.shadedrelief.com]()) for some textures
- [Install4j](http://www.ej-technologies.com/products/install4j/overview.html) (multi-platform installer builder), for providing a free open source license
- Bitrock's [InstallBuilder](http://installbuilder.bitrock.com/) for providing a free open source license.
- And several online resources without which this would have not been possible

If you think I missed someone, please let me know.

