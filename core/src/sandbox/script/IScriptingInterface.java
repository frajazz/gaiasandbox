package sandbox.script;

/**
 * Scripting interface. Provides an interface to the Gaia Sandbox
 * core and exposes all the methods that are callable from a script
 * in order to interact with the program (create demonstrations,
 * tutorials, load data, etc.). You should never use any 
 * integration other than this interface for scripting.
 * @author Toni Sagrista
 *
 */
public interface IScriptingInterface {

    /**
     * Pre-loads the given images as textures for later use. They will be cached so that they do not 
     * need to be loaded in the next use. 
     * @param paths The texture paths.
     */
    public void preloadTextures(String... paths);

    /**
     * Sets the current time frame to <b>real time,</b>. All the
     * commands executed after this command becomes active will be in
     * the <b>real time</b> frame (clock ticks). 
     */
    public void activateRealTimeFrame();

    /**
     * Sets the current time frame to <b>simulation time,</b>. All the
     * commands executed after this command becomes active will be in
     * the <b>simulation time</b> frame (simulation clock in the app). 
     */
    public void activateSimulationTimeFrame();

    /**
     * Sets a headline message that will appear in a big font in the
     * screen.
     * @param headline The headline text.
     */
    public void setHeadlineMessage(String headline);

    /**
     * Sets a subhead message that will appear in a small font 
     * below the headline.
     * @param subhead The subhead text.
     */
    public void setSubheadMessage(String subhead);

    /**
     * Clears the headline messge.
     */
    public void clearHeadlineMessage();

    /**
     * Clears the subhead message
     */
    public void clearSubheadMessage();

    /**
     * Clears both the subhead and the headline messages.
     */
    public void clearAllMessages();

    /**
     * Adds a new one-line message in the screen with the given id and the given coordinates. If an object 
     * already exists with the given id, it is removed. However, if a message object already exists 
     * with the same id, its properties are updated.
     * @param id A unique identifier, used to identify this message when you want to remove it.
     * @param message The string message, to be displayed in one line.
     * @param x The x coordinate of the bottom-left corner, in [0..1] from left to right. This is not resolution-dependant.
     * @param y The y coordinate of the bottom-left corner, in [0..1] from bottom to top. This is not resolution-dependant.
     * @param r The red component of the color in [0..1].
     * @param g The green component of the color in [0..1].
     * @param b The blue component of the color in [0..1].
     * @param a The alpha component of the color in [0..1].
     * @param fontSize The size of the font. The system will use the existing font closest to the chosen size.
     */
    public void displayMessageObject(int id, String message, float x, float y, float r, float g, float b, float a, float fontSize);

    /**
     * Adds a new multi-line text in the screen with the given id, coordinates and size. If an object 
     * already exists with the given id, it is removed. However, if a text object already exists 
     * with the same id, its properties are updated.
     * @param id A unique identifier, used to identify this message when you want to remove it.
     * @param message The string message, to be displayed in one line.
     * @param x The x coordinate of the bottom-left corner, in [0..1] from left to right. This is not resolution-dependant.
     * @param y The y coordinate of the bottom-left corner, in [0..1] from bottom to top. This is not resolution-dependant.
     * @param maxWidth The maximum width in screen percentage [0..1]. Set to 0 to let the system decide.
     * @param maxHeight The maximum height in screen percentage [0..1]. Set to 0 to let the system decide.
     * @param r The red component of the color in [0..1].
     * @param g The green component of the color in [0..1].
     * @param b The blue component of the color in [0..1].
     * @param a The alpha component of the color in [0..1].
     * @param fontSize The size of the font. The system will use the existing font closest to the chosen size.
     */
    public void displayTextObject(int id, String text, float x, float y, float maxWidth, float maxHeight, float r, float g, float b, float a, float fontSize);

