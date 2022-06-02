package beastfx.app.methodsection;


import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.*;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import beast.base.core.BEASTInterface;
import beast.base.core.Input;
import beast.base.inference.StateNode;
import beast.base.inference.parameter.Parameter;
import beast.base.inference.parameter.RealParameter;
import beast.base.util.Randomizer;
import beastfx.app.inputeditor.BeautiDoc;
import beastfx.app.inputeditor.BeautiSubTemplate;
import beastfx.app.inputeditor.InputEditorFactory;

/** Contains information about a word or phrase in the MethodsText,
 * including a pointer to where the phrase came from 
 */
public class Phrase {
	public enum PhraseType{regular, reference, tipdates};
	
	/** object being described **/
	Object source;
		
	/** input associated with the source **/
	Input<?> input;
	
	/** parent object being described **/
	BEASTInterface parent;

	/** text description of the object **/
	String text;
	
	/** differentiates types to allow different CSS styles **/
	PhraseType type = PhraseType.regular;

	public Phrase(Object source, BEASTInterface parent, Input<?> parameter, String phrase) {
		this.source = source;
		this.parent = parent;
		this.input = parameter;
		this.text = phrase;
		
		if (!MethodsText.partitionGroupMap.containsKey(source)) {
			MethodsText.partitionGroupMap.put(source, new LinkedHashSet<>());
		}
		MethodsText.partitionGroupMap.get(source).add(this);
	}

	
	public Phrase(Object source, String phrase) {
		this(source, null, null, phrase);
	}

	public Phrase(String phrase) {
		this(null, phrase);
	}
	
	public Phrase(String phrase, PhraseType type) {
		this(null, phrase);
		this.type = type;
	}

	static String toSimpleString(List<Phrase> phrases) {
		StringBuilder b = new StringBuilder();
		for (Phrase phrase : phrases) {
			b.append(phrase.toString());
			if (b.length() > 0 && b.charAt(b.length() -1 ) != ' ') {
				b.append(' ');
			}
		}
		return b.toString();
	}

	static String toString(List<Phrase> phrases) {
		List<Phrase> [] ps = new List[1];
		ps[0] = phrases;
		return toString(ps);
	}

	/** convert list of parallel phrases (which produce the same text with toString(List<Phrase>)
	 * to text
	 */
	static String toString(List<Phrase> [] phrases) {
		StringBuilder b = new StringBuilder();
		List<Phrase> basePhrases = phrases[0];
		for (int i = 0; i < basePhrases.size(); i++) {
			Phrase phrase = basePhrases.get(i);
			b.append(phrase.toString());
			if (phrase.source instanceof StateNode && ((StateNode) phrase.source).isEstimatedInput.get()) {
				Set<String> ids = new LinkedHashSet<>();
				for (int j = 0; j < phrases.length; j++) {
					String id = ((StateNode)phrases[j].get(i).source).getID();
					id = BeautiDoc.parsePartition(id);
					ids.add(id);
				}
				List<String> ids2 = new ArrayList<>();
				ids2.addAll(ids);
				b.append("(" + XML2TextPane.printParitions(ids2).trim() +")");					
			}
		}

		String text = b.toString();
		// clean up
		text = text.replaceAll("  ", " ");
		text = text.replaceAll("\n\n", "\n");
		text = text.replaceAll("\\s([\\.,\\)\\}\\]])", "$1");
		text = text.replaceAll("([\\[\\{\\(])\\s", "$1");
		for (char c : new char[]{'a','e','i','o','u'}) {
			text = text.replaceAll(" a " + c, " an " + c);					
		}
		return text;
	}

	static void addTextToDocument(StyledDocument doc, ActionListener al, BeautiDoc beautiDoc, List<Phrase> m) {
        List<Phrase> [] phrases = new List[1];
    	phrases[0] = m;
    	addTextToDocument(doc, al, beautiDoc, phrases);
	}
	
