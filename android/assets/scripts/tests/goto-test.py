# This script tests the go-to commands. To be run asynchronously.
# Created by Toni Sagrista

from time import sleep
from sandbox.script import EventScriptingInterface


gs = EventScriptingInterface()

gs.disableInput()
gs.cameraStop()
gs.minimizeInterfaceWindow()

gs.goToObject("Sol")

gs.setHeadlineMessage("Sun")
gs.setSubheadMessage("This is the Sun, our star")

sleep(5)
gs.clearAllMessages()

gs.goToObject("Earth")

gs.setHeadlineMessage("Earth")
gs.setSubheadMessage("This is the Earth, our home")

sleep(5)
gs.clearAllMessages()

gs.setCameraFocus("Sol")

gs.enableInput()
gs.maximizeInterfaceWindow()