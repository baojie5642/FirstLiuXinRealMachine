package SpaceProtostuff.demo1;

import java.io.Serializable;

public class A implements Serializable{
	private static final long serialVersionUID=2016060810595155555l;
	Object objectAInner;

	public A(){
		super();
	}
	
	public Object getObjectA() {
		return objectAInner;
	}

	public void setObjectA(Object object) {
		objectAInner = object;
	}

	public static void main(String[] args) {
		B b = new B();
		A a = new A();
		a.setObjectA(b);

		B bb = (B) a.getObjectA();
		System.out.println("print  A-B-List before  serialize");
		for (int i = 0; i < bb.getList().size(); i++) {
			System.out.println("list[" + i + "]=" + bb.getList().get(i));
		}
		byte[] test = SerializationUtil.serialize(a);
		A newA = SerializationUtil.deserialize(test, A.class);
		bb = (B) newA.getObjectA();
		System.out.println("print   A-B-List after serialize");
		for (int i = 0; i < bb.getList().size(); i++) {
			System.out.println("list[" + i + "]=" + bb.getList().get(i));
		}
		System.out.println();
	}
}