    /**
     * Adds a new image object at the given coordinates. If an object 
     * already exists with the given id, it is removed. However, if an image object already exists 
     * with the same id, its properties are updated.<br>
     * <strong>Warning: This method will only work in the asynchronous mode. 
     * Run the script with the "asynchronous" check box activated!</strong>
     * @param id A unique identifier, used to identify this message when you want to remove it.
     * @param path The path to the image. It can either be an absolute path (not recommended) or a path relative to the Gaia Sandbox folder.
     * @param x The x coordinate of the bottom-left corner, in [0..1] from left to right. This is not resolution-dependant.
     * @param y The y coordinate of the bottom-left corner, in [0..1] from bottom to top. This is not resolution-dependant.
     */
    public void displayImageObject(int id, String path, float x, float y);

    /**
     * Adds a new image object at the given coordinates. If an object 
     * already exists with the given id, it is removed. However, if an image object already exists 
     * with the same id, its properties are updated.<br>
     * <strong>Warning: This method will only work in the asynchronous mode. 
     * Run the script with the "asynchronous" check box activated!</strong>
     * @param id A unique identifier, used to identify this message when you want to remove it.
     * @param path The path to the image. It can either be an absolute path (not recommended) or a path relative to the Gaia Sandbox folder.
     * @param x The x coordinate of the bottom-left corner, in [0..1] from left to right. This is not resolution-dependant.
     * @param y The y coordinate of the bottom-left corner, in [0..1] from bottom to top. This is not resolution-dependant.
     * @param r The red component of the color in [0..1].
     * @param g The green component of the color in [0..1].
     * @param b The blue component of the color in [0..1].
     * @param a The alpha component of the color in [0..1].
     */
    public void displayImageObject(int id, final String path, float x, float y, float r, float g, float b, float a);

    /**
     * Removes all objects.
     */
    public void removeAllObjects();

    /**
     * Removes the item with the given id.
     * @param ids Integer with the integer id of the object to remove.
     */
    public void removeObject(int id);

    /**
     * Removes the items with the given ids. They can either messages, images or whatever else.
     * @param ids Vector with the integer ids of the objects to remove.
     */
    public void removeObjects(int[] ids);

    /**
     * Disables all input events from mouse, keyboard, touchscreen, etc.
     */
    public void disableInput();

    /**
     * Enables all input events.
     */
    public void enableInput();

    /**
     * Sets the camera in focus mode with the focus object that bears the given name.
     * @param focusName The name of the new focus object.
     */
    public void setCameraFocus(String focusName);

    /**
     * Activates or deactivates the camera lock to the focus reference
     * system when in focus mode.
     * @param lock Activate or deactivate the lock.
     */
    public void setCameraLock(boolean lock);

    /**
     * Sets the camera in free mode.
     */
    public void setCameraFree();

    /**
     * Sets the camera in FoV1 mode. The camera is positioned in Gaia's focal plane and
     * observes what Gaia observes through its field of view 1.
     */
    public void setCameraFov1();

    /**
     * Sets the camera in FoV2 mode. The camera is positioned in Gaia's focal plane and
     * observes what Gaia observes through its field of view 2.
     */
    public void setCameraFov2();

    /**
     * Sets the camera in Fov1 and 2 mode. The camera is positioned in Gaia's focal plane and
     * observes what Gaia observes through its two fields of view.
     */
    public void setCameraFov1and2();

    /**
     * Sets the camera position to the given coordinates, in Km, equatorial system.
     * @param vec Vector of three components in equatorial coordinates and km.
     */
    public void setCameraPostion(double[] vec);

    /**
     * Sets the camera direction vector to the given vector, equatorial system.
     * @param dir The direction vector in equatorial coordinates.
     */
    public void setCameraDirection(double[] dir);

    /**
     * Sets the camera up vector to the given vector, equatorial system.
     * @param up The up vector in equatorial coordinates.
     */
    public void setCameraUp(double[] up);

