# Test script. Tests GUI scroll movement commands.
# Created by Toni Sagrista

from time import sleep
from gaia.cu9.ari.gaiaorbit.script import EventScriptingInterface


gs = EventScriptingInterface()

gs.disableInput()
gs.cameraStop()

gs.setGuiScrollPosition(20.0)
sleep(1)
gs.setGuiScrollPosition(40.0)
sleep(1)
gs.setGuiScrollPosition(60.0)
sleep(1)
gs.setGuiScrollPosition(80.0)
sleep(1)
gs.setGuiScrollPosition(100.0)
sleep(1)

gs.enableInput()
