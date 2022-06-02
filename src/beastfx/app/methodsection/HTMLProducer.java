package beastfx.app.methodsection;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.List;

import beast.base.core.BEASTInterface;
import beast.base.core.Input;
import beast.base.evolution.substitutionmodel.SubstitutionModel;
import beast.base.evolution.tree.TreeIntervals;
import beast.base.inference.StateNode;
import beast.base.inference.parameter.Parameter;
import beast.base.inference.parameter.RealParameter;
import beastfx.app.inputeditor.BeautiDoc;
import beastfx.app.inputeditor.BeautiSubTemplate;
import beastfx.app.inputeditor.InputEditorFactory;
import beastfx.app.methodsection.MethodsText;
import beastfx.app.methodsection.MethodsTextFactory;
import beastfx.app.methodsection.Phrase;
import beastfx.app.methodsection.Phrase.PhraseType;
import beastfx.app.methodsection.objecteditor.ObjectEditor;
import beastfx.app.methodsection.objecteditor.ObjectEditorFactory;

/** generate HTML for MethodsServer for a set of phrases **/
public class HTMLProducer {

	public String toHTML(BeautiDoc beautiDoc, List<Phrase> basePhrases) {
		
		StringBuilder b = new StringBuilder();
		int sectionCount = 0;
		
		for (int i = 0; i < basePhrases.size(); i++) {
			Phrase phrase = basePhrases.get(i);

			if (phrase instanceof SectionPhrase) {
				if (sectionCount > 0) {
					b.append("</div></div></div>\n");
				}
				sectionCount++;
				b.append("<div class='accordion'><h3>" + phrase.toHTML() + "</h3><div><div class='"+ basePhrases.get(i+1).getType() +"'>\n");
			}
			
			if (phrase instanceof SectionPhrase) {
			} else if (phrase instanceof PartitionPhrase) {				
				b.append("<a class='pe' href='/cmd=PartitionEditor'>" + phrase.toHTML() + "</a>\n");
			} else if (phrase.type ==  PhraseType.tipdates) {
				b.append("<a class='pe' href='/cmd=TipDates'>" + phrase.toHTML() + "</a>");
			} else if (phrase instanceof CitationPhrase) {
				int counter = ((CitationPhrase)phrase).counter;
//	        	String ref = "unknown";
//				try {
//					ref = ((CitationPhrase)phrase).toReference();
//				} catch (Exception e) {
//				}
				b.append("<sup><a href='/cmd=CitationPhrase counter=" + counter+ "'>[" + counter + "]</a></sup>");
//						"<div class='tooltip'><sup>"
//						+  +
//						  "<span class='tooltiptext'>" + ref + "</span>" +
//						"</div>\n");
			} else if (phrase.source instanceof RealParameter) {
				b.append(" <a class='para' onclick='edit(\"" + ((BEASTInterface) phrase.source).getID()+ "\")'>" + phrase.toString() + "</a>");

			} else if (phrase.parent != null && phrase.parent instanceof Parameter<?> && phrase.input.getName().equals("value")) {
				String source = phrase.parent.getID() + " " + phrase.input.getName();
		        String text = phrase.source.toString();
		        text = text.substring(1, text.length() - 1);
				//b.append("<input size='5' onkeyup='window.location=\"/cmd=Text value=\"+value+\" source=" + source +"' value='" + text +"'/>");
				b.append("<input size='5' onkeyup='doIt(value,\"" + source + "\")' value='" + text +"'/>");

			} else if (phrase.parent != null && phrase.parent instanceof BEASTInterface && phrase.input.getType() == Double.class) {
				String source = phrase.parent.getID() + " " + phrase.input.getName();
		        String text = phrase.source.toString();
				//b.append("<input size='5' onkeyup='window.location=\"/cmd=Text value=\"+value+\" source=" + source +"' value='" + text +"'/>");
				b.append("<input size='5' onkeyup='doIt(value,\"" + source + "\")' value='" + text +"'/>");

			} else if (phrase.source instanceof BEASTInterface && phrase.input != null && phrase.parent != null) {
		        InputEditorFactory inputEditorFactory = beautiDoc.getInputEditorFactory();
		        List<BeautiSubTemplate> plugins = inputEditorFactory.getAvailableTemplates(phrase.input, phrase.parent, null, beautiDoc);
		        if (plugins.size() > 0) {
		        	StringBuilder b2 = new StringBuilder();

			        String id = ((BEASTInterface)phrase.source).getID();
                    if (id != null && id.indexOf('.') != -1) {
                    	id = id.substring(0,  id.indexOf('.'));
                    }
		        	int width = id.length() * 8;
                    boolean isSelected = false;
                    for (int k = 0; k < plugins.size(); k++) {
                        BeautiSubTemplate template = plugins.get(k);
                        if (template.getMainID().replaceAll(".\\$\\(n\\)", "").equals(id) ||
                        		template.getMainID().replaceAll(".s:\\$\\(n\\)", "").equals(id) || 
                        		template.getMainID().replaceAll(".c:\\$\\(n\\)", "").equals(id) || 
                        		template.getMainID().replaceAll(".t:\\$\\(n\\)", "").equals(id) ||
                        		(template.getShortClassName() != null && template.getShortClassName().equals(id))) {
                        	b2.append("<option selected='true' value='" + template.toString() +"'>" + template.toString() + "</option>\n");
                        	
                        	FontRenderContext frc = new FontRenderContext(new AffineTransform(),true,true);     
                        	Font font = new Font("Arial", Font.PLAIN, 12);
                        	width = 1 + (int)(font.getStringBounds(template.toString(), frc).getWidth());
                        	isSelected = true;
                        } else {
                        	b2.append("<option value='" + template.toString() +"'>" + template.toString() + "</option>\n");
                        }
                    }
                    b2.append("</select>\n");

			        if (isSelected) {
			        	b.append("<select style='width:" + width + "pt;font: 12pt arial, sans-serif;border: 0px solid transparent;' "
			        			+ "onchange='window.location=\"/cmd=Select value=\"+value+\" source="+ phrase.parent.getID() + " input=" + phrase.input.getName() +
			        			" object=" +  ((BEASTInterface)phrase.source).getID() + "\"'>");
						b.append(b2.toString());					
			        } else {
						b.append(phrase.toHTML());					
			        }
		        } else {
					b.append(phrase.toHTML());					
		        }
			} else if (phrase.source != null & phrase.source instanceof BEASTInterface) {
				b.append("<a href='/cmd=EditObject source=" + ((BEASTInterface)phrase.source).getID() + "'>" 
						+ phrase.text + "</a>");				
			} else {
				b.append(phrase.toHTML());
			}
			
		}	
		b.append("</div></div></div>");
		StringBuilder b2 = new StringBuilder();
		for (int k = 0; k < b.length(); k++) {
			int j = b.charAt(k);
			if (j < 128) {
				b2.append((char) j);
			} else {
				b2.append("&#");
				b2.append(j);
				b2.append(';');
			}
		}
		if (sectionCount > 0) {
			b.append("</div>");			
		}
		return b2.toString();
    }


	ObjectEditorFactory objectEditorFactory;
	
	public String getHTMLEditor(BEASTInterface o, BeautiDoc doc) {
		if (objectEditorFactory == null) {
			objectEditorFactory = new ObjectEditorFactory();
			objectEditorFactory.init(doc);
		}
		
		
		StringBuilder html = new StringBuilder();
		html.append("<div id=\"dialog\" title=\"Edit " + o.getID() + "\">\n");
		html.append("<table>\n");
		for (Input<?> input : o.listInputs()) {
			try {
				ObjectEditor editor = objectEditorFactory.getObjectEditor(o, input, doc);
				html.append("<tr>");
				html.append(editor.toHTML(o, input));
				html.append("</tr>\n");
			} catch (Throwable e) {
				// ignore 
			}
		}
		html.append("</table>\n");
		html.append("</div>\n");
		return html.toString();
	}
}
