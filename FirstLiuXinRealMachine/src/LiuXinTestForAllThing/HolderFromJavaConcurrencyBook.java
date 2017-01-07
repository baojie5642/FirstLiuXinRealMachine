package LiuXinTestForAllThing;

public class HolderFromJavaConcurrencyBook {

	private int n;

	public HolderFromJavaConcurrencyBook(int n) {
		this.n = n;
	}

	public void assertSanity() {
		if (n != n) {
			throw new AssertionError("This statement is false !");
		}
	}

}
