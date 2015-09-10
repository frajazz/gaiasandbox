# Welcome script to be run asynchronously.
# This script is executed when the GaiaSandbox is first started.
# Created by Toni Sagrista

from time import sleep
from gaia.cu9.ari.gaiaorbit.script import EventScriptingInterface

gs = EventScriptingInterface()

# Disable input
gs.disableInput()
gs.cameraStop()
gs.minimizeInterfaceWindow()

# Welcome
gs.setHeadlineMessage("Welcome to the Gaia Sandbox")
gs.setSubheadMessage("Explore Gaia, the Solar System and the whole Galaxy!")

sleep(2.5)

# Earth
gs.setHeadlineMessage("Earth")
gs.setSubheadMessage("This is our planet, the Earth")
gs.setCameraFocus("Earth")

sleep(3.5)

# Sun
gs.setHeadlineMessage("Sun")
gs.setSubheadMessage("This is our star, the Sun")
gs.setCameraFocus("Sol")

sleep(3.5)

# Back to Earth
gs.setCameraFocus("Earth")

# Maximize interface and enable input
gs.clearAllMessages()
gs.maximizeInterfaceWindow()
gs.enableInput()
