# This script tests the displaying of custom messages and images.
# Created by Toni Sagrista

from time import sleep
from gaia.cu9.ari.gaiaorbit.script import EventScriptingInterface


gs = EventScriptingInterface()


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
Creates a typewriter effect where text appears one letter at a time.
The parameter delay indicates the time in seconds between each letter. 
"""
def typewriter(id, text, x, y, width, height, r, g, b, a, delay):
    buffer = ""
    gs.displayTextObject(id, "", x, y, width, height, r, g, b, a, textSize)
    for letter in text:
        buffer += letter
        gs.displayTextObject(id, buffer, x, y, width, height, r, g, b, a, textSize)
        sleep(delay)


# Minimize interface, disable input, stop camera
gs.disableInput()
gs.cameraStop()
gs.minimizeInterfaceWindow()

textSize = 13
text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean sollicitudin felis et metus cursus, ac placerat nisi laoreet. Aenean porttitor elit velit. Curabitur cursus diam non bibendum tincidunt. Maecenas laoreet pellentesque nunc, a sollicitudin ligula lacinia vitae.";
gs.displayMessageObject(1, "Position/size test", 0.1, 0.9, 1.0, 0.7, 0.0, 1.0, 22)
gs.displayTextObject(2, "1 -" + text, 0.1, 0.5, 0.9, 0.3, 1.0, 1.0, 1.0, 1.0, textSize)
gs.displayTextObject(3, "2 - " + text, 0.2, 0.1, 0.36, 0.3, 1.0, 1.0, 1.0, 1.0, textSize)
gs.displayTextObject(4, "3 - " + text, 0.6, 0.1, 0.36, 0.4, 1.0, 1.0, 1.0, 1.0, textSize)


wait_input(0, [1, 2, 3, 4])
text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean sollicitudin felis et metus cursus, ac placerat nisi laoreet. Aenean porttitor elit velit. Curabitur cursus diam non bibendum tincidunt. Maecenas laoreet pellentesque nunc, a sollicitudin ligula lacinia vitae. Quisque eu risus est. Nulla ut risus volutpat, fermentum risus sit amet, blandit nulla. Curabitur venenatis risus sed blandit euismod. Quisque suscipit, felis quis egestas malesuada, enim orci mattis erat, a luctus felis massa eu tellus. Sed interdum pharetra gravida. Nulla lacinia malesuada neque ut cursus.";
gs.displayMessageObject(1, "Typewriter test", 0.1, 0.9, 1.0, 0.7, 0.0, 1.0, 22)
typewriter(2, text, 0.1, 0.4, 0.8, 0.25, 1.0, 1.0, 1.0, 1.0, 0.01)

wait_input(0, [1, 2])

gs.enableInput()
gs.maximizeInterfaceWindow()