	static void addTextToDocument(StyledDocument doc, ActionListener al, BeautiDoc beautiDoc, List<Phrase> [] phrases) {
        List<String> textString = new ArrayList<>();
        List<String> styleString = new ArrayList<>();

        //Initialize some styles.
        Style def = StyleContext.getDefaultStyleContext().
                        getStyle(StyleContext.DEFAULT_STYLE);

        Style regular = doc.addStyle("regular", def);
        StyleConstants.setFontFamily(def, "SansSerif");
        StyleConstants.setFontSize(def, 16);
        StyleConstants.setSubscript(def, true);
        StyleConstants.setAlignment(def, StyleConstants.ALIGN_RIGHT);

		List<Phrase> basePhrases = phrases[0];
		for (int i = 0; i < basePhrases.size(); i++) {
			Phrase phrase = basePhrases.get(i);

			if (phrase instanceof PartitionPhrase) {
		        JButton button = new JButton(
		        		"<html><font color=\"blue\"><u>" + phrase.toString() + "</u></font></html>"
		        		);
		        button.setBorder(BorderFactory.createEmptyBorder());
		        String partitionStyle = "partitionPhrase " + Randomizer.nextInt();
		        Style s = doc.addStyle(partitionStyle, regular);
		        StyleConstants.setAlignment(s, StyleConstants.ALIGN_LEFT);

		        button.setCursor(Cursor.getDefaultCursor());
		        button.setActionCommand("PartitionEditor");
		        button.addActionListener(al);
		        //button.setMaximumSize(new Dimension(phrase.toString().length() * 10, 20));
		        StyleConstants.setComponent(s, button);
		        
		        textString.add(phrase.toString());
				styleString.add(partitionStyle);

			} else if (phrase instanceof CitationPhrase) {
		        JButton button = new JButton(
		        		"<html><font color=\"blue\"><u><sup>[" + ((CitationPhrase)phrase).counter + "]</sup></u></font></html>"
		        		);
		        button.setBorder(BorderFactory.createEmptyBorder());
		        String partitionStyle = "citationPhrase " + Randomizer.nextInt();
		        Style s = doc.addStyle(partitionStyle, regular);
		        StyleConstants.setAlignment(s, StyleConstants.ALIGN_LEFT);

		        button.setCursor(Cursor.getDefaultCursor());
		        try {
					button.setActionCommand("CitationPhrase " + ((CitationPhrase)phrase).counter);
		        	String ref = ((CitationPhrase)phrase).toReference();
					button.setToolTipText(ref);
				} catch (Exception e) {
					e.printStackTrace();
				}
		        button.addActionListener(al);
		        //button.setMaximumSize(new Dimension(phrase.toString().length() * 10, 20));
		        StyleConstants.setComponent(s, button);
		        
		        textString.add(phrase.toString());
				styleString.add(partitionStyle);
				
			} else if (phrase.source instanceof RealParameter) {
		        JButton button = new JButton(
		        		"<html><font color=\"blue\"><u>" + phrase.toString() + "</u></font></html>"
		        		);
		        button.setBorder(BorderFactory.createEmptyBorder());
		        String partitionStyle = ((BEASTInterface) phrase.source).getID();
		        Style s = doc.addStyle(partitionStyle, regular);
		        StyleConstants.setAlignment(s, StyleConstants.ALIGN_LEFT);

		        button.setCursor(Cursor.getDefaultCursor());
		        button.setActionCommand("RealParameter " + partitionStyle);
		        button.addActionListener(al);
		        //button.setMaximumSize(new Dimension(phrase.toString().length() * 10, 20));
		        StyleConstants.setComponent(s, button);
		        
		        textString.add(phrase.toString());
				styleString.add(partitionStyle);

			} else if (phrase.parent != null && phrase.parent instanceof Parameter<?> && phrase.input.getName().equals("value")) {
				// beautiDoc.registerPlugin((BEASTInterface) phrase.parent);
				String entryStyle = phrase.parent.getID() + " " + phrase.input.getName();
		        Style s = doc.addStyle(entryStyle, regular);
		        StyleConstants.setAlignment(s, StyleConstants.ALIGN_LEFT);
		        String text = phrase.source.toString();
		        text = text.substring(1, text.length() - 1);
		        final JTextField entry = new JTextField(text);
		        //entry.setBorder(BorderFactory.createEmptyBorder());
		        entry.setForeground(Color.BLUE);
		        entry.setCursor(Cursor.getDefaultCursor());
		        entry.setActionCommand(phrase.parent.getID() + " " + phrase.input.getName());
		        entry.addActionListener(al);
		        //entry.setMaximumSize(new Dimension(80,80));
		        entry.getDocument().addDocumentListener(new DocumentListener() {
		            @Override
		            public void removeUpdate(DocumentEvent e) {
		            	entry.postActionEvent();
		            }

					@Override
		            public void insertUpdate(DocumentEvent e) {
		            	entry.postActionEvent();
		            }

		            @Override
		            public void changedUpdate(DocumentEvent e) {
		            	entry.postActionEvent();
		            }
		        });
		        StyleConstants.setComponent(s, entry);	
	
		        textString.add(" ");
				styleString.add(entryStyle);
			} else if (phrase.source instanceof BEASTInterface && phrase.input != null && phrase.parent != null) {
				// beautiDoc.registerPlugin((BEASTInterface) phrase.source);
		        InputEditorFactory inputEditorFactory = beautiDoc.getInputEditorFactory();
		        List<BeautiSubTemplate> plugins = inputEditorFactory.getAvailableTemplates(phrase.input, phrase.parent, null, beautiDoc);
		        if (plugins.size() > 0) {
			        JComboBox<Object> combobox = new JComboBox<>(plugins.toArray());
			        combobox.setBorder(BorderFactory.createEmptyBorder());
			        Style s = doc.addStyle(phrase.parent.getID() + " " + phrase.input.getName(), regular);
			        StyleConstants.setAlignment(s, StyleConstants.ALIGN_LEFT);

			        String id = ((BEASTInterface)phrase.source).getID();
                    if (id != null && id.indexOf('.') != -1) {
                    	id = id.substring(0,  id.indexOf('.'));
                    }
                    boolean isSelected = false;
                    for (int k = 0; k < combobox.getItemCount(); k++) {
                        BeautiSubTemplate template = (BeautiSubTemplate) combobox.getItemAt(k);
                        if (template.getMainID().replaceAll(".\\$\\(n\\)", "").equals(id) ||
                        		template.getMainID().replaceAll(".s:\\$\\(n\\)", "").equals(id) || 
                        		template.getMainID().replaceAll(".c:\\$\\(n\\)", "").equals(id) || 
                        		template.getMainID().replaceAll(".t:\\$\\(n\\)", "").equals(id) ||
                        		(template.getShortClassName() != null && template.getShortClassName().equals(id))) {
                        	combobox.setSelectedItem(template);
                        	//combobox.setMaximumSize(new Dimension(20 * 8 + 15, 200));
                        	isSelected = true;
                        }
                    }
			        combobox.setCursor(Cursor.getDefaultCursor());
			        combobox.setActionCommand(phrase.parent.getID() + " " + phrase.input.getName());
			        combobox.addActionListener(al);
	
			        if (isSelected) {
				        StyleConstants.setComponent(s, combobox);	
			        	textString.add(" ");
			        	styleString.add(phrase.parent.getID() + " " + phrase.input.getName());
			        } else {
						textString.add(phrase.text);
						styleString.add("regular");
			        }
		        } else {
					textString.add(phrase.text);
					styleString.add("regular");
		        }
			} else {
				textString.add(phrase.text);
				styleString.add("regular");
			}
		}	

		try {
            for (int i=0; i < textString.size(); i++) {
                doc.insertString(doc.getLength(), textString.get(i), doc.getStyle(styleString.get(i)));
            }
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert initial text into text pane.");
        }
    }
	
	
	public static String toText(BeautiDoc beautiDoc, List<Phrase> basePhrases) {
		StringBuilder b = new StringBuilder();
		for (Phrase p : basePhrases) {
			b.append(p.toString());
		}
		return b.toString();
	}
	
