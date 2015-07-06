/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package gaia.cu9.ari.gaiaorbit.util.g3d;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.badlogic.gdx.utils.BufferUtils;

/**
 * Copy of libgdx's IndexArray class using integers instead of shorts for indices.
 */
public class IndexArrayInt implements IndexDataInt {
	final static IntBuffer tmpHandle = BufferUtils.newIntBuffer(1);

	IntBuffer buffer;
	ByteBuffer byteBuffer;

	// used to work around bug: https://android-review.googlesource.com/#/c/73175/
	private final boolean empty;

	/** Creates a new IndexArrayInt to be used with vertex arrays.
	 * 
	 * @param maxIndices the maximum number of indices this buffer can hold */
	public IndexArrayInt(int maxIndices) {

		empty = maxIndices == 0;
		if (empty) {
			maxIndices = 1; // avoid allocating a zero-sized buffer because of bug in Android's ART < Android 5.0
		}

		byteBuffer = BufferUtils.newUnsafeByteBuffer(maxIndices * 2);
		buffer = byteBuffer.asIntBuffer();
		buffer.flip();
		byteBuffer.flip();
	}

	/** @return the number of indices currently stored in this buffer */
	public int getNumIndices () {
		return empty ? 0 : buffer.limit();
	}

	/** @return the maximum number of indices this IndexArrayInt can store. */
	public int getNumMaxIndices () {
		return empty ? 0 : buffer.capacity();
	}

	/** <p>
	 * Sets the indices of this IndexArrayInt, discarding the old indices. The count must equal the number of indices to be copied to
	 * this IndexArrayInt.
	 * </p>
	 * 
	 * <p>
	 * This can be called in between calls to {@link #bind()} and {@link #unbind()}. The index data will be updated instantly.
	 * </p>
	 * 
	 * @param indices the vertex data
	 * @param offset the offset to start copying the data from
	 * @param count the number of shorts to copy */
	public void setIndices (int[] indices, int offset, int count) {
		buffer.clear();
		buffer.put(indices, offset, count);
		buffer.flip();
		byteBuffer.position(0);
		byteBuffer.limit(count << 1);
	}
	
	public void setIndices (IntBuffer indices) {
		int pos = indices.position();
		buffer.clear();
		buffer.limit(indices.remaining());
		buffer.put(indices);
		buffer.flip();
		indices.position(pos);
		byteBuffer.position(0);
		byteBuffer.limit(buffer.limit() << 1);
	}

	/** <p>
	 * Returns the underlying ShortBuffer. If you modify the buffer contents they wil be uploaded on the call to {@link #bind()}.
	 * If you need immediate uploading use {@link #setIndices(int[], int, int)}.
	 * </p>
	 * 
	 * @return the underlying short buffer. */
	public IntBuffer getBuffer () {
		return buffer;
	}

	/** Binds this IndexArrayInt for rendering with glDrawElements. */
	public void bind () {
	}

	/** Unbinds this IndexArrayInt. */
	public void unbind () {
	}

	/** Invalidates the IndexArrayInt so a new OpenGL buffer handle is created. Use this in case of a context loss. */
	public void invalidate () {
	}

	/** Disposes this IndexArrayInt and all its associated OpenGL resources. */
	public void dispose () {
		BufferUtils.disposeUnsafeByteBuffer(byteBuffer);
	}
}
