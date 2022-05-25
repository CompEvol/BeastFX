package beastfx.app.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import beast.app.util.OutFile;
import beast.app.util.Utils;
import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.core.Log;
import beast.base.inference.Logger;
import beast.base.inference.Runnable;
import beast.base.parser.XMLParser;
import beast.base.util.FileUtils;
import beast.pkgmgmt.BEASTClassLoader;
import beast.pkgmgmt.BEASTVersion;
import beast.pkgmgmt.PackageManager;

@Description("Does sanity checks on a BEAST package\n" 
		+ "o make sure all classes implementing services are registered as services\n"
		+ "o make sure folders are in the right place (lib, template, examples)\n"
		+ "o make sure source code is available and matches jar library\n"
		+ "o make sure version.xml is present\n"
		+ "and more...")
public class PackageHealthChecker extends Runnable {
	final public Input<File> packageInput = new Input<>("package", "zip-file containing BEAST package", new File(OutFile.NO_FILE)); 
	final public Input<OutFile> outputInput = new Input<>("output", "output-file where report is stored. Use stdout if not specified.", new OutFile(OutFile.NO_FILE)); 
	
	private String packageName;
	private String packageFileName;
	private String packageDir;
	private Set<String> classesInJar = null;
	private PrintStream out = System.out;
	
	@Override
	public void initAndValidate() {
	}

	@Override
	public void run() throws Exception {
		if (OutFile.isSpecified(outputInput.get())) {
			out = new PrintStream(outputInput.get());
		}
		if (packageInput.get() == null || packageInput.get().getName().equals("[[none]]")) {
			throw new IllegalArgumentException("package zip file must be specified");
		}
		if (!packageInput.get().exists()) {
			throw new IllegalArgumentException("package zip file (" + packageInput.get().getPath() + ") does not exist");
		}
		
		
		// unzip the package file
		packageFileName = packageInput.get().getName();
		if (packageFileName.contains(".")) {
			packageFileName = packageFileName.substring(0, packageFileName.lastIndexOf('.'));
		}
		packageDir = (Utils.isWindows() ? "c:\\temp\\" : "/tmp/") + packageFileName;
		new File(packageDir).mkdir();
		PackageManager.doUnzip(packageInput.get().getPath(), packageDir);
		
		
		// do checks
		String versionFileName = checkVersionFile();
		checkServices(versionFileName);
		checkFolders();
		checkSourceCode();
		checkXMLExample();
		// checkBEAUTITemplates();
		
		// clean up package directory
		deleteRecursively(new File(packageDir));
		if (OutFile.isSpecified(outputInput.get())) {
			out.close();
		}
		Log.warning("Done");
		System.exit(0);
	}
	
	private void checkXMLExample() throws IOException {
		PackageManager.loadExternalJars();
		Logger.FILE_MODE = Logger.LogFileMode.overwrite;
		
		String separator = Utils.isWindows() ? "\\\\" : File.separator;
		List<String> failedFiles = new ArrayList<>(); 
		for (String fileName : new File(packageDir + separator + "examples").list()) {
            Log.warning("Processing " + fileName);
            XMLParser parser = new XMLParser();
            try {
                parser.parseFile(new File(packageDir + separator + "examples" + separator + fileName));
            } catch (Exception e) {
            	e.printStackTrace()
            	;
                out.println("Example Xml parsing failed for " + fileName
                        + ": " + e.getMessage());
                failedFiles.add(fileName);
            }
		}		
	}

	private void checkBEAUTITemplates() {
		String separator = Utils.isWindows() ? "\\\\" : File.separator;
		List<String> failedFiles = new ArrayList<>(); 
		for (String fileName : new File(packageDir + separator + "templates").list()) {
            Log.warning("Processing " + fileName);
            XMLParser parser = new XMLParser();
            try {            	
            	String xml = FileUtils.load(new File(packageDir + separator + "templates" + separator + fileName));
                parser.parseBareFragment(xml, false);
            } catch (Exception e) {
            	e.printStackTrace()
            	;
                out.println("BEAUti template parsing failed for " + fileName
                        + ": " + e.getMessage());
                failedFiles.add(fileName);
            }
		}			}

