#!/bin/bash
#
# Creates the .desktop shortcut in RPM based distros
#

# Copy shortcut to apps folder
cp /opt/GaiaSandbox/gaiasandbox.desktop /usr/local/share/applications

# global.properties file permissions
chmod ugo+w /opt/GaiaSandbox/conf/global.properties