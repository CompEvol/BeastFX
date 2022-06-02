package beastfx.app.methodsection;


import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import beastfx.app.beauti.Beauti;
import beastfx.app.inputeditor.BeautiConfig;
import beastfx.app.inputeditor.BeautiDoc;
import beast.base.core.BEASTInterface;
import beast.base.inference.MCMC;
import beast.base.inference.Runnable;
import beast.base.core.Log;
import beast.base.parser.XMLParser;
import beastfx.app.methodsection.implementation.BEASTObjectMethodsText;

public class MethodsServer extends Runnable {
	BeautiDoc beautiDoc;
	Beauti beauti;
	String html;
	List<Phrase> m;
	XML2Text xml2textProducer;
	final HTMLProducer htmlProducer = new HTMLProducer();
	
	File tmpFile = null;
	File file = null;

	ModelEditor me = new ModelEditor(true);
	
	public MethodsServer() {
		beautiDoc = new BeautiDoc();
		beautiDoc.beautiConfig = new BeautiConfig();
		beautiDoc.beautiConfig.initAndValidate();
		beauti = new Beauti(beautiDoc);
	}

	public MethodsServer(BeautiDoc doc) {
		beautiDoc = doc;
		beauti = new Beauti(beautiDoc);
	}
	
	
	public final static String header = "<!DOCTYPE html>\n" +
			"<html>\n" +
			"<style>\n" +
			".reference {font-size:10pt;color:#aaa;}\n" +
			".tipdates {display:inline;}\n" +
			"a{color:#555;text-decoration:none;background-color:#fafafa;}\n" + 
			".pe {color:#555;background-color:#fafafa;}\n" + 
			".para {color:#555;background-color:#fafafa;}\n" + 
			"select{color:#555;font-weight:normal;-webkit-appearance:none;background-color:#fafafa;border-width:5pt;}\n" + 
			"a:hover{background-color:#aaa;}\n" + 
			"select:hover{background-color:#aaa;}\n" +
			"</style>\n" +
"  <link rel=\"stylesheet\" href=\"//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css\">\n" + 
"  <link rel=\"stylesheet\" href=\"/resources/demos/style.css\">\n" + 
"  <script src=\"https://code.jquery.com/jquery-1.12.4.js\"></script>\n" + 
"  <script src=\"https://code.jquery.com/ui/1.12.1/jquery-ui.js\"></script>\n" +
"  <script>\n" +
"  $( function() {\n" +
"  $( \".accordion\" ).accordion( {\n" +
"  collapsible: true,\n" +
"      heightStyle: \"content\"\n" +
"  })\n" +
"  } );\n" +
"  </script>\n" +
			"<script>\n" +
			"function edit(cmd) {\n" +
			"	var xhttp = new XMLHttpRequest();\n" +
			"   xhttp.onreadystatechange = function() {\n" +
			"    if (this.readyState == 4 && this.status == 200) {\n" +
			"       // Typical action to be performed when the document is ready:\n" +
			"       document.getElementById('dialogs').innerHTML = xhttp.responseText;\n" +
			"		       $( \"#dialog\" ).dialog({\n" +
			"		    	      modal: true,\n" +
			"		    	      buttons: {\n" +
			"		    	        Ok: function() {\n" +
			"		    	          $( this ).dialog( \"close\" );\n" +
			"		    	        }\n" +
			"		    	      }\n" +
			"		    	    });\n" +
			"    }\n" +
			"   }\n" +
			"	xhttp.open(\"GET\", \"/cmd=Edit id=\" + cmd, true);\n" +
			"	xhttp.send();\n" +
			"};\n" +
			"function doIt(value, cmd) {\n" +
			"	var xhttp = new XMLHttpRequest();\n" +
			"	xhttp.open(\"GET\", \"/cmd=SetValue source=\" + cmd+\" value=\"+value, true);\n" +
			"	xhttp.send();\n" +
			"}\n" +
			"</script>\n"+
			"<body style='font: 12pt arial, sans-serif;margin: 50pt 100pt 50pt 100pt;' >\n"
			// "<div id='accordion'>\n"
			//+ "<input type='button' onclick='window.myObject.doIt(\"ok\");' value='Click me'/>\n"
			;

