package SpaceProtostuff;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

public final class RuntimeEnv0 {
	private RuntimeEnv0() {
	}

	public static <T> Instantiator<T> newInstantiator(Class<T> clazz) {
		return new MyFunctionDileWithListBugInProtostuff<T>(clazz);
	}

	public static abstract class Instantiator<T> {
		public abstract T newInstance();
	}

	private static final class MyFunctionDileWithListBugInProtostuff<T> extends Instantiator<T> {
		private static final Objenesis objenesis = new ObjenesisStd(true);
		final Class<T> clazz;

		public MyFunctionDileWithListBugInProtostuff(final Class<T> clazz) {
			this.clazz = clazz;
		}

		@Override
		public T newInstance() {
			return objenesis.newInstance(clazz);
		}
	}
}
