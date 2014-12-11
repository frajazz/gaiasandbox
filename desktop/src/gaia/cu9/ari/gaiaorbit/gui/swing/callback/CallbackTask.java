package gaia.cu9.ari.gaiaorbit.gui.swing.callback;

public class CallbackTask implements java.lang.Runnable {

    private final Runnable task;

    private final Callback callback;

    public CallbackTask(Runnable task, Callback callback) {
	this.task = task;
	this.callback = callback;
    }

    public void run() {
	Object result = task.run();
	callback.complete(result);
    }

}