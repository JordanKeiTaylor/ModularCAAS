import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import java.util.Collection;

public class ProjectBuilder {


	public static void main(String[] args){
		try {
			String currDir = new java.io.File( "." ).getCanonicalPath();
			String pathToPackages = currDir + "/packages";
			String pathToBuild = currDir + "/build";
			String pathToSchemaBuilder = pathToBuild + "/schema";
			
			System.out.println("Writing project");
			Collection<File> packages = FileUtils.listFilesAndDirs(new File(pathToPackages), FalseFileFilter.FALSE, TrueFileFilter.TRUE);
			for(File pkg :  packages){
				ProjectBuilder.TransferSchemaFiles(pkg);
			}
		} catch(Exception e){
			System.out.println("Error:" + e);
		}
	}
	
	public static void TransferSchemaFiles(File dir){		
		try {
			String currDir = new java.io.File( "." ).getCanonicalPath();
			String destDir = currDir + "/build/schema";
			String schemaPath = dir.getAbsolutePath() + "/schema";
			FileUtils.copyDirectoryToDirectory(new File(schemaPath), new File(destDir));
		} catch(Exception e){
			System.out.println("Error:" + e);
		}
	}
	
	private void TransferWorkerDirectory(){
		
	}
	
	private void TransferSnapshotSchema(){
		
	}
	
}

