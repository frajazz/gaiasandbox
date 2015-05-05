package gaia.cu9.ari.gaiaorbit.util.screenshot;

import gaia.cu9.ari.gaiaorbit.render.BufferedFrame;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

/**
 * Buffers the writing of images to disk.
 * @author Toni Sagrista
 *
 */
public class BufferedFileImageRenderer implements IFileImageRenderer {
    /** Daemon timer **/
    private static Timer timer = new Timer(true);

    /**
     * Output frame buffer and BufferedFrame pool
     */
    private List<BufferedFrame> outputFrameBuffer;
    private Pool<BufferedFrame> bfPool;
    private int bufferSize;

    public BufferedFileImageRenderer(int bufferSize) {
        this.bufferSize = bufferSize;
        outputFrameBuffer = new ArrayList<BufferedFrame>(bufferSize);
        bfPool = Pools.get(BufferedFrame.class, bufferSize);
    }

    @Override
    public String saveScreenshot(String folder, String fileprefix, int w, int h, boolean immediate) {
        String res = null;
        if (!immediate) {
            if (outputFrameBuffer.size() >= bufferSize) {
                flush();
            }

            synchronized (outputFrameBuffer) {
                BufferedFrame bf = bfPool.obtain();
                bf.pixmap = ImageRenderer.renderToPixmap(w, h);
                bf.folder = folder;
                bf.filename = fileprefix;

                outputFrameBuffer.add(bf);
            }
            res = "buffer";
        } else {
            // Screenshot while the frame buffer is on
            res = ImageRenderer.renderToImageGl20(folder, fileprefix, w, h);
        }
        return res;
    }

    @Override
    public void flush() {
        synchronized (outputFrameBuffer) {
            final List<BufferedFrame> outputFrameBufferCopy = new ArrayList<BufferedFrame>(outputFrameBuffer);
            outputFrameBuffer.clear();

            final int size = outputFrameBufferCopy.size();
            if (size > 0) {
                // Notify
                Logger.info(I18n.bundle.get("notif.flushframebuffer"));

                TimerTask tt = new TimerTask() {
                    @Override
                    public void run() {
                        String folder = null;
                        for (int i = 0; i < size; i++) {
                            BufferedFrame bf = outputFrameBufferCopy.get(i);
                            ImageRenderer.writePixmapToImage(bf.folder, bf.filename, bf.pixmap);
                            folder = bf.folder;
                            bfPool.free(bf);
                        }
                        Logger.info(I18n.bundle.format("notif.flushframebuffer.finished", size, folder));
                    }
                };
                timer.schedule(tt, 0);
            }

        }

    }

}
