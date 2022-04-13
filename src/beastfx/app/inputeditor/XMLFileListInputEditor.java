package beastfx.app.inputeditor;

import beastfx.app.inputeditor.BeautiDoc;
import beast.app.util.TreeFile;

public class XMLFileListInputEditor extends FileListInputEditor {
	private static final long serialVersionUID = 1L;

	public XMLFileListInputEditor(BeautiDoc doc) {
		super(doc);
	}
	
    public XMLFileListInputEditor() {
		super();
	}

	@Override
    public Class<?> baseType() {
		return TreeFile.class;
    }
}
