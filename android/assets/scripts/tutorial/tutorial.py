# This is the tutorial script.
# Most icons used in this script are from the Tango icon library (http://tango.freedesktop.org/)
# Created by Toni Sagrista

from __future__ import division

from time import sleep

from sandbox.script import EventScriptingInterface


headerSize = 25
textSize = 13
twdelay = 0.007
arrowH = 7

gs = EventScriptingInterface()

version = gs.getVersionNumber()

"""
Prints a notice on the screen and waits for any input.
y - y coordinate of the notice in [0..1], from bottom to top
idsToRemove - List with the ids to remove when input is received.
"""
def wait_input(y, idsToRemove):
    waitid = 3634

    gs.displayMessageObject(waitid, "Press any key to continue...", 0.6, y, 0.9, 0.9, 0.0, 1.0, 15)
    gs.waitForInput()
    gs.removeObjects(idsToRemove)
    gs.removeObject(waitid)


""" 
Adds arrow to screen.
id - The id of the arrow.
x - x coordinate of bottom-left corner in pixels, from left to right.
y - y coordinate of bottom-left corner in pixels, from bottom to top.
"""
def arrow(id, x, y):
    w = gs.getScreenWidth()
    h = gs.getScreenHeight()

    gs.displayImageObject(id, "scripts/tutorial/arrow-left.png", x / w, y / h, 1.0, 1.0, 0.0, 1.0)


""" 
Adds a small arrow to screen.
id - The id of the arrow.
x - x coordinate of bottom-left corner in pixels, from left to right.
y - y coordinate of bottom-left corner in pixels, from bottom to top.
args - optional, red, green, blue and alpha components of color.
"""
def arrow_small(id, x, y, *args):
    w = gs.getScreenWidth()
    h = gs.getScreenHeight()

    if(len(args) > 0):
        gs.displayImageObject(id, "scripts/tutorial/arrow-left-s.png", x / w, y / h, args[0], args[1], args[2], args[3])
    else:
        gs.displayImageObject(id, "scripts/tutorial/arrow-left-s.png", x / w, y / h, 1.0, 1.0, 0.0, 1.0)

"""
Adds a message in the given position and with the given color
x - x coordinate of bottom-left corner in pixels, from left to right.
y - y coordinate of bottom-left corner in pixels, from bottom to top.
"""
def message(id, msg, x, y, color, size):
    w = gs.getScreenWidth()
    h = gs.getScreenHeight()

    gs.displayMessageObject(id, msg, x / w, y / h, color[0], color[1], color[2], color[3], size)


"""
Creates a typewriter effect where text appears one letter at a time.
The parameter twdelay indicates the time in seconds between each letter. 
"""
def typewriter(id, text, x, y, width, height, r, g, b, a, twdelay):
    buffer = ""
    gs.displayTextObject(id, "", x, y, width, height, r, g, b, a, textSize)
    for letter in text:
        buffer += letter
        gs.displayTextObject(id, buffer, x, y, width, height, r, g, b, a, textSize)
        sleep(twdelay)

gs.preloadTextures("scripts/tutorial/arrow-left-s.png", "scripts/tutorial/gaia.png", "scripts/tutorial/clock.png", "scripts/tutorial/camera.png", "scripts/tutorial/visibility.png", "scripts/tutorial/light.png", "scripts/tutorial/preferences.png")

# Disable input and prepare
gs.disableInput()
gs.cameraStop()
gs.stopSimulationTime()
gs.setFov(50.0)
gs.minimizeInterfaceWindow()
gs.setGuiPosition(0, 1)
gs.goToObject("Earth")

