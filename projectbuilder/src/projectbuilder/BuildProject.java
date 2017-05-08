package projectbuilder;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
				BuildProject.TransferSnapshotSchema(pkg);
				BuildProject.TransferUnityAssets(pkg);
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
	
	private static void TransferSnapshotSchema(File dir){
		try {
			String currDir = new java.io.File( "." ).getCanonicalPath().replaceAll("/projectbuilder", "");
			String destDir = currDir + "/build/workers/SnapshotGenerator/worker/src/main/scala/preprocessors";			
			String workerPath = dir.getAbsolutePath() + "/SnapshotGenerator";
			FileUtils.copyDirectory(new File(workerPath), new File(destDir));
		} catch(Exception e){
			System.out.println("Error:" + e);
		}
		
	}
	
	private static void TransferUnityAssets(File dir){
		TransferUnityEntityPrefabs(dir);
		TransferUnityGamelogic(dir);
		TransferUnityScripts(dir);
		TransferUnityTextures(dir);
		
	}
	
	private static void TransferUnityEntityPrefabs(File dir){
		try {
			String currDir = new java.io.File( "." ).getCanonicalPath().replaceAll("/projectbuilder", "");
			String destDir = currDir + "/build/workers/unity/Assets/EntityPrefabs";			
			String workerPath = dir.getAbsolutePath() + "/unity/Assets/EntityPrefabs";
			FileUtils.copyDirectory(new File(workerPath), new File(destDir));
		} catch(Exception e){
			System.out.println("Error:" + e);
		}
		
	}
	
	private static void TransferUnityGamelogic(File dir){
		try {
			String currDir = new java.io.File( "." ).getCanonicalPath().replaceAll("/projectbuilder", "");
			String destDir = currDir + "/build/workers/unity/Assets/Gamelogic/Visualizers";			
			String workerPath = dir.getAbsolutePath() + "/unity/Assets/Gamelogic/Visualizers";
			FileUtils.copyDirectory(new File(workerPath), new File(destDir));
		} catch(Exception e){
			System.out.println("Error:" + e);
		}
		
	}
	
	private static void TransferUnityScripts(File dir){
		try {
			String currDir = new java.io.File( "." ).getCanonicalPath().replaceAll("/projectbuilder", "");
			String destDir = currDir + "/build/workers/unity/Assets/Scripts";			
			String workerPath = dir.getAbsolutePath() + "/unity/Assets/Scripts";
			FileUtils.copyDirectory(new File(workerPath), new File(destDir));
		} catch(Exception e){
			System.out.println("Error:" + e);
		}
		
	}
	
	private static void TransferUnityTextures(File dir){
		try {
			String currDir = new java.io.File( "." ).getCanonicalPath().replaceAll("/projectbuilder", "");
			String destDir = currDir + "/build/workers/unity/Assets/Textures";			
			String workerPath = dir.getAbsolutePath() + "/unity/Assets/Textures";
			FileUtils.copyDirectory(new File(workerPath), new File(destDir));
		} catch(Exception e){
			System.out.println("Error:" + e);
		}
		
	}
	
	private void concatDefaultLaunchJSON(){
		Gson gson = new Gson();
	}

}

class workerConfigJSON{
	private String name;
	private Integer numEntitiesPerWorker;
}

class LaunchJSON{
	private String template;
	
	class legacyFlagObject {
		private String name;
		private String value;
	}
	
	class dimensionsObject {
		private Integer x_meters;
		private Integer z_meters;
	}
	
	class SnapshotObject{
		private Integer snapshot_write_period_seconds;
	}
	
	class loadBalacingObject{
		
	}
	
	class worldObject{
		private Integer chunkEdgeLengthMeters;
		private Integer streaming_query_interval;
		private legacyFlagObject[] legacy_flags;
		private legacyFlagObject[] legacy_javaparams;
		private dimensionsObject dimensions;
		private SnapshotObject snapshots;
	}
	
	class autoHexGridObject{
		
	}
	
}
