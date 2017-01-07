package SpaceProtostuff.FromDisk;

import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;

public class ReadFileInDirIntoQueue {

	protected static final ThreadPoolExecutor DefultThreadPoolForSearch = (ThreadPoolExecutor) Executors
			.newCachedThreadPool();

	protected static ConcurrentLinkedQueue<File> createFileList(final String path) {
		ReadFromDiskCheck.checkPath(path);

		File file = new File(path);
		File[] fileList = file.listFiles(ReadFromDiskFileFilter.Defult_File_Filter);
		int i = 0;
		ConcurrentLinkedQueue<File> fileListQueue = new ConcurrentLinkedQueue<File>();
		try {
			if (fileList == null) {
				throw new NullPointerException("fileList: " + fileList + " ,must not be null.");
			}
			i = fileList.length;
			if (i == 0) {
				throw new IllegalArgumentException("there is no file in this path:" + path + ".");
			}
			for (int j = 0; j < i; j++) {
				if (fileList[j].isFile() && fileList[j].getName().endsWith(".lx")) {
					fileListQueue.add(fileList[j]);
				} else {
					// 递归调用createFileList()
				}
			}
		} finally {
			if (null != fileList) {
				for (int k = 0; k < i; k++) {
					fileList[k] = null;
				}
				fileList = null;
			}
		}
		return fileListQueue;
	}

	public static void main(String args[]){
		File file=new File("/home/liuxin/work/liuxinAllTest/taskname/whichline");
		List<File> files=(List<File>) FileUtils.listFiles(file, FileFileFilter.FILE,DirectoryFileFilter.DIRECTORY );
		int i=files.size();
		for(int j=0;j<i;j++){
			System.out.println(files.get(j).getAbsolutePath());
		}
		
		
		
	}
	
	
	
}
