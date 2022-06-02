package beastfx.app.methodsection;



import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintStream;
import java.util.*;

import javax.swing.*;
import javax.swing.text.StyledDocument;

import beastfx.app.inputeditor.BeautiConfig;
import beastfx.app.inputeditor.BeautiDoc;
import beastfx.app.inputeditor.BeautiSubTemplate;
import beastfx.app.methodsection.implementation.BEASTObjectMethodsText;
import beast.base.core.BEASTInterface;
import beast.base.core.Description;
import beast.base.parser.XMLParser;
import beast.base.inference.MCMC;

@Description("Convert MCMC analysis in XML file to a methods section")
public class XML2TextPane extends JTextPane implements ActionListener {
	private static final long serialVersionUID = 1L;

	BeautiDoc beautiDoc;
	String text;
	
	List<Phrase> m;
	
	XML2Text xml2textProducer;
	
	public XML2TextPane(String [] args) throws Exception {
		beautiDoc = new BeautiDoc();
		File file = new File(args[0]);
		beautiDoc.setFileName(file.getAbsolutePath());
		beautiDoc.beautiConfig = new BeautiConfig();
		beautiDoc.beautiConfig.initAndValidate();		
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
		MCMC mcmc = (MCMC) parser.parseFile(file);
		beautiDoc.mcmc.setValue(mcmc, beautiDoc);
		for (BEASTInterface o : InputFilter.getDocumentObjects(beautiDoc.mcmc.get())) {
			beautiDoc.registerPlugin(o);
		}
		beautiDoc.determinePartitions();
		BEASTObjectMethodsText.setBeautiCFG(beautiDoc.beautiConfig);
		
		MethodsText.initNameMap();
		initialise((MCMC) beautiDoc.mcmc.get());
		
		
		if (args.length > 1) {
			PrintStream out = new PrintStream(args[1]);
			out.print(text);
			out.close();
			System.exit(0);
		}
	}
	
	
	private void refreshText()  throws Exception {
//		XMLProducer p = new XMLProducer();
//		String xml = p.toXML(beautiDoc.mcmc.get());
//		PrintStream ps = new PrintStream(new File("/tmp/beast-raw.xml"));
//		ps.println(xml);
//		ps.close();
//		
//		beautiDoc.save("/tmp/beast.xml");		
		beautiDoc.determinePartitions();
		beautiDoc.scrubAll(false, false);
		CitationPhrase.citations.clear();

		StyledDocument doc = getStyledDocument();
		doc.remove(0, doc.getLength());
		MethodsText.clear();
		text = xml2textProducer.initialise((MCMC) beautiDoc.mcmc.get());
		m = xml2textProducer.getPhrases();
        Phrase.addTextToDocument(getStyledDocument(), this, beautiDoc, m);
	}
	
	public void initialise(MCMC mcmc) throws Exception {		
		xml2textProducer = new XML2Text(beautiDoc);
		text = xml2textProducer.initialise((MCMC) beautiDoc.mcmc.get());
		m = xml2textProducer.getPhrases();
        Phrase.addTextToDocument(getStyledDocument(), this, beautiDoc, m);
	}

	@Override
    public void actionPerformed(ActionEvent e) {
		ModelEditor me = new ModelEditor(true);
		boolean refresh = false;
		
    	if (e.getSource() instanceof JButton) {
    		String cmd = e.getActionCommand();
    		if (cmd.startsWith("PartitionEditor")) {
    			cmd="cmd=PartitionEditor";
    		} else if (cmd.startsWith("RealParameter")) {
    			String id = cmd.split(" ")[1];
    			cmd="cmd=RealParameter id="+id;
    		} else if (cmd.startsWith("CitationPhrase")) {
    			cmd="cmd=CitationPhrase counter=" + cmd.substring(cmd.indexOf(' ') + 1);;
    		}
    		refresh = me.handleCmd(cmd, beautiDoc, this);
    	} if (e.getSource() instanceof JComboBox) {
    		JComboBox<String> b = (JComboBox<String>) e.getSource();
    		String cmd = e.getActionCommand();
    		int k = cmd.lastIndexOf(' ');
    		String pid = cmd.substring(0, k);
    		String inputName = cmd.substring(k + 1);    		
            BeautiSubTemplate selected = (BeautiSubTemplate) b.getSelectedItem();
            refresh = me.handleCmd("cmd=Select value=\"" + selected.toString()+"\" source=" + pid + " input=" + inputName, beautiDoc, this);
    	} else if (e.getSource() instanceof JTextField) {
    		JTextField b = (JTextField) e.getSource();
    		String cmd = e.getActionCommand();
    		int k = cmd.lastIndexOf(' ');
    		String id = cmd.substring(0, k);
    		String inputName = cmd.substring(k + 1);
    		String value = b.getText();
    		refresh = me.handleCmd("cmd=Text id=" + id + " value=\"" + value + "\" input=" + inputName, beautiDoc, this);
    	}
    	
    	if (refresh) {
    		try {
				refreshText();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
    	}
    }

	static String printParitions(List<String> partitionIDs, int totalPartitionCount) {
		StringBuilder b = new StringBuilder();
		if (partitionIDs.size() == totalPartitionCount) {
			if (partitionIDs.size() == 2) {
				b.append("both partitions ");
			} else {
				b.append("all partitions ");
			}
		} else {
	    	for (int j = 0; j < partitionIDs.size() - 1; j++) {
	    		b.append(partitionIDs.get(j));
	    		if (j < partitionIDs.size() - 2) {
	    			b.append(", ");
	    		} else {
	    			b.append(" and ");
	    		}
	    	}
	    	b.append(partitionIDs.get(partitionIDs.size() - 1) + " ");
		}
		return b.toString();
	}


	static String printParitions(List<String> partitionIDs) {
		return XML2TextPane.printParitions(partitionIDs, -1);	
	}

	public static void main(String[] args) throws Exception {
		if (System.getProperty("beasy.style") != null) {
			String style = System.getProperty("beasy.style");
			CitationPhrase.CitationMode = CitationPhrase.mode.valueOf(style);
		}

		final XML2TextPane textPane = new XML2TextPane(args);
        
        JScrollPane paneScrollPane = new JScrollPane(textPane);
        paneScrollPane.setVerticalScrollBarPolicy(
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        paneScrollPane.setPreferredSize(new Dimension(650, 455));
        paneScrollPane.setMinimumSize(new Dimension(10, 10));

        //Create and set up the window.
        JFrame frame = new JFrame("XML2TextPane");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.add(paneScrollPane, BorderLayout.CENTER);
        JButton copyButton = new JButton("Copy text to clipboard");
        frame.add(copyButton, BorderLayout.SOUTH);
        copyButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				StringSelection stringSelection = new StringSelection(textPane.text);
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(stringSelection, null);				
			}
		});

        frame.pack();
        frame.setVisible(true);
        
	}
}