	private void checkServices(String versionFileName) {
		Map<String, Set<String>> declaredSerices = collectDecladedServices(versionFileName);
		for (String file : new File(packageDir + "/lib").list()) {
			if (file.toLowerCase().endsWith(".jar")) {
				JarHealthChecker jarCheck = new JarHealthChecker(new File(file));
				jarCheck.checkServices(classesInJar, out, declaredSerices);
			}
		}
	}
	
	
	private Map<String, Set<String>> collectDecladedServices(String versionFileName) {
		Map<String,Set<String>> declaredServices = new HashMap<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document doc = factory.newDocumentBuilder().parse(versionFileName);
            declaredServices = PackageManager.parseServices(doc);
        } catch (Exception e) {
            // ignore
            System.err.println(e.getMessage());
        }
		
//		Map<String,Set<String>> declaredServices = new HashMap<>();		
//		for (String file : new File(packageDir + "/lib").list()) {
//			if (file.toLowerCase().endsWith(".jar")) {
//				String destDir = packageDir + "/lib/" + file + "_extracted";
//				new File(destDir).mkdir();
//				try {
//					PackageManager.doUnzip(packageDir + "/lib/" + file, destDir);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				File serviceDir = new File(destDir +"/META-INF/services");
//				if (!serviceDir.exists()) {
//					report("No services found in jar " + file);
//				} else {
//					for (String metaInfFile : serviceDir.list()) {
//						if (!metaInfFile.endsWith("MANIFEST.MF") && !(metaInfFile.charAt(0)=='.')) {
//							if (!declaredServices.containsKey(metaInfFile)) {
//								declaredServices.put(metaInfFile, new HashSet<String>());
//							}
//							try {
//								for (String className : FileUtils.load(metaInfFile).split("\n")) {
//									declaredServices.get(metaInfFile).add(className);
//								}
//							} catch (IOException e) {
//								e.printStackTrace();
//							}
//						}
//					}
//				}
//			}
//		}
		return declaredServices;
	}

	private void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                deleteRecursively(f);
            }
        }
        file.delete();            
    }




	private void checkFolders() {
		report("Checking folder structure");
		boolean hasExamples = false;
		int exampleCount = 0;
		boolean hasLib = false;
		int libCount = 0;
		boolean hasTemplates = false;
		int templateCount = 0;
		
		String [] files = new File(packageDir).list();
		for (String fileName : files) {
			if (fileName.toLowerCase().equals("examples")) {
				hasExamples = true;
				for (String example : new File(packageDir+"/examples").list()) {
					if (example.toLowerCase().endsWith("xml") || example.toLowerCase().endsWith("json")) {
						exampleCount++;
					}
				}
			}
			if (fileName.toLowerCase().equals("lib")) {
				hasLib = true;
				for (String example : new File(packageDir+"/lib").list()) {
					if (example.toLowerCase().endsWith("jar")) {
						libCount++;
					}
				}
			}
			if (fileName.toLowerCase().equals("templates")) {
				hasTemplates = true;
				for (String example : new File(packageDir+"/templates").list()) {
					if (example.toLowerCase().endsWith("xml")) {
						templateCount++;
					}
				}
			}
		}		
		if (!hasExamples) {
			report("No examples directory found. It is recommended to have at least one XML example file "
					+ "showing the features of the package in the examples directory at the top level of the package");
		}
		if (hasExamples && exampleCount == 0) {
			report("No examples in examples directory found. It is recommended to have at least one XML example "
					+ "file showing the features of the package");
		}
		if (!hasLib) {
			report("No lib directory found. It is recommended to have at least one jar file containing java classes "
					+ "in the lib directory at the top level of the package");
		}
		if (hasLib && libCount == 0) {
			report("No jar library found in lib directory.");
		}
		if (!hasTemplates) {
			report("No templates directory found. For BEAUti support of the package, the BEAUti templates are "
					+ "expected to be in the templates directory at the top level of the package");
		}
		if (hasTemplates && templateCount == 0) {
			report("No BEAUti template found in templates directory, so BEAUti will have no support for this package.");
		}
	}

	private void checkSourceCode() {
		report("Checking source code file");
		if (classesInJar == null) {
			collectClasses();
		}
		// collect source file names
		Set<String> sourceClassFiles = new HashSet<>();
		for (String fileName : new File(packageDir).list()) {
			if (fileName.endsWith("jar") || fileName.endsWith("zip")) {
				try {
					ZipInputStream zip = new ZipInputStream(new FileInputStream(packageDir + "/" + fileName));
					while(true) {
					    ZipEntry e = zip.getNextEntry();
					    if (e == null)
					      break;
					    String name = e.getName();
					    if (name.toLowerCase().endsWith("java")) {
					    	sourceClassFiles.add(name);
					    }
					}
					zip.close();
				} catch (IOException e) {
					report(e.getMessage());
				}
			}
		}
		if (sourceClassFiles.size() == 0) {
			report("Source code file expected (jar or zip) at top level as perhaps " + packageName+ ".src.jar, but no source files found");
			return;
		}
		
		// check source file names have associated classes
		for (String sourceFile : sourceClassFiles) {
			String clazz = sourceFile.replaceAll(".java", "").replaceAll("/", ".");
			if (!classesInJar.contains(clazz)) {
				report("Source file " + sourceFile + " in source jar but class file " + clazz + " was not in class jar");
			}
		}

		// check class file names have associated sources
		for (String clazz : classesInJar) {
			if (!clazz.contains("$")) {
				String sourceFile = clazz.replaceAll("\\.", "/") + ".java";
				if (!sourceClassFiles.contains(sourceFile)) {
					report("Class file " + clazz + " in class jar but source file " + sourceFile + " was not in source jar");
				}
			}
		}
	}

	private String checkVersionFile() {
		report("Checking version.xml");
		String versionFileName = packageDir + "/version.xml";
		if (!new File(versionFileName).exists()) {
			report("Expected file version.xml at top level in the zip file");
			// perhaps it is somewhere else in the zip file?
			try {
				ZipInputStream zip = new ZipInputStream(new FileInputStream(packageInput.get()));
				while(true) {
				    ZipEntry e = zip.getNextEntry();
				    if (e == null)
				      break;
				    String name = e.getName();
				    if (name.endsWith("version.xml")) {
				    	report("but found it here: " + name);
				    }
				}
			} catch (IOException e) {
				report(e.getMessage());
			}
			report("Could not find file version.xml where I expected it: " + versionFileName);
			report("Cannot determine package name, so assume it is " + packageFileName);
			packageName = packageFileName;
			return versionFileName;
		}
		
		packageName = determinPackageName(versionFileName);
		
		checkDependenciesInVersionXML(versionFileName);
		checkApplicationsInVersionXML(versionFileName);
		checkMapElementsInVersionXML(versionFileName);
		return versionFileName;
	}
	
	private String determinPackageName(String versionFileName) {
		packageName = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // find name of package
            Document doc = factory.newDocumentBuilder().parse(versionFileName);
            Element packageElement = doc.getDocumentElement();
            if (packageElement.getNodeName().equals("addon")) {
            	report("Deprecated top level element 'addon' found. Use 'package' instead.");
            }
            packageName = packageElement.getAttribute("name");
            String version = packageElement.getAttribute("version");
            if (version == null || version.length() == 0) {
    			report("Excpected version attribute containing package version to be specified on the root element of version.xml");
            } else {
            	if (version.split("\\.").length != 3) {
        			report("The version attribute containing package version on the root element of version.xml does not appear to be in standard <major-version>.<minor-version>.<bug-fix-version>, e.g \"2.7.0\"");
        			report("More info on semantic versioning: https://semver.org/");
            	}
            }
            
            NodeList content = packageElement.getChildNodes();
            for (int i = 0; i < content.getLength(); i++) {
            	Node node = content.item(i);
            	if (node instanceof Element) {
            		String name = ((Element) node).getNodeName();
            		if (!(name.equals("packageapp") || name.equals("map") || name.equals("addonapp") || name.equals("depends") || name.equals("service"))) {
            			report("Unrecognised element found in version.xml, which will be ignored:" + name + " (potentially a typo)");
            		}
            	}
            }
            
        } catch (SAXException e) {
            // too bad, won't print out any info
			report("Excpected  " + versionFileName + " to be an XML file with name attribute containing the package name on the root element");
        } catch (IOException e) {
        	report("Cannot read version.xml file: " + e.getMessage());
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

        if (packageName == null) {
			report("Cannot determine package name, so assume it is " + packageFileName);
			packageName = packageFileName;
        }
		return packageName;
	}

	private void checkDependenciesInVersionXML(String versionFileName) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document doc;
        try {
            doc = factory.newDocumentBuilder().parse(new File(versionFileName));
            doc.normalize();
            // get package-app info from version.xml
            NodeList nodes = doc.getElementsByTagName("depends");
            if (nodes.getLength() == 0) {
            	report("No package dependencies specified in version.xml.");
            	report("At least one dependency of the form <depends on='beast2' version='" + BEASTVersion.INSTANCE.getVersion() + "'/> was expected.");
            	return;
            }
            for (int j = 0; j < nodes.getLength(); j++) {
                Element packageAppElement = (Element) nodes.item(j);
                NamedNodeMap atts = packageAppElement.getAttributes();
                for (int i = 0; i < atts.getLength(); i++) {
                	String name = atts.item(i).getNodeName();
                	if (!(name.equals("on") || name.equals("version")|| name.equals("atleast")|| name.equals("atmost"))) {
                		report("Unrecognised attributes " + name + " found in 'depends' element: use 'on' 'version', 'atleast' or 'atmost'");
                	}
                }
                
                String onName = packageAppElement.getAttribute("on");
                if (onName == null || onName.length() == 0) {
                	report("depends element found, but 'on' attribute is not specified");
                }
                String versionName = packageAppElement.getAttribute("version");
                String versionAtLeast = packageAppElement.getAttribute("atleast");
                String versionAtMost = packageAppElement.getAttribute("atmost");
                if ((versionName == null || versionName.length() == 0) &&
                		(versionAtLeast == null || versionAtLeast.length() == 0)&&
                		(versionAtMost == null || versionAtMost.length() == 0)) {
                	report("depends element found, but 'version', 'atleast' or 'atmost' attribute is not specified");
                } else if (versionAtMost != null && versionAtLeast != null && versionAtMost.length() > 0 && versionAtLeast.length() > 0) {
                	try {
                		if (BEASTVersion.parseVersion(versionAtMost) < BEASTVersion.parseVersion(versionAtLeast)) {
                			report("The atmost attribute must be larger than the atleast attribute, but: " + versionAtLeast + " > " + versionAtMost);                		
                		}
                	} catch (Throwable e) {
                		report("There may be an ill-formatted version in a 'depends' element: "+ versionAtLeast + " or " + versionAtMost);
                	}
                }
            }
        } catch (Exception e) {
            // ignore
            System.err.println(e.getMessage());
        }
	}

	private void checkApplicationsInVersionXML(String versionFileName) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document doc;
        try {
            doc = factory.newDocumentBuilder().parse(new File(versionFileName));
            doc.normalize();
            // get package-app info from version.xml
            NodeList nodes = doc.getElementsByTagName("packageapp");
            if (nodes.getLength() == 0) {
            	nodes = doc.getElementsByTagName("addonapp");
                if (nodes.getLength() != 0) {
                	report("Deprecated element name 'addonapp' used in version.xml. Use 'packageapp' instead");
                }
            }
            if (nodes.getLength() == 0) {
            	report("Observation: No package applications found in version.xml so no apps will be available for the BEAST applauncher");
            	report("More information about applauncher:");
            	report("http://www.beast2.org/2014/08/04/beast-apps-for-the-appstore.html");
            	report("http://www.beast2.org/2019/07/23/better-apps-for-the-beast-appstore.html");
            	return;
            }
            for (int j = 0; j < nodes.getLength(); j++) {
                Element packageAppElement = (Element) nodes.item(j);
                Set<String> recognisedAttributes = new HashSet<>();
                recognisedAttributes.add("class");
                recognisedAttributes.add("description");
                recognisedAttributes.add("args");
                recognisedAttributes.add("icon");
                NamedNodeMap atts = packageAppElement.getAttributes();
                for (int i = 0; i < atts.getLength(); i++) {
                	String name = atts.item(i).getNodeName();
                	if (!recognisedAttributes.contains(name)) {
                		report("Unrecognised attributes " + name + " found: use one of " + Arrays.toString(recognisedAttributes.toArray()));
                	}
                }
                
                String className = packageAppElement.getAttribute("class");
                if (className == null || className.length() == 0) {
                	report("packageapp element found, but class attribute is not specified");
                } else {
                	if (!jarFileContainsClass(className)) {
                    	report("class " + className +" specified in packageapp of version.xml could not be found in lib/" + packageName + ".jar");
                	}
                }
                
                String description = packageAppElement.getAttribute("description");
                if (description == null || description.length() == 0) {
                	report("packageapp element found, but no description provided: please specify the description attribute");
                }
                String argumentsString = packageAppElement.getAttribute("args");

                String iconLocation = packageAppElement.getAttribute("icon");
                if (iconLocation == null || iconLocation.length() == 0) {
                	report("packageapp element found, but no icon provided (no or empty icon attribute) so default icon will be used in BEAST applauncher");
                }
            }
        } catch (Exception e) {
            // ignore
            System.err.println(e.getMessage());
        }
    }
	
	private void checkMapElementsInVersionXML(String versionFileName) {
		try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document doc = factory.newDocumentBuilder().parse(new File(versionFileName));
            doc.normalize();
            NodeList nodes = doc.getElementsByTagName("map");
            for (int i = 0; i < nodes.getLength(); i++) {
                Element map = (Element) nodes.item(i);
                
                NamedNodeMap atts = map.getAttributes();
                for (int j = 0; j < atts.getLength(); j++) {
                	String name = atts.item(i).getNodeName();
                	if (!(name.equals("from") || name.equals("to"))) {
                		report("Unrecognised attributes " + name + " found: must be \"from\" or \"to\"");
                	}
                }
                
                String fromClass = map.getAttribute("from");
                if (fromClass == null || fromClass.length() == 0) {
                	report("The 'from' attribute in map element in version.xml must be specified");
                }
                String toClass = map.getAttribute("to");
                if (toClass == null || toClass.length() == 0) {
                	report("The 'from' attribute in map element in version.xml must be specified");
                }
        		if (!jarFileContainsClass(toClass)) {
                	report("class " + toClass +" specified in map element of version.xml could not be found in lib/" + packageName + ".jar");
            	}
            }
        } catch (ParserConfigurationException|SAXException|IOException e) {
            e.printStackTrace();
        }        		

	}

	private boolean jarFileContainsClass(String toClass) {
		if (classesInJar == null) {
			collectClasses();
		}
		return classesInJar.contains(toClass);
	}
	
	private void collectClasses() {
		classesInJar = new HashSet<>();
		File jardir = new File(packageDir + "/lib");
		if (!jardir.exists()) {
			report("Expected lib directory at top level of zip file containing jar files with classes, but could not find any");
			return;
		}
		for (File file : jardir.listFiles()) {
			if (file.getName().toLowerCase().endsWith("jar")) {
				try {
					BEASTClassLoader.classLoader.addJar(file.getPath());
				} catch (Throwable t) {
					// ignore
				}
				try {
					ZipInputStream zip = new ZipInputStream(new FileInputStream(file));
					while(true) {
						ZipEntry e = zip.getNextEntry();
						if (e == null)
							break;
						String name = e.getName();
						if (name.endsWith("class")) {
							if (classesInJar.contains(name)) {
								report("Class " + name + " has multiple entries in jar files");
							}
							name = name.substring(0, name.length() - 6).replaceAll("/", ".");
							classesInJar.add(name);
						}
					}
					zip.close();
				} catch (IOException e) {
					report(e.getMessage());
				}	
			}
		}
	}

	private void report(String msg) {
		out.println(msg);
	}
	
	public static void main(String[] args) throws Exception {
		new Application(new PackageHealthChecker(), "Package Health Checker", args);
	}

}
