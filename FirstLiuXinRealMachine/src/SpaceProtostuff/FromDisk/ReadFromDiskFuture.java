package SpaceProtostuff.FromDisk;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;

public class ReadFromDiskFuture<T> {

	private volatile int state;
	private static final int New = 0;
	private static final int Completing = 1;
	private static final int Compled = 2;
	private static final int UnknowError = 3;
	private Object outcome;
	private volatile WaitNode waiters;

	private ReadFromDiskFuture(final Class<T> whichTypeCanReturn) {
		this.state = New;
	}

	@SuppressWarnings("rawtypes")
	public static <T> ReadFromDiskFuture createFutureToBuild(final Class<T> whichTypeCanReturn) {
		if(null==whichTypeCanReturn){
			throw new NullPointerException();
		}
		@SuppressWarnings("unchecked")
		ReadFromDiskFuture futureToBuild = new ReadFromDiskFuture(whichTypeCanReturn);
		return futureToBuild;
	}

	public boolean isDone() {
		return state != New;
	}

	public T get() throws InterruptedException, ExecutionException {
		int s = state;
		if (s <= Completing) {
			s = awaitDone(false, 0L);
		}
		return report(s);
	}

	public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		if (unit == null) {
			throw new NullPointerException();
		}
		int s = state;
		if (s <= Completing && (s = awaitDone(true, unit.toNanos(timeout))) <= Completing) {
			throw new TimeoutException();
		}
		return report(s);
	}

	private int awaitDone(boolean timed, long nanos) throws InterruptedException {
		final long deadline = timed ? System.nanoTime() + nanos : 0L;
		WaitNode q = null;
		boolean queued = false;
		for (;;) {
			if (Thread.interrupted()) {
				removeWaiter(q);
				throw new InterruptedException();
			}
			int s = state;
			if (s > Completing) {
				if (q != null) {
					q.thread = null;
				}
				return s;
			} else if (s == Completing) {
				Thread.yield();
			} else if (q == null) {
				q = new WaitNode();
			} else if (!queued) {
				queued = UnSafe.compareAndSwapObject(this, waitersOffset, q.next = waiters, q);
			} else if (timed) {
				nanos = deadline - System.nanoTime();
				if (nanos <= 0L) {
					removeWaiter(q);
					return state;
				}
				LockSupport.parkNanos(this, nanos);
			} else {
				LockSupport.park(this);
			}
		}
	}

	private void removeWaiter(WaitNode node) {
		if (node != null) {
			node.thread = null;
			retry: for (;;) {
				for (WaitNode pred = null, q = waiters, s; q != null; q = s) {
					s = q.next;
					if (q.thread != null) {
						pred = q;
					} else if (pred != null) {
						pred.next = s;
						if (pred.thread == null) {
							continue retry;
						}
					} else if (!UnSafe.compareAndSwapObject(this, waitersOffset, q, s)) {
						continue retry;
					}
				}
				break;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private T report(int s) throws ExecutionException {
		Object x = outcome;
		if (s == Compled) {
			return (T) x;
		}
		if (s >= UnknowError) {
			throw new IllegalStateException();
		}
		throw new ExecutionException((Throwable) x);
	}

	protected void set(final T getThis) {
		setInner(getThis);
	}

	private void setInner(final T getThis) {
		if (UnSafe.compareAndSwapInt(this, stateOffset, New, Completing)) {
			outcome = getThis;
			UnSafe.putOrderedInt(this, stateOffset, Compled);
			finishCompletion();
		}
	}

	private void finishCompletion() {
		for (WaitNode q; (q = waiters) != null;) {
			if (UnSafe.compareAndSwapObject(this, waitersOffset, q, null)) {
				for (;;) {
					Thread t = q.thread;
					if (t != null) {
						q.thread = null;
						LockSupport.unpark(t);
					}
					WaitNode next = q.next;
					if (next == null) {
						break;
					}
					q.next = null;
					q = next;
				}
				break;
			}
		}
		done();
	}

	protected void done() {

	}

	private static final class WaitNode {
		volatile Thread thread;
		volatile WaitNode next;

		WaitNode() {
			thread = Thread.currentThread();
		}
	}

	private static final sun.misc.Unsafe UnSafe;
	private static final long stateOffset;
	private static final long waitersOffset;
	static {
		try {
			UnSafe = ReadFromDiskGetUnsafe.getUnsafe();
			Class<?> k = ReadFromDiskFuture.class;
			stateOffset = UnSafe.objectFieldOffset(k.getDeclaredField("state"));
			waitersOffset = UnSafe.objectFieldOffset(k.getDeclaredField("waiters"));
		} catch (Exception e) {
			throw new Error(e);
		}
	}

}
