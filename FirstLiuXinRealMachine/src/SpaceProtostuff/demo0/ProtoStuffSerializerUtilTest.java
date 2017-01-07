package SpaceProtostuff.demo0;

public class ProtoStuffSerializerUtilTest {
	 public static class Person{
	        int id;
	        String name;
	        
	        public Person(){
	            
	        }
	        
	        public Person(int id, String name){
	            this.id = id;
	            this.name = name;
	        }
	        
	        public int getId() {
	            return id;
	        }
	        public String getName() {
	            return name;
	        }
	        
	    }
	    
	    
	    public static void main(String args[]){
	        Person p = new Person(1,"ff");
	        byte[] arr = ProtoStuffSerializerUtil.serialize(p);
	        Person result = ProtoStuffSerializerUtil.deserialize(arr, Person.class);
	        
	        System.out.println(result.getName());
	    }
}
