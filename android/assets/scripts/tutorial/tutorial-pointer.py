# This is the tutorial script.
# Most icons used in this script are from the Tango icon library (http://tango.freedesktop.org/)
# Created by Toni Sagrista

from __future__ import division

from time import sleep

from gaia.cu9.ari.gaiaorbit.script import EventScriptingInterface


headerSize = 25
textSize = 13
twdelay = 0.01
arrowH = 7

gs = EventScriptingInterface()

version = gs.getVersionNumber()


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
Creates a typewriter effect where text appears one letter at a time.
The parameter twdelay indicates the time in seconds between each letter.
"""
def typewriter_px(id, text, x, y, width, height, r, g, b, a, twdelay):
    buffer = ""

    gs.displayMessageObject(id, "", x, y, r, g, b, a, textSize)
    for letter in text:
        buffer += letter
        gs.displayMessageObject(id, buffer, x, y, r, g, b, a, textSize)
        sleep(twdelay)

gs.preloadTextures("scripts/tutorial/arrow-left-s.png")



#
# POINT TO TUTORIAL
#
gs.maximizeInterfaceWindow()
gs.setGuiPosition(0, 1)

sleep(1.5)


# Modes
posize = gs.getPositionAndSizeGui("tutorial")
arrow_small(100, posize[0] + posize[2], posize[1] + posize[3] / 2 - arrowH)
w = gs.getScreenWidth()
h = gs.getScreenHeight()
typewriter_px(200, 'If you need help, you can run the tutorial by clicking on this button!', (posize[0] + posize[2] + 100) / w, (posize[1] + posize[3] / 2) / h, 0.6, 0.1, 1.0, 1.0, 1.0, 1.0, twdelay)
sleep(10.0)

# Restore input and interface
gs.removeAllObjects()

