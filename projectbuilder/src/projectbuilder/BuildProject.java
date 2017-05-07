package projectbuilder;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;

import java.io.File;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class BuildProject {
	public static void main(String[] args){
		try {
			String currDir = new java.io.File( "." ).getCanonicalPath().replaceAll("/projectbuilder", "");
			System.out.println("currDir:" + currDir);
			String pathToPackages = currDir + "/packages";
			String pathToBuild = currDir + "/build";
			String pathToSchemaBuilder = pathToBuild + "/schema";
			
			System.out.println("Writing project");
			System.out.println("to packages" + pathToPackages);
			File schemaFile = new File(pathToPackages);
			File[] packages = schemaFile.listFiles(File::isDirectory);
			for(File pkg :  packages){
				BuildProject.TransferSchemaFiles(pkg);
				BuildProject.TransferWorkerDirectory(pkg);
			}
		} catch(Exception e){
			System.out.println("Error:" + e);
		}
	}
	
	private static void TransferSchemaFiles(File dir){		
		try {
			String currDir = new java.io.File( "." ).getCanonicalPath().replaceAll("/projectbuilder", "");
			System.out.println("Writing");
			System.out.println("currDir:" + dir.getAbsolutePath());
			String destDir = currDir + "/build/schema";
			
			String schemaPath = dir.getAbsolutePath() + "/schema";
			FileUtils.copyDirectory(new File(schemaPath), new File(destDir));
		} catch(Exception e){
			System.out.println("Error:" + e);
		}
	}
	
	private static void TransferWorkerDirectory(File dir){
		try {
			String currDir = new java.io.File( "." ).getCanonicalPath().replaceAll("/projectbuilder", "");
			String destDir = currDir + "/build/workers";			
			String workerPath = dir.getAbsolutePath() + "/worker";
			FileUtils.copyDirectory(new File(workerPath), new File(destDir));
		} catch(Exception e){
			System.out.println("Error:" + e);
		}
		
	}
	
	private void TransferSnapshotSchema(){
		
	}
	
	private void concatDefaultLaunchJSON(){
		
	}

}
