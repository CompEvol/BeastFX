package beastfx.app.methodsection;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.core.Log;
import beast.pkgmgmt.Utils6;
import beastfx.app.tools.Application;


@Description("Resolves citation entry for a DOI, e.g. to bibtex format")
public class DOI2Citation extends beast.base.inference.Runnable {
	final public static String DOI2BIB_FILE = "/BeastFX/doi2bib.properties";
	
	public Input<String> doiInput = new Input<>("doi","DOI to be resolved", "http://doi.org/10.1371/journal.pcbi.1003537");
	public Input<String> styleInput = new Input<>("style","style used to format citations, can be 'bibtex' or any of the "
			+ "Citation Style Languages (https://github.com/citation-style-language/styles)", "bibtex");
	
	String bibtex;
	/** bibtex cache **/
	static private Properties DOI2CitationMap;

	public DOI2Citation() {}
	
	/** resolve citation for DOI in style specified by "style" 
	 * style can be 'bibtex' or any of the
	 * Citation Style Languages (https://github.com/citation-style-language/styles)
	 **/
	static String resolve(String DOI, String style) throws Exception {
		DOI2Citation converter = new DOI2Citation();
		try {
			converter.initByName("doi", DOI, "style", style);
			converter.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return converter.bibtex;
	}
	
	/** resolve citation for DOI in bibtex format **/
	static String resolve(String DOI) throws Exception {
		return resolve(DOI, "bibtex");
	}
	
	
	static void initialise() {
		if (DOI2CitationMap == null) {
			DOI2CitationMap = loadMap();
		}
	}

	static public Properties loadMap() {
        File beastProps = new File(Utils6.getPackageUserDir() + DOI2BIB_FILE);
        if (beastProps.exists()) {
            Properties props = new Properties();

            try {
                //load a properties file
                props.load(new FileInputStream(beastProps));
                return props;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return new Properties();
	}
	
	/**
	 * Set property value in beauti.properties file
	 * if value == null, the property will be removed
	 */
	static public void saveMap() {
        File propsFile = new File(Utils6.getPackageUserDir() + DOI2BIB_FILE);

        try {
            propsFile.createNewFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        // Write properties file
        try {
        	DOI2CitationMap.store(new FileOutputStream(propsFile),
                    "Automatically-generated by methods.DOI2Bibtex.\n");
        } catch (IOException ex) {
            Log.err(ex.getMessage());
        }
	}

	@Override
	public void initAndValidate() {
	}

	@Override
	public void run() throws Exception {
		if (true) {
			// http://data.crossref.org/ not functioning any more?
			return;
		}
		initialise();
		
		String DOI = doiInput.get().trim();
		if (!DOI.toLowerCase().startsWith("http")) {
			DOI = "http://citation.crosscite.org/" + DOI;
		}
		if (DOI.toLowerCase().contains("dx.doi.org")) {
			int k = DOI.toLowerCase().indexOf("dx.doi.org");
			DOI = DOI.substring(0,k) + "citation.crosscite.org" + DOI.substring(k + "dx.doi.org".length());
		}
		if (DOI.toLowerCase().contains("doi.org")) {
			int k = DOI.toLowerCase().indexOf("doi.org");
			DOI = DOI.substring(0,k) + "citation.crosscite.org" + DOI.substring(k + "doi.org".length());
		}
		
		// check the cache
		if (DOI2CitationMap.containsKey(styleInput.get() + "/" + DOI)) {
			bibtex = (String) DOI2CitationMap.get(styleInput.get() + "/" + DOI);
			return;
		}
		
		// useful info here: https://citation.crosscite.org/docs.html
		URL url = new URL(DOI);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		//con.setRequestProperty("Accept", "text/bibliography; style=bibtex");
		con.setRequestProperty("Accept", "text/x-bibliography; style=" + styleInput.get());
		con.setRequestProperty("User-Agent", "BEAST 2/2.7 (https://beast2.org/; mailto:r.bouckaert@auckland.ac.nz) BeastFX/2.7");
		con.setDoOutput(true);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String str;
		StringBuffer b = new StringBuffer();
		while ((str = in.readLine()) != null) {
		    b.append(str);
		}
		in.close();
		bibtex = b.toString();
		if (styleInput.get().equals("bibtex")) {
			bibtex = bibtex.replaceFirst(",",",\n");
			bibtex = bibtex.replaceAll("},","},\n");
		}
		con.disconnect();

		DOI2CitationMap.put(styleInput.get() + "/" + DOI, bibtex);		
		saveMap();
		
		Log.info(bibtex);
		Log.warning("\n\n\nDone!");
	}

	public static void main(String[] args) throws Exception {
		new DOI2Citation().run();
		if (true) {return;}
		new Application(new DOI2Citation(), "DOI 2 Citation", args);		
	}
	
}
