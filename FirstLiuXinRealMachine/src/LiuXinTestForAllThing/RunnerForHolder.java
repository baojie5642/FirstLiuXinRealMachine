package LiuXinTestForAllThing;

public class RunnerForHolder implements Runnable{
	
	
	
	
	
	@Override
	public void run(){
		HolderFromJavaConcurrencyBook holderFromJavaConcurrencyBook=null;
		InitHolder initHolder=null;
		while(true){
			try {
				OneStaticSem.SEMAPHORE.acquire();
				for(int i=0;i<10000;i++){
					initHolder=new InitHolder();
					//holderFromJavaConcurrencyBook=initHolder.
					
					
					
					
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
