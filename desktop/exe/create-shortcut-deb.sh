#!/bin/bash
#
# Creates the .desktop shortcut in Debain systems
#

# Copy shortcut to apps folder
cp /opt/GaiaSandbox/gaiasandbox.desktop $HOME/.local/share/applications

# global.properties file permissions
chmod ugo+w /opt/GaiaSandbox/conf/global.properties