# Copy shortcut to applications folder
cp $I4J_INSTALL_LOCATION/gs.desktop $HOME/.local/share/applications/gaiasandbox.desktop

# global.properties file permissions
chmod ugo+w $I4J_INSTALL_LOCATION/conf/global.properties