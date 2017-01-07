package SpaceProtostuff;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

public class ObjectToByte {
	private static final ConcurrentHashMap<Class<?>, Schema<?>> CachedSchema = new ConcurrentHashMap<Class<?>, Schema<?>>();
	private static final ReentrantLock mainLock = new ReentrantLock();

	private ObjectToByte() {
		super();
	}

	@SuppressWarnings("unchecked")
	private static <T> Schema<T> getSchema(final Class<T> cls) {
		if (null == cls) {
			throw new NullPointerException("cls in getSchema() must not be null");
		}
		Schema<T> schema = (Schema<T>) CachedSchema.get(cls);
		if (schema == null) {
			System.getProperties().setProperty("protostuff.runtime.always_use_sun_reflection_factory", "true");
			schema = RuntimeSchema.createFrom(cls);
			if (schema != null) {
				CachedSchema.putIfAbsent(cls, schema);
			}
		}
		return schema;
	}

	public static <T> byte[] toByte(final T obj) {
		if (null == obj) {
			throw new NullPointerException("obj in toByte() must not be null");
		}
		final ReentrantLock lock = mainLock;
		lock.lock();
		try {
			final byte[] realBytes = realBytes(obj);
			return realBytes;
		} finally {
			lock.unlock();
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> byte[] realBytes(final T obj) {
		byte[] bytes = null;
		Class<T> cls = (Class<T>) obj.getClass();
		Schema<T> schema = getSchema(cls);
		byte[] bytesForBuffer = new byte[LinkedBuffer.DEFAULT_BUFFER_SIZE];
		LinkedBuffer buffer = LinkedBuffer.use(bytesForBuffer);
		try {
			bytes = ProtostuffIOUtil.toByteArray(obj, schema, buffer);
			if ((null == bytes) || (bytes.length == 0)) {
				bytes = new byte[0];
			}
		} finally {
			releaseSource(buffer, bytesForBuffer, schema, cls);
		}
		return bytes;
	}

	private static <T> void releaseSource(LinkedBuffer buffer, byte bytesForBuffer[], Schema<T> schema, Class<T> cls) {
		try {
			releaseInner(buffer,bytesForBuffer,schema,cls);
		} finally {
			releaseInner(buffer,bytesForBuffer,schema,cls);
		}
	}

	private static <T> void releaseInner(LinkedBuffer buffer, byte bytesForBuffer[], Schema<T> schema, Class<T> cls){
		if (null != buffer) {
			buffer.clear();
			buffer = null;
		}
		if (bytesForBuffer != null) {
			bytesForBuffer = null;
		}
		if (null != schema) {
			schema = null;
		}
		if (null != cls) {
			cls = null;
		}
	}
	
}