#
# WELCOME
#
gs.displayMessageObject(0, "Gaia Sandbox tutorial", 0.3, 0.9, 1.0, 0.7, 0.0, 1.0, headerSize)
sleep(1.0)
typewriter(1, "Welcome! In this tutorial you will learn the basic functionality and interface of this application in an interactive mode.\nHang on, it is starting...", 0.3, 0.75, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
sleep(1.0)
wait_input(0.75, [0, 1])
sleep(1.0)

#
# GENERAL NAVIGATION
#
gs.displayImageObject(0, "scripts/tutorial/gaia.png", 0.25, 0.88)
gs.displayMessageObject(1, "General navigation", 0.33, 0.9, 1.0, 0.7, 0.0, 1.0, headerSize)
sleep(1.0)

typewriter(2, 'The Gaia Sandbox (' + version + ') is an interactive 3D visualisation application that permits the real time exploration of the galaxy in space and time.', 0.3, 0.75, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
wait_input(0.75, [])
typewriter(3, 'You can move around using the mouse and keyboard (or touch screen if applicable), clicking and dragging the LEFT MOUSE button to rotate the scene and using the SCROLL WHEEL to zoom in and out.', 0.3, 0.7, 0.6, 0.08, 1.0, 1.0, 1.0, 1.0, twdelay)
wait_input(0.65, [2, 3])
sleep(1.0)
gs.setRotationCameraSpeed(20.0)
typewriter(2, 'For example, right...', 0.3, 0.75, 0.6, 0.08, 1.0, 1.0, 1.0, 1.0, twdelay)
gs.cameraRotate(-0.1, 0)
sleep(3.0)
gs.cameraStop()
sleep(1.0)

typewriter(3, 'And left', 0.3, 0.73, 0.6, 0.08, 1.0, 1.0, 1.0, 1.0, twdelay)
gs.cameraRotate(0.1, 0)
sleep(3.0)
gs.cameraStop()
sleep(1.0)

typewriter(4, 'Up...', 0.3, 0.71, 0.6, 0.08, 1.0, 1.0, 1.0, 1.0, twdelay)
gs.cameraRotate(0, -0.1)
sleep(3.0)
gs.cameraStop()
sleep(1.0)

typewriter(5, 'And down', 0.3, 0.69, 0.6, 0.08, 1.0, 1.0, 1.0, 1.0, twdelay)
gs.cameraRotate(0, 0.1)
sleep(3.0)
gs.cameraStop()
sleep(1.0)

gs.removeObjects([2, 3, 4, 5])

gs.setCameraSpeed(20.0)
typewriter(2, 'We can also move away from the Earth...', 0.3, 0.75, 0.6, 0.08, 1.0, 1.0, 1.0, 1.0, twdelay)
gs.goToObject("Earth", 800000)
gs.cameraStop()
sleep(1.0)

typewriter(3, 'Or zoom back in', 0.3, 0.73, 0.6, 0.08, 1.0, 1.0, 1.0, 1.0, twdelay)
gs.goToObject("Earth")
gs.cameraStop()
sleep(1.0)

wait_input(0.69, [2, 3])

typewriter(2, 'We can roll the camera by holding LEFT SHIFT and dragging the LEFT MOUSE button', 0.3, 0.75, 0.6, 0.08, 1.0, 1.0, 1.0, 1.0, twdelay)
sleep(1.0)
typewriter(3, 'Like this', 0.3, 0.71, 0.6, 0.08, 1.0, 1.0, 1.0, 1.0, twdelay)
gs.cameraRoll(0.2)
sleep(2.5)
gs.cameraStop()
sleep(1.0)
gs.cameraRoll(-0.2)
sleep(2.5)
gs.cameraStop()

wait_input(0.69, [2, 3])

gs.setTurningCameraSpeed(20)
typewriter(2, 'We can also look away from the focus by holding LEFT CONTROL and dragging the LEFT MOUSE button', 0.3, 0.75, 0.6, 0.08, 1.0, 1.0, 1.0, 1.0, twdelay)
sleep(1.0)
typewriter(3, 'Like this', 0.3, 0.69, 0.6, 0.08, 1.0, 1.0, 1.0, 1.0, twdelay)
gs.cameraTurn(5.0, 0)
sleep(2.5)
gs.cameraStop()
gs.goToObject("Earth", 100)
sleep(3.0)
gs.cameraCenter()
gs.goToObject("Earth")
gs.cameraStop()

wait_input(0.69, [2, 3])

typewriter(2, 'Finally, to select a different focus, just DOUBLE CLICK on it', 0.3, 0.75, 0.6, 0.08, 1.0, 1.0, 1.0, 1.0, twdelay)
sleep(1.0)
typewriter(3, 'Let\'s change the focus a couple of times...', 0.3, 0.69, 0.6, 0.08, 1.0, 1.0, 1.0, 1.0, twdelay)
sleep(2.5)
gs.setCameraFocus("Betelgeuse")
sleep(2.5)
gs.setCameraFocus("Mars")
sleep(2.5)
gs.setCameraFocus("Sol")
sleep(2.5)
gs.setCameraFocus("Earth")
sleep(2.5)
gs.cameraStop()
gs.cameraCenter()

wait_input(0.69, [2, 3])

typewriter(2, 'Let us now take a look at the time simulation', 0.3, 0.75, 0.6, 0.08, 1.0, 1.0, 1.0, 1.0, twdelay)

wait_input(0.69, [0, 1, 2])

#
# TIME
#
gs.maximizeInterfaceWindow()
gs.setGuiPosition(0, 1)
gs.setGuiScrollPosition(0)

gs.displayImageObject(0, "scripts/tutorial/clock.png", 0.25, 0.88)
gs.displayMessageObject(1, "Time simulation", 0.33, 0.9, 1.0, 0.7, 0.0, 1.0, headerSize)
sleep(1.0)

# Play/pause
posize = gs.getPositionAndSizeGui("play stop")
arrow_small(100, posize[0] + posize[2], posize[1] + posize[3] / 2 - arrowH)
typewriter(2, 'The time can be paused and resumed using the PLAY/PAUSE button next to the title.\nIt also indicates whether the time is currently activated or not.', 0.3, 0.75, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
sleep(1.0)
wait_input(0.75, [100, 2])
sleep(1.0)

# Pace
posize = gs.getPositionAndSizeGui("plus")
arrow_small(100, posize[0] + posize[2], posize[1] + posize[3] / 2 - arrowH)
typewriter(2, "The speed of the simulation time is governed by the pace (shown in the text field 'pace'). You can modify the pace either entering a number in the field or by using the + and - buttons. You can also use the shortcuts '[' and ']'.", 0.3, 0.7, 0.6, 0.15, 1.0, 1.0, 1.0, 1.0, twdelay)
sleep(1.0)
wait_input(0.7, [100, 2])

# Date
posize = gs.getPositionAndSizeGui("input time")
arrow_small(100, posize[0] + posize[2], posize[1] + posize[3] / 2 - arrowH)
typewriter(2, "The current date is displayed in the date field marked by the arrow. You can toggle the time by pressing SPACE", 0.3, 0.75, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
sleep(1.0)
wait_input(0.75, [100, 2])

# Demo
gs.goToObject("Earth")
typewriter(2, "Let's test it. We'll start the time simulation now.", 0.3, 0.75, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
sleep(1.0)
wait_input(0.75, [2])
gs.setSimulationPace(1)
gs.startSimulationTime()
arrow_small(100, posize[0] + posize[2], posize[1] + posize[3] / 2 - arrowH, 1.0, 0.0, 0.0, 1.0)
typewriter(2, "The time is running, check the red arrow!", 0.3, 0.75, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
sleep(5.0)
gs.stopSimulationTime()
gs.removeObject(2)
typewriter(2, "Time stopped!", 0.3, 0.75, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
sleep(1.0)
typewriter(3, "We can also run the time backwards. Let's set a negative pace", 0.3, 0.73, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
posize = gs.getPositionAndSizeGui("input pace")
typewriter(4, "Check the pace value as we set it to -1 and start the simulation again", 0.3, 0.71, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
arrow_small(101, posize[0] + posize[2], posize[1] + posize[3] / 2 - arrowH)
sleep(1.0)
gs.setSimulationPace(-1)
wait_input(0.75, [100, 2])
gs.startSimulationTime()
sleep(4.0)
# Restore pace
gs.setSimulationPace(0.1)
gs.stopSimulationTime()

wait_input(0.75, [2, 3, 4, 100, 101])

typewriter(2, "Let's now find out about the camera modes", 0.3, 0.75, 0.6, 0.08, 1.0, 1.0, 1.0, 1.0, twdelay)

wait_input(0.75, [0, 1, 2])


#
# CAMERA
#
gs.maximizeInterfaceWindow()
gs.setGuiPosition(0, 1)
gs.setGuiScrollPosition(0)

gs.displayImageObject(0, "scripts/tutorial/camera.png", 0.25, 0.88)
gs.displayMessageObject(1, "Camera", 0.33, 0.9, 1.0, 0.7, 0.0, 1.0, headerSize)
sleep(1.0)

# Modes
posize = gs.getPositionAndSizeGui("camera mode")
arrow_small(100, posize[0] + posize[2], posize[1] + posize[3] / 2 - arrowH)
typewriter(2, 'There are 3 camera modes:\n -Focus camera\n -Free camera\n -Field of view', 0.3, 0.75, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
typewriter(3, 'You can select the camera mode using the select box or with the keys 0-4 in the numeric keypad', 0.3, 0.65, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
sleep(1.0)
wait_input(0.65, [100, 2, 3])

typewriter(2, 'You can use the sliders to change the camera field of view', 0.3, 0.75, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
posize = gs.getPositionAndSizeGui("field of view")
arrow_small(100, posize[0] + posize[2], posize[1] + posize[3] / 2 - arrowH)
sleep(2.0)

typewriter(3, 'The camera speed', 0.3, 0.73, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
posize = gs.getPositionAndSizeGui("camera speed")
arrow_small(101, posize[0] + posize[2], posize[1] + posize[3] / 2 - arrowH)
sleep(2.0)

typewriter(4, 'The camera rotation speed', 0.3, 0.71, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
posize = gs.getPositionAndSizeGui("rotate speed")
arrow_small(102, posize[0] + posize[2], posize[1] + posize[3] / 2 - arrowH)
sleep(2.0)

typewriter(5, 'And the camera turning speed', 0.3, 0.69, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
posize = gs.getPositionAndSizeGui("turn speed")
arrow_small(103, posize[0] + posize[2], posize[1] + posize[3] / 2 - arrowH)
sleep(2.0)

wait_input(0.69, [100, 101, 102, 103, 2, 3, 4, 5])
sleep(1.0)

typewriter(2, 'You can also lock the camera to the focus. This means that the motion of the camera is locked to that of the object, so that it follows it around everywhere', 0.3, 0.75, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
posize = gs.getPositionAndSizeGui("focus lock")
arrow_small(100, posize[0] + posize[2], posize[1] + posize[3] / 2 - arrowH)
sleep(1.0)
wait_input(0.69, [100, 2])
sleep(1.0)

typewriter(2, 'You can play with that later, let\'s now see the objects pane', 0.3, 0.75, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
sleep(1.0)
wait_input(0.75, [0, 1, 2])


#
# OBJECTS
#
gs.maximizeInterfaceWindow()
gs.setGuiPosition(0, 1)
gs.setGuiScrollPosition(500)
sleep(1.0)

gs.displayImageObject(0, "scripts/tutorial/globe.png", 0.25, 0.88)
gs.displayMessageObject(1, "Objects pane", 0.33, 0.9, 1.0, 0.7, 0.0, 1.0, headerSize)
sleep(1.0)

posize = gs.getPositionAndSizeGui("objects list scroll")
arrow_small(100, posize[0] + posize[2], posize[1] + posize[3] / 2 - arrowH)
typewriter(2, 'You can select the current focus using the objects list.\nHowever, it only contains objects with proper names.', 0.3, 0.75, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
typewriter(3, 'You can also select a focus by DOUBLE CLICKING on it.', 0.3, 0.65, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
sleep(1.0)
wait_input(0.65, [100, 2, 3])

posize = gs.getPositionAndSizeGui("search box")
arrow_small(100, posize[0] + posize[2], posize[1] + posize[3] / 2 - arrowH)
typewriter(2, 'Finally, you can use the search box highlighted by the arrow to search for any object by name. You can also get a search dialog with the shortcut CTRL+F.', 0.3, 0.75, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)

gs.enableInput()
doneko = True

while doneko:
    gs.removeObject(4)
    typewriter(3, 'Try it now, search for \'Betelgeuse\'', 0.3, 0.69, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
    doneko = gs.waitFocus("Betelgeuse", 18000)
    if doneko:
        typewriter(4, 'Cmon, you can do it! You just needed to press CTRL+F and type Betelgeuse. Let\'s try again...', 0.3, 0.65, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
        sleep(3.0)

gs.removeObjects([100, 3, 4])
gs.disableInput()
sleep(3.0)
typewriter(3, 'Well done! Lets move forward', 0.3, 0.69, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
sleep(1.0)
gs.setCameraFocus("Earth")
wait_input(0.65, [100, 0, 1, 2, 3])
gs.removeAllObjects()

#
# VISIBILITY
#
gs.maximizeInterfaceWindow()
gs.setGuiPosition(0, 1)
gs.setGuiScrollPosition(500)
sleep(1.0)

gs.displayImageObject(0, "scripts/tutorial/visibility.png", 0.25, 0.88)
gs.displayMessageObject(1, "Object visibility", 0.33, 0.9, 1.0, 0.7, 0.0, 1.0, headerSize)
sleep(1.0)

posize = gs.getPositionAndSizeGui("visibility table")
arrow_small(100, posize[0] + posize[2], posize[1] + posize[3] / 2 - arrowH)
typewriter(2, 'You can toggle the visibility of object types on and off using these buttons by the yellow arrow', 0.3, 0.75, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
typewriter(3, 'For example, we can switch off planets...', 0.3, 0.69, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
sleep(1.0)
gs.setVisibility("Planets", False)
sleep(1.0)
typewriter(4, 'And stars too', 0.3, 0.67, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
sleep(1.0)
gs.setVisibility("Stars", False)
sleep(1.0)
wait_input(0.65, [3, 4])
sleep(1.0)
typewriter(3, 'And we can re-enable them again', 0.3, 0.69, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
sleep(1.0)
gs.setVisibility("Planets", True)
gs.setVisibility("Stars", True)
sleep(1.0)
wait_input(0.65, [2, 3])
sleep(1.0)
typewriter(2, 'The visibility of elements can also be toggled using keyboard shortcuts. For exmaple\nP - Planets\nO - Orbits\nC - Constellations\nS - Stars\netc.', 0.3, 0.71, 0.6, 0.15, 1.0, 1.0, 1.0, 1.0, twdelay)
wait_input(0.65, [100, 0, 1, 2])

gs.removeAllObjects()

#
# LIGHTING
#
gs.maximizeInterfaceWindow()
gs.setGuiPosition(0, 1)
gs.setGuiScrollPosition(500)
sleep(1.0)

gs.displayImageObject(0, "scripts/tutorial/light.png", 0.25, 0.88)
gs.displayMessageObject(1, "Scene lighting", 0.33, 0.9, 1.0, 0.7, 0.0, 1.0, headerSize)
sleep(1.0)

typewriter(2, 'We are getting to the end', 0.3, 0.75, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
sleep(1.0)
posize = gs.getPositionAndSizeGui("star brightness")
arrow_small(100, posize[0] + posize[2], posize[1] + posize[3] / 2 - arrowH)
posize = gs.getPositionAndSizeGui("ambient light")
arrow_small(101, posize[0] + posize[2], posize[1] + posize[3] / 2 - arrowH)
posize = gs.getPositionAndSizeGui("bloom effect")
arrow_small(102, posize[0] + posize[2], posize[1] + posize[3] / 2 - arrowH)

typewriter(3, 'We can adjust some lighting parameters such as:', 0.3, 0.69, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
sleep(1.0)
typewriter(4, 'The star brightness', 0.3, 0.67, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
posize = gs.getPositionAndSizeGui("star brightness")
arrow_small(100, posize[0] + posize[2], posize[1] + posize[3] / 2 - arrowH, 1.0, 0.0, 0.0, 1.0)
sleep(2.0)
typewriter(5, 'The ambient light (affects the shadowed parts of planets and bodies)', 0.3, 0.65, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
posize = gs.getPositionAndSizeGui("star brightness")
arrow_small(100, posize[0] + posize[2], posize[1] + posize[3] / 2 - arrowH)
posize = gs.getPositionAndSizeGui("ambient light")
arrow_small(101, posize[0] + posize[2], posize[1] + posize[3] / 2 - arrowH, 1.0, 0.0, 0.0, 1.0)
sleep(2.0)
typewriter(6, 'The bloom post-processing effect', 0.3, 0.63, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
posize = gs.getPositionAndSizeGui("ambient light")
arrow_small(101, posize[0] + posize[2], posize[1] + posize[3] / 2 - arrowH)
posize = gs.getPositionAndSizeGui("bloom effect")
arrow_small(102, posize[0] + posize[2], posize[1] + posize[3] / 2 - arrowH, 1.0, 0.0, 0.0, 1.0)
sleep(2.0)
wait_input(0.63, [100, 101, 102, 0, 1, 2, 3, 4, 5, 6])

gs.removeAllObjects()

#
# GAIA
#
gs.maximizeInterfaceWindow()
gs.setGuiPosition(0, 1)
gs.setGuiScrollPosition(500)
sleep(1.0)

gs.goToObject("Gaia")
gs.displayImageObject(0, "scripts/tutorial/gaia.png", 0.25, 0.88)
gs.displayMessageObject(1, "Gaia scan options", 0.33, 0.9, 1.0, 0.7, 0.0, 1.0, headerSize)
sleep(1.0)

typewriter(2, 'Finally, there are three controls for managing the simulation of the Gaia sky scan', 0.3, 0.75, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
sleep(1.0)
wait_input(0.69, [2])
sleep(1.0)

posize = gs.getPositionAndSizeGui("compute gaia scan")
arrow_small(100, posize[0] + posize[2], posize[1] + posize[3] / 2 - arrowH)
typewriter(2, 'First, the \'Enable Gaia scan\' checkbox enables the computation of the scanned stars in real time', 0.3, 0.75, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
sleep(1.0)
wait_input(0.69, [100, 2])
sleep(1.0)

posize = gs.getPositionAndSizeGui("transit color")
arrow_small(100, posize[0] + posize[2], posize[1] + posize[3] / 2 - arrowH)
typewriter(2, 'Then, you can enable the coloring of stars depending on how many times they have been observed', 0.3, 0.75, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
sleep(1.0)
wait_input(0.69, [100, 2])
sleep(1.0)

posize = gs.getPositionAndSizeGui("only observed stars")
arrow_small(100, posize[0] + posize[2], posize[1] + posize[3] / 2 - arrowH)
typewriter(2, 'And finally, this checkbox prevents the drawing of stars that have not been observed', 0.3, 0.75, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
sleep(1.0)
wait_input(0.69, [100, 2])
sleep(1.0)

typewriter(2, 'Remember to enable the simulation time so that Gaia actually moves when trying this out!', 0.3, 0.75, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
sleep(1.0)
wait_input(0.69, [100, 0, 1, 2])
sleep(1.0)

#
# FINAL REMARKS
#
gs.minimizeInterfaceWindow()
gs.setGuiPosition(0, 1)
gs.goToObject("Earth")
sleep(1.0)

gs.displayImageObject(0, "scripts/tutorial/preferences.png", 0.25, 0.88)
gs.displayMessageObject(1, "Just one more thing...", 0.33, 0.9, 1.0, 0.7, 0.0, 1.0, headerSize)
sleep(1.0)

typewriter(2, 'If you need more detailed information on this software and/or how to perform advanced tasks (such scripting, image outputting, etc.) you can read the online documentation in our GitHub account. Also, feel free to send us any bug reports or suggestions. We are always happy to recieve them.\nNow, finally, enjoy the application!', 0.3, 0.69, 0.6, 0.15, 1.0, 1.0, 1.0, 1.0, twdelay)
sleep(1.0)
gs.displayMessageObject(90, "Press any key to finish", 0.6, 0.65, 0.9, 0.9, 0.0, 1.0, 15)
gs.waitForInput()



# Restore input and interface
gs.removeAllObjects()
gs.enableInput()
gs.maximizeInterfaceWindow()