    /**
     * Changes the speed of the camera and its acceleration.
     * @param speed The new speed, from 1 to 100.
     */
    public void setCameraSpeed(float speed);

    /**
     * Changes the speed of the camera when it rotates around a focus.
     * @param speed The new rotation speed, from 1 to 100.
     */
    public void setRotationCameraSpeed(float speed);

    /**
     * Changes the turning speed of the camera.
     * @param speed The new turning speed, from 1 to 100.
     */
    public void setTurningCameraSpeed(float speed);

    /**com.badlogic.gdx.graphics.g2d.BitmapFont: { ui-10: { file: ui-10.fnt } },
    com.badlogic.gdx.graphics.g2d.BitmapFont: { ui-11: { file: ui-11.fnt } },
    com.badlogic.gdx.graphics.g2d.BitmapFont: { ui-12: { file: ui-12.fnt } },
    com.badlogic.gdx.graphics.g2d.BitmapFont: { ui-13: { file: ui-13.fnt } },
    com.badlogic.gdx.graphics.g2d.BitmapFont: { ui-15: { file: ui-15.fnt } },
    com.badlogic.gdx.graphics.g2d.BitmapFont: { ui-23: { file: ui-23.fnt } },
     * Adds a forward movement to the camera with the given value. If value is 
     * negative the movement is backwards.
     * @param value The magnitude of the movement, between 0 and 1.
     */
    public void cameraForward(double value);

    /**
     * Adds a rotation movement to the camera, or a pitch/yaw if in free mode.
     * @param deltaX The x component, between 0 and 1. Positive is right and negative is left.
     * @param deltaY The y component, between 0 and 1. Positive is up and negative is down.
     */
    public void cameraRotate(double deltaX, double deltaY);

    /**
     * Adds a roll force to the camera.
     * @param roll The intensity of the roll.
     */
    public void cameraRoll(double roll);

    /**
     * Adds a turn force to the camera. If the camera is in focus mode, it permanently deviates
     * the line of sight from the focus until centered again.
     * @param deltaX The x component, between 0 and 1. Positive is right and negative is left.
     * @param deltaY The y component, between 0 and 1. Positive is up and negative is down.
     */
    public void cameraTurn(double deltaX, double deltaY);

    /**
     * Stops all camera motion.
     */
    public void cameraStop();

    /**
     * Centers the camera to the focus, removing any deviation of the line of sight. Useful to center the focus
     * object again after turning.
     */
    public void cameraCenter();

    /**
     * Changes the field of view of the camera.
     * @param newFov The new field of view value in degrees, between 20 and 160.
     */
    public void setFov(float newFov);

    /**
     * Sets the component described by the given name visible or invisible.
     * @param name The name of the component (Stars, Planets, Moons, etc.)
     * @param visible The visible value.
     */
    public void setVisibility(String name, boolean visible);

    /**
     * Sets the ambient light to a certain value.
     * @param value The value of the ambient light, between 0 and 100.
     */
    public void setAmbientLight(float value);

    /**
     * Sets the time of the application. The long value represents specified 
     * number of milliseconds since the standard base time known as "the epoch",
     * namely January 1, 1970, 00:00:00 GMT.
     * @param value Number of milliseconds since the epoch (Jan 1, 1970)
     */
    public void setSimulationTime(long time);

    /**
     * Starts the simulation.
     */
    public void startSimulationTime();

    /**
     * Stops the simulation time.
     */
    public void stopSimulationTime();

    /**
     * Changes the pace of time.
     * @param pace The pace in number of simulation hours per real time second.
     */
    public void setSimulationPace(double pace);

    /**
     * Sets the star brightness value.
     * @param brightness The brightness value, between 0 and 100.
     */
    public void setStarBrightness(float brightness);

