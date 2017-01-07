package SpaceProtostuff.FromDisk;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ReadFromDiskSearchFileRunner implements Callable<ConcurrentLinkedQueue<File>>{

	private final ConcurrentLinkedQueue<File> fileQueue;
	
	private ReadFromDiskSearchFileRunner(final ConcurrentLinkedQueue<File> fileQueue){
		this.fileQueue=fileQueue;
	}
	
	
	public static ReadFromDiskSearchFileRunner createSearchFileRunner(final ConcurrentLinkedQueue<File> fileQueue){
		ReadFromDiskSearchFileRunner readFromDiskSearchFileRunner=new ReadFromDiskSearchFileRunner(fileQueue);
		return readFromDiskSearchFileRunner;
	}
	
	
	@Override
	public ConcurrentLinkedQueue<File> call(){
		ConcurrentLinkedQueue<File> realFileList=new ConcurrentLinkedQueue<File>();
		
		File file=fileQueue.poll();
		if(null==file){
			return new ConcurrentLinkedQueue<File>();
		}else {
			
			File[] listFiles=file.listFiles(ReadFromDiskDirFilter.Defult_Dir_Filter);
			if(null==listFiles||listFiles.length==0){
				File[] listFiles2=file.listFiles(ReadFromDiskFileFilter.Defult_File_Filter);
				if(null==listFiles2||listFiles2.length==0){
					return realFileList;
					
					
				}else {
					
					
					
				}
				
				
				
			}else {
				
				
				
				
			}
			
			
			
			
			
			
		}
		
		
		
		
		
		
		
		return new ConcurrentLinkedQueue<File>() ;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