	public static String toHTML(BeautiDoc beautiDoc, List<Phrase> basePhrases) {
		
		StringBuilder b = new StringBuilder();
		
		PhraseType phraseType = null;
		for (int i = 0; i < basePhrases.size(); i++) {
			Phrase phrase = basePhrases.get(i);
			
			if (i > 0) {
				if (phrase.getType() != phraseType) {
					if (phraseType == PhraseType.reference) {
						b.append("</div>\n"); 
					} else {
						b.append("</span>\n"); 
					}
					if (phrase.getType() == PhraseType.reference) {
						b.append("<div class='"+ phrase.getType()+"'>");
					} else {
						b.append("<span class='"+ phrase.getType()+"'>");
					}
				}
			} else {
				b.append("<div class='"+ phrase.getType()+"'>");
			}
			phraseType = phrase.getType();

			if (phrase instanceof PartitionPhrase) {				
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
				b.append(" <a class='para' href='/cmd=RealParameter id=" + ((BEASTInterface) phrase.source).getID()+ "'>" + phrase.toString() + "</a>");

			} else if (phrase.parent != null && phrase.parent instanceof Parameter<?> && phrase.input.getName().equals("value")) {
				String source = phrase.parent.getID() + " " + phrase.input.getName();
		        String text = phrase.source.toString();
		        text = text.substring(1, text.length() - 1);
				//b.append("<input size='5' onkeyup='window.location=\"/cmd=Text value=\"+value+\" source=" + source +"' value='" + text +"'/>");
				b.append("<input size='5' onkeyup='window.myObject.doIt(value,\"" + source + "\")' value='" + text +"'/>");

			} else if (phrase.parent != null && phrase.parent instanceof BEASTInterface && phrase.input.getType() == Double.class) {
				String source = phrase.parent.getID() + " " + phrase.input.getName();
		        String text = phrase.source.toString();
				//b.append("<input size='5' onkeyup='window.location=\"/cmd=Text value=\"+value+\" source=" + source +"' value='" + text +"'/>");
				b.append("<input size='5' onkeyup='window.myObject.doIt(value,\"" + source + "\")' value='" + text +"'/>");

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
		b.append("</div>");
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
		return b2.toString();
    }
	

	
	@Override
	public String toString() {
		return text;
	}

	public String toHTML() {
		return text.replaceAll("\n", "\n<p>");
	}

	public void setInputX(BEASTInterface parent, Input<?> input) {
		this.parent = parent;
		this.input = input;		
	}


	public void setText(String text) {
		this.text = text;		
	}


	public void setType(PhraseType type) {
		this.type = type;
	}

	public PhraseType getType() {
		return type;
	}
}
