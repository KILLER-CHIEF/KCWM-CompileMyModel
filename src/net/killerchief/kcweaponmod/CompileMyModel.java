package net.killerchief.kcweaponmod;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class CompileMyModel {

	private static List<File> javaFiles(File directory) {
		List<File> textFiles = new ArrayList<File>();
		if (directory.exists() && directory.isDirectory())
		{
			for (File file : directory.listFiles()) {
				if (file.isDirectory()) {
					textFiles.addAll(javaFiles(file));
				} else if (file.getName().toLowerCase().endsWith((".java"))) {
					textFiles.add(file);
				}
			}
		}
		return textFiles;
	}
	
	
	public static void main(String[] args) {
		if (args != null && args.length > 0)
		{
			if (args.length == 1)
			{
				log("Begin Compiling Class");
				if (begin(args[0])) {
					log("Program exited with success!");
					return;
				}
			}
			else if (args.length == 2)
			{
				if (args[0] != null && args[1] != null)
				{
					if (args[0].equalsIgnoreCase("obfuscate"))
					{
						if (AddNamePairs())
						{
							File dir = Paths.get(workingDirectory, args[1]).toFile();
							log("obfuscating: "+dir.getAbsolutePath());
							
							List<File> classes = javaFiles(dir);
							
							log(classes.toString());
							
							for (File file : classes) {
								try {
									List<String> lines = readSmallTextFile(file.getAbsolutePath());
									List<String> out = new ArrayList<String>();
									for (String line : lines) {
										out.add(filterNonObfuscatedText(line));
									}
									writeSmallTextFile(out, file.getAbsolutePath());
								} catch (IOException e) {
									log("Reading file failed: "+ file.getAbsolutePath());
									e.printStackTrace();
								}
							}
							log("Success!");
							return;
						}
						
					}
					else if (args[0].equalsIgnoreCase("deobfuscate"))
					{
						if (AddNamePairs())
						{
							File dir = Paths.get(workingDirectory, args[1]).toFile();
							log("deobfuscating: "+dir.getAbsolutePath());
							
							List<File> classes = javaFiles(dir);
							
							log(classes.toString());
							
							for (File file : classes) {
								try {
									List<String> lines = readSmallTextFile(file.getAbsolutePath());
									List<String> out = new ArrayList<String>();
									for (String line : lines) {
										out.add(filterObfuscatedText(line));
									}
									writeSmallTextFile(out, file.getAbsolutePath());
								} catch (IOException e) {
									log("Reading file failed: "+ file.getAbsolutePath());
									e.printStackTrace();
								}
							}
							log("Success!");
							return;
						}
					}
				}
			}
		}
		else
		{
			log("There are no input arguments.");
		}
		log("Program exited with failure! Check Log.");
	}
	
	public static void log(String text)
	{
		System.out.println(text);
	}
	
	private final static Charset ENCODING = StandardCharsets.UTF_8;
	public static final String workingDirectory = Paths.get(".").toAbsolutePath().normalize().toString();
	private final static String FILE_ObfuscateNamePairs = Paths.get(workingDirectory, "ObfuscationNamePairs.txt").toString();
	private final static String FILE_TemplateClassHeader = Paths.get(workingDirectory, "TemplateClassHeader.java").toString();
	private final static String FILE_TemplateClassFooter = Paths.get(workingDirectory, "TemplateClassFooter.java").toString();
	private final static String FILE_RemoveList = Paths.get(workingDirectory, "RemoveCodeList.txt").toString();
	private static HashMap<String, String> ObfuscateNames = new HashMap<String, String>();
	
	private static List<String> readSmallTextFile(String aFileName) throws IOException {
		Path path = Paths.get(aFileName);
		return Files.readAllLines(path, ENCODING);
	}

	private static void writeSmallTextFile(List<String> aLines, String aFileName) throws IOException {
		Path path = Paths.get(aFileName);
		Files.write(path, aLines, ENCODING);
	}
	
	private static boolean AddNamePairs()
	{
		try {
			List<String> lines = readSmallTextFile(FILE_ObfuscateNamePairs);
			for (int i = 0; i < lines.size(); i++) {
				String line = lines.get(i);
				if (!line.startsWith("#") && !line.trim().equalsIgnoreCase("") && line.contains("-"))
				{
					String[] pair = line.split("-");
					if (pair.length == 2)
						ObfuscateNames.put(pair[0].trim(), pair[1].trim());
					else
						log("Bad entry on line "+i);
				}
			}
		} catch (IOException e) {
			log("Reading DeobfuscationNames.txt failed!");
			e.printStackTrace();
			return false;
		}
		
		log("Successfully read and saved obfuscation name pairs.");
		return true;
	}
	
	private static String filterNonObfuscatedText(String text)
	{
		if (text.trim().equalsIgnoreCase("")) {
			return text;
		}
		
		String out = text;
		for (Entry<String, String> ele : ObfuscateNames.entrySet()) {
			if (out.contains(ele.getKey()))
			{
				out = out.replace(ele.getKey(), ele.getValue());
			}
		}
		return out;
	}
	private static String filterObfuscatedText(String text)
	{
		if (text.trim().equalsIgnoreCase("")) {
			return text;
		}
		
		String out = text;
		for (Entry<String, String> ele : ObfuscateNames.entrySet()) {
			if (out.contains(ele.getValue()))
			{
				out = out.replace(ele.getValue(), ele.getKey());
			}
		}
		return out;
	}
	
	private static boolean begin(String className)
	{
		if (AddNamePairs())
		{
			log("Beginning weapon class read and compile.");
			log(className);
			List<String> classToOutput = new ArrayList<String>();
			String classPath = className.replace(".", "\\");
			try {
				classPath = Paths.get(workingDirectory, "src", classPath+".java").toString();
				log(classPath);
				List<String> lines = readSmallTextFile(classPath);
				String replaceClassName = null;
				String newClassName = null;
				final String TEXTpublicClass = "public class ";
				final String TEXTextends = " extends ";
				final String TEXTtemplatePackage = "*TemplatePackageHere*";
				final String TEXTtemplateClassName = "*TemplateClassNameHere*";
				
				for (int i = 0; i < lines.size(); i++) {
					String line = lines.get(i);
					
					//once class body found, start copying target class in while replacing non-obfuscated definitions
					if (replaceClassName != null)
					{
						classToOutput.add(filterNonObfuscatedText(line).replace("public "+replaceClassName+"()", "public "+newClassName+"()"));
					}
					
					//the line when class definition is performed
					if (replaceClassName == null && line.contains(TEXTpublicClass)) {
						replaceClassName = line.substring(line.indexOf(TEXTpublicClass) + TEXTpublicClass.length(), line.indexOf(TEXTextends)).trim();
						if (replaceClassName.equalsIgnoreCase("")) {
							log("Error: Could not locate class name on line "+i);
							return false;
						}
						
						//build the header on the output class
						try {
							List<String> Templines = readSmallTextFile(FILE_TemplateClassHeader);
							int packageDotClass = className.lastIndexOf(".");
							if (packageDotClass < 1) {
								log("Error: packageDotClass < 1");
								return false;
							}
							
							newClassName = className.substring(packageDotClass+1);
							
							//replace package and class name definitions
							for (String templine : Templines) {
								classToOutput.add(templine.replace(TEXTtemplatePackage, className.substring(0, packageDotClass)).replace(TEXTtemplateClassName, newClassName));
							}
							if (line.trim().endsWith("{"))
							{
								classToOutput.add("{");
							}
							
						} catch (IOException e) {
							log("Reading & modifying Template Header Class at "+FILE_TemplateClassHeader+" failed!");
							e.printStackTrace();
							return false;
						}
						
					}
					
				}
			} catch (IOException e) {
				log("Reading "+className+" failed!");
				e.printStackTrace();
				return false;
			}
			
			//Remove last } and anything afterward as it would be useless in code anyway.
			for (int j = classToOutput.size()-1; j > 0; j--) {
				if (classToOutput.get(j).contains("}"))
				{
					String endline = classToOutput.get(j);
					if (endline.trim().equalsIgnoreCase("}")) {
						classToOutput.remove(j);
						break;
					}
					int bracket = endline.lastIndexOf("}");
					classToOutput.remove(j);
					classToOutput.add(j, endline.substring(0, bracket));
				}
				else
				{
					classToOutput.remove(j);
				}
			}
			
			try {
				classToOutput.addAll(readSmallTextFile(FILE_TemplateClassFooter));
			} catch (IOException e) {
				log("Reading "+FILE_TemplateClassFooter+" failed!");
				e.printStackTrace();
				return false;
			}
			
			//Remove lines that contain elements in the RemoveList
			try {
				List<String> removelist = readSmallTextFile(FILE_RemoveList);
				for (int k = removelist.size()-1; k > 0; k--) {
					if (removelist.get(k).trim().equalsIgnoreCase("") || removelist.get(k).trim().startsWith("#"))
					{
						removelist.remove(k);
					}
				}
				
				for (int k = classToOutput.size()-1; k > 0; k--) {
					if (classToOutput.get(k).trim().equalsIgnoreCase("")) {
						continue;
					}
					for (String part : removelist) {
						if (classToOutput.get(k).contains(part))
						{
							classToOutput.remove(k);
						}
					}
				}
			} catch (IOException e) {
				log("Reading "+FILE_TemplateClassFooter+" failed!");
				e.printStackTrace();
				return false;
			}
			
			try {
				writeSmallTextFile(classToOutput, classPath);
			} catch (IOException e) {
				log("Failed to write to original class file!");
				e.printStackTrace();
			}
			
			//the end!
			
			String cmdCompile = "javac -cp \""+Paths.get(workingDirectory, "src").toString()+"\" \""+classPath+"\"";
			log("Compiling Class:");
			log(cmdCompile);
			try {
				Process p = Runtime.getRuntime().exec(cmdCompile);
				BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line;
				while ((line = input.readLine()) != null) {
					System.out.println(line);
				}
				input.close();
			} catch (IOException e) {
				log("ERROR: Failed to execute Java compile command!");
				e.printStackTrace();
				return false;
			}
			
			File out = new File(classPath.replace(".java", ".class"));
			if (out.exists())
			{
				log("Compile appears to have succeeded!");
				return true;
			}
			log("Compile Failed!");
		}
		return false;
	}

}