    /**
     * Configures the frame outputting system, setting the resolution of the images,
     * the target frames per second, the output folder and the image name prefix.
     * @param width Width of images.
     * @param height Height of images.
     * @param fps Target frames per second (number of images per second).
     * @param folder The output folder path.
     * @param namePrefix The file name prefix.
     
     */
    public void configureRenderOutput(int width, int height, int fps, String folder, String namePrefix);

    /**
     * Activates or deactivates the image output system. If called with true,
     * the system starts outputting images right away.
     * @param active Whether to activate or deactivate the frame output system.
     
     */
    public void setFrameOutput(boolean active);

    /**
     * Runs a seamless trip to the object with the given name, until it
     * fills the viewport. <strong>Warning> This will only work in asynchronous mode.</strong>
     * @param name The name of the object.
     */
    public void goToObject(String name);

    /**
     * Runs a seamless trip to the object with the given name until the camera is at the given distance.
     * If distance is negative, the default distance is six times the radius of the object.
     * <strong>Warning> This will only work in asynchronous mode.</strong>
     * @param name The name of the object.
     * @param distance The distance in km.
     */
    public void goToObject(String name, double distance);

    /**
     * Returns the distance to the surface of the object identified with the given name.
     * If the object is an abstract node or does not exist, it returns a negative distance.
     * @param objectName The name of the object.
     * @return The distance to the object in km if it exists, a negative value otherwise.
     */
    public double getDistanceTo(String objectName);

    /**
     * Sets the vertical scroll position in the GUI.
     * @param pixelY The pixel to set the scroll position to.
     */
    public void setGuiScrollPosition(float pixelY);

    /**
     * Maximizes the interface window.
     */
    public void maximizeInterfaceWindow();

    /**
     * Minimizes the interface window.
     */
    public void minimizeInterfaceWindow();

    /**
     * Moves the interface window to a new position.
     * @param x The new x coordinate of the new top-left corner of the window, in [0..1] from left to right.
     * @param y The new y coordinate of the new top-left corner of the window, in [0..1] from bottom to top.
     * @param delayMs
     */
    public void setGuiPosition(float x, float y);

    /**
     * Blocks the execution until any kind of input (keyboard, mouse, etc.) is received.
     */
    public void waitForInput();

    /**
     * Blocks the execution until the Enter key is pressed.
     */
    public void waitForEnter();

    /**
     * Blocks the execution until the given key or button is pressed.
     * @param code The key or button code. Please see {@link com.badlogic.gdx.Input}.
     */
    public void waitForInput(int code);

    /**
     * Returns the screen width in pixels.
     * @return The screen width in pixels.
     */
    public int getScreenWidth();

    /**
     * Returns the screen height in pixels.
     * @return The screen height in pixels.
     */
    public int getScreenHeight();

    /**
     * Returns the size and position of the GUI element that goes by the given name
     * or null if such element does not exist.
     * <strong>Warning> This will only work in asynchronous mode.</strong>
     * @param name The name of the gui element.
     * @return A vector of floats with the position (0, 1) of the bottom left corner in pixels from the bottom-left of the
     * screen and the size (2, 3) in pixels of the element.
     */
    public float[] getPositionAndSizeGui(String name);

    /**
     * Returns the version number string.
     * @return The version number string.
     */
    public String getVersionNumber();

    /**
     * Blocks the script until the focus is the object indicated by the name. There is an optional time out.
     * @param name The name of the focus to wait for
     * @param timeoutMs Timeout in ms to wait. Set negative to disable timeout.
     * @return True if the timeout ran out. False otherwise.
     */
    public boolean waitFocus(String name, long timeoutMs);

    /**
     * Starts recording the camera path to a temporary file. This command has no
     * effect if the camera is already being recorded.
     */
    public void startRecordingCameraPath();

    /**
     * Stops the current camera recording. This command has no effect if the
     * camera was not being recorded.
     */
    public void stopRecordingCameraPath();

    /**
     * Runs the camera recording file with the given path.
     * @param path The path of the camera recording file to run.
     */
    public void runCameraRecording(String path);

}
