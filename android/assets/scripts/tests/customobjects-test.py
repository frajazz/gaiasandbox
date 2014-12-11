# This script tests the displaying of custom messages and images.
# Created by Toni Sagrista

from time import sleep
from sandbox.script import EventScriptingInterface


gs = EventScriptingInterface()

# Minimize interface, disable input, stop camera
gs.disableInput()
gs.cameraStop()
gs.minimizeInterfaceWindow()

# Add messages
gs.displayMessageObject(0, "This is the zero message", 0.2, 0.0, 1.0, 0.0, 0.0, 8)
sleep(1.5)
gs.displayMessageObject(1, "This is the first message", 0.2, 0.2, 0.3, 0.4, 0.6, 10)
sleep(1.5)
gs.displayMessageObject(2, "This is the second message", 0.3, 0.1, 0.0, 1.0, 0.0, 11)
sleep(1.5)
gs.displayImageObject(10, "scripts/customobjects-test/profile.png", 0.1, 0.7)
gs.displayMessageObject(3, "Monkey!", 0.7, 0.62, 0.9, 0.0, 1.0, 18)
sleep(1.5)
gs.displayMessageObject(4, "This is the fourth message", 0.4, 0.6, 1.0, 1.0, 0.0, 17)
sleep(1.5)
gs.displayMessageObject(5, "This is the fifth message", 0.5, 0.8, 1.0, 0.0, 1.0, 22)
sleep(1.5)
gs.removeObject(0)
sleep(1.5)
gs.removeObject(1)
sleep(1.5)
gs.removeObject(2)
sleep(1.5)
gs.removeObject(3)
sleep(1.5)
gs.removeObject(4)
sleep(1.5)
gs.removeObject(5)
sleep(1.5)
gs.removeObject(10)
sleep(1.5)

# Maximize interface and enable input
gs.maximizeInterfaceWindow()
gs.enableInput()
