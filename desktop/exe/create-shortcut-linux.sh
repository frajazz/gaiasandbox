#!/bin/bash
#
# Creates the .desktop shortcut in linux
#

# Copy shortcut to apps folder
cp /opt/GaiaSandbox/gaiasandbox.desktop ~/.local/share/applications

# global.properties file permissions
chmod ugo+w /opt/GaiaSandbox/conf/global.properties