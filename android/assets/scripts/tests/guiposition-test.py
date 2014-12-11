# Test script. Tests GUI position commands.
# Created by Toni Sagrista

from time import sleep
from sandbox.script import EventScriptingInterface


gs = EventScriptingInterface()

gs.disableInput()
gs.cameraStop()
gs.maximizeInterfaceWindow()

gs.setGuiPosition(0, 0)
sleep(1)
gs.minimizeInterfaceWindow()
gs.setGuiPosition(0, 0)
sleep(1)
gs.maximizeInterfaceWindow()
gs.setGuiPosition(0.5, 0.5)
sleep(1)
gs.setGuiPosition(1, 1)
sleep(1)
gs.setGuiPosition(0, 1)

gs.enableInput()