	public final static String footer = //"</div>\n" + 
			  "<p><a style='font-size:10pt;color:#aaf;' "
			+ "href=\"/cmd=AddPrior\">Add other prior</a>\n"
			+ "<div id=\"dialogs\"></div>\n"
			+ "<p><center><img src='data:image/png;base64," 
				+ ImageUtil.getIcon("methods/beasy.png", "png")+ "'></center>";

    class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
    		Log.warning.println(t.getRequestMethod());
        	String request = t.getRequestURI().toString().replaceAll("%20", " ");
        	Log.warning.println(">>" + request + "<<");
        	String response = null;
        	if (request.startsWith("/cmd=Edit")) {
        		String id = ModelEditor.getAttribute("id", request);
        		BEASTInterface o = beautiDoc.pluginmap.get(id);
        		response = htmlProducer.getHTMLEditor(o, beautiDoc);
System.out.println(response);
        	} else {
        		me.handleCmd(request, beautiDoc, null);
	    		try {
	    			response = processCmd(request);
	    			if (response == null) {
	    				response = "null";
	    			}
	                t.sendResponseHeaders(200, response.length());
	                OutputStream os = t.getResponseBody();
	                os.write(response.getBytes());
	                os.close();
	    		} catch (Exception e) {
	    			e.printStackTrace();
	                t.sendResponseHeaders(200, e.getMessage().length());
	                OutputStream os = t.getResponseBody();
	                os.write(e.getMessage().getBytes());
	                os.close();
	    		}
        	}        	
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }


		private String processCmd(String request) throws Exception {
			XML2Text xml2textProducer = new XML2Text(beautiDoc);
			xml2textProducer.initialise((MCMC) beautiDoc.mcmc.get());
			List<Phrase> m = xml2textProducer.getPhrases();
			
			String html = header + toHTML(m) + footer + "</body>\n</html>";
			
	        FileWriter outfile = new FileWriter("/tmp/index.html");
	        outfile.write(html);
	        outfile.close();
	        return html;
		}

    }

	private String toHTML(List<Phrase> m) {
		return htmlProducer.toHTML(beautiDoc, m);
	}
    
	@Override
	public void initAndValidate() {
	}

	@Override
	public void run() throws Exception {
        HttpServer server = null;
        int port = 8000;
        boolean portFound = false;
        InetSocketAddress add;
        do {
        	try {
        		add = new InetSocketAddress(port);
        		server = HttpServer.create(add, 0);
        		portFound = true;
        	} catch (BindException e) {
        		port++;
        	}
        } while (!portFound || port == 9000);
        if (!portFound) {
        	throw new RuntimeException("Cannot find port available in range 8000 - 9000 -- perhaps there is some security software preventing to acces these ports?");
        }
        server.createContext("/", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        
        Desktop.getDesktop().browse(new URI("http://127.0.0.1:" + port + "/"));
        // server.stop(0);
        
    }

    public static void main(String[] args) throws Exception {
		MCMC mcmc = null;
		MethodsServer server = new MethodsServer();
		BeautiDoc beautiDoc = server.beautiDoc;
		if (args.length > 0) {
			File file = new File(args[0]);
			beautiDoc.setFileName(file.getAbsolutePath());
			String xml = BeautiDoc.load(file);
			int i = xml.indexOf("beautitemplate=");
			if (i > 0) {
				i += 15;
				char c = xml.charAt(i);
				i++;
				int start = i;
				while (xml.charAt(i) != c) {
					i++;
				}
				String template = xml.substring(start, i);
				if (!template.endsWith("xml")) {
					template = template + ".xml";
				}
				beautiDoc.loadNewTemplate(template);
			} else {
				beautiDoc.loadNewTemplate("Standard.xml");
			}
			
			
			XMLParser parser = new XMLParser();
			mcmc = (MCMC) parser.parseFile(file);
		} else {
			mcmc = (MCMC) beautiDoc.mcmc.get();
		}

		beautiDoc.mcmc.setValue(mcmc, beautiDoc);
		for (BEASTInterface o : InputFilter.getDocumentObjects(beautiDoc.mcmc.get())) {
			if (o != null) {
				beautiDoc.registerPlugin(o);
			}
		}
		beautiDoc.determinePartitions();
		BEASTObjectMethodsText.setBeautiCFG(beautiDoc.beautiConfig);
		
		MethodsText.initNameMap();
		server.run();
    	//new Application(server, "Methods server", args);
    }
}

