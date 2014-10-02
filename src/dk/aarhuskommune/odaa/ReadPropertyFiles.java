package dk.aarhuskommune.odaa;

import java.util.Hashtable;
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ReadPropertyFiles {
	private String filename = "DKAarhuskommuneODAA.ini";
	private String path = System.getProperty("user.dir");
	private Hashtable<String, String> properties = null;

	public ReadPropertyFiles(String pathFilename) {
		super();
		if (pathFilename!=null) {
			File file = new File(pathFilename);
			this.filename=file.getName();			
			String absolutePath = file.getAbsolutePath();
			this.path = absolutePath.substring(0,absolutePath.lastIndexOf(File.separator));
		}
	}

	public ReadPropertyFiles(String path, String filename) {
		super();
		this.path = path;
		this.filename=filename;
	}

	public Hashtable<String, String> getProperties(Properties p) {		
		for (final String name : p.stringPropertyNames())
			properties.put(name, p.getProperty(name));
		return properties;
	} // getProperties

	public String getParameter(String parameter) {
		if (properties == null) {
			Properties p = new Properties();
			FileInputStream input = null;
			try {
				input = new FileInputStream(path + "//" + filename);
				p.load(input);
				properties = new Hashtable<String, String>();
				properties = getProperties(p);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} finally {
				if (input != null) { 
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}		
		return properties.get(parameter);
	} // getParameter

// GET and SET
	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public ReadPropertyFiles() {
		super();
	}
}
