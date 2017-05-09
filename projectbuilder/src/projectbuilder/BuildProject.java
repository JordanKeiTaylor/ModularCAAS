package projectbuilder;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;


public class BuildProject {
	public static void main(String[] args){
		try {
			String currDir = new java.io.File( "." ).getCanonicalPath().replaceAll("/projectbuilder", "");
			System.out.println("currDir:" + currDir);
			String pathToPackages = currDir + "/packages";
			Boolean alreadyAdded = false;
			
			System.out.println("Writing project");
			System.out.println("to packages" + pathToPackages);
			File schemaFile = new File(pathToPackages);
			File[] packages = schemaFile.listFiles(File::isDirectory);
			for(File pkg :  packages){
				alreadyAdded = BuildProject.UpdatePackageList(pkg);
				BuildProject.TransferSchemaFiles(pkg);
				BuildProject.TransferWorkerDirectory(pkg);
				BuildProject.TransferSnapshotSchema(pkg);
				BuildProject.TransferUnityAssets(pkg);
				if(!alreadyAdded){
					BuildProject.TransferWorkerYamlFile(pkg);
					BuildProject.TransferPreprocessorYamlFile(pkg);
					BuildProject.TransferPreprocessorConfigYamlFile(pkg);
					BuildProject.AppendToPrefabsList(pkg);
				}
			}
			
		} catch(Exception e){
			System.out.println("Error:" + e);
		}
	}
	
	private static Boolean UpdatePackageList(File dir){
		Boolean alreadyAdded = false;
		
		try {
			String currDir = new java.io.File( "." ).getCanonicalPath().replaceAll("/projectbuilder", "");
			String[] packageNameFinder = dir.getAbsolutePath().split("/");
			String packageName = packageNameFinder[packageNameFinder.length - 1];
			
			String existingYamlPath = currDir + "/build/currentPackages.yaml";
			
		    List<String> existingConfig = Files.readAllLines(Paths.get(existingYamlPath), StandardCharsets.UTF_8);
		    
		    
		    
		    if(existingConfig.contains(packageName)){
		    	System.out.println("Didnt add package " + packageName + " to package list because it was already there");
		    	alreadyAdded = true;
		    } else {
		    	existingConfig.add(packageName);
		    }
		  
		    Path file = Paths.get(existingYamlPath);
		    Files.write(file, existingConfig, Charset.forName("UTF-8"));
			
		} catch(Exception e){
			System.out.println("Error:" + e);
		}
		return alreadyAdded;
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
	
	private static void TransferWorkerYamlFile(File dir){
		try {
			String currDir = new java.io.File( "." ).getCanonicalPath().replaceAll("/projectbuilder", "");		
			String existingYamlPath = currDir + "/build/workers/SnapshotGenerator/worker/src/main/scala/workers/workers.yaml";
			String toAppendPath = dir.getAbsolutePath() + "/worker/config.yaml";
			//System.out.println("Character to remove");
		    List<String> existingConfig = Files.readAllLines(Paths.get(existingYamlPath), StandardCharsets.UTF_8);
		    //System.out.println(existingConfig.get(existingConfig.size()-1).trim().isEmpty());
		    List<String> linesToAppend = Files.readAllLines(Paths.get(toAppendPath), StandardCharsets.UTF_8);
		    existingConfig.addAll(linesToAppend);

		    existingConfig = existingConfig.stream()
		    	    .filter(str -> !str.trim().isEmpty()).collect(Collectors.toList());
		    
		    Path file = Paths.get(existingYamlPath);
		    Files.write(file, existingConfig, Charset.forName("UTF-8"));

		} catch(Exception e){
			System.out.println("Error:" + e);
		}
	}
	
	private static void TransferPreprocessorConfigYamlFile(File dir){
		try {
			String currDir = new java.io.File( "." ).getCanonicalPath().replaceAll("/projectbuilder", "");		
			String existingYamlPath = currDir + "/build/workers/SnapshotGenerator/worker/src/main/scala/preprocessors/packageConfigs.yaml";
			String toAppendPath = dir.getAbsolutePath() + "/SnapshotGenerator/config.yaml";
			//System.out.println("Character to remove");
		    List<String> existingConfig = Files.readAllLines(Paths.get(existingYamlPath), StandardCharsets.UTF_8);
		    System.out.println(existingConfig);
		    List<String> linesToAppend = Files.readAllLines(Paths.get(toAppendPath), StandardCharsets.UTF_8);
		    existingConfig.addAll(linesToAppend);

		    existingConfig = existingConfig.stream()
		    	    .filter(str -> !str.trim().isEmpty()).collect(Collectors.toList());
		    
		    Path file = Paths.get(existingYamlPath);
		    Files.write(file, existingConfig, Charset.forName("UTF-8"));

		} catch(Exception e){
			System.out.println("Error:" + e);
		}
	}
	
	private static void AppendToPrefabsList(File dir){
		try {
			String currDir = new java.io.File( "." ).getCanonicalPath().replaceAll("/projectbuilder", "");		
			String existingYamlPath = currDir + "/build/workers/SnapshotGenerator/worker/src/main/scala/core/settings/SnapshotPrefabs.scala";
			String toAppendPath = dir.getAbsolutePath() + "/prefabsAdded.yaml";
			//System.out.println("Character to remove");
		    List<String> existingConfig = Files.readAllLines(Paths.get(existingYamlPath), StandardCharsets.UTF_8);
		    existingConfig.remove(existingConfig.size() - 1);
		    
		    List<String> linesToAppend = Files.readAllLines(Paths.get(toAppendPath), StandardCharsets.UTF_8);
		    
		    linesToAppend = linesToAppend.stream()
		            .map(elt -> "  val " + elt + " = Value(13)")
		            .collect(Collectors.toList());
		    linesToAppend.add("}");
		    System.out.println("Appended");
		    System.out.println(linesToAppend);
		    existingConfig.addAll(linesToAppend);

		    existingConfig = existingConfig.stream()
		    	    .filter(str -> !str.trim().isEmpty()).collect(Collectors.toList());
		    
		    Path file = Paths.get(existingYamlPath);
		    Files.write(file, existingConfig, Charset.forName("UTF-8"));

		} catch(Exception e){
			System.out.println("Error:" + e);
		}
	}
	
	
	
	private static void TransferPreprocessorYamlFile(File dir){
		try {
			String currDir = new java.io.File( "." ).getCanonicalPath().replaceAll("/projectbuilder", "");	
			String existingYamlPath = currDir + "/build/workers/SnapshotGenerator/worker/src/main/scala/preprocessors/PreprocessorList.yaml";
			String toAppendPath = dir.getAbsolutePath() + "/SnapshotGenerator/preprocessor.yaml";
			
		    List<String> existingConfig = Files.readAllLines(Paths.get(existingYamlPath), StandardCharsets.UTF_8);
		    List<String> linesToAppend = Files.readAllLines(Paths.get(toAppendPath), StandardCharsets.UTF_8);
		    existingConfig.addAll(linesToAppend);
		    
		    existingConfig = existingConfig.stream()
		    	    .filter(str -> !str.trim().isEmpty()).collect(Collectors.toList());

		    Path file = Paths.get(existingYamlPath);
		    Files.write(file, existingConfig, Charset.forName("UTF-8"));

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
	

}
