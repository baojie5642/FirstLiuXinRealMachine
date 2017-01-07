package LiuXinTestForAllThing;

import java.util.concurrent.ThreadLocalRandom;

public class InitHolder {

	public HolderFromJavaConcurrencyBook holderFromJavaConcurrencyBook;

	public void initHolder() {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int i = random.nextInt();
		holderFromJavaConcurrencyBook = new HolderFromJavaConcurrencyBook(i);
	}
}
