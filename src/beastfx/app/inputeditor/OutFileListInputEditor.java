package beastfx.app.inputeditor;

import beastfx.app.inputeditor.BeautiDoc;
import beast.app.util.TreeFile;

public class OutFileListInputEditor extends FileListInputEditor {
	private static final long serialVersionUID = 1L;

	public OutFileListInputEditor(BeautiDoc doc) {
		super(doc);
	}
	
    public OutFileListInputEditor() {
		super();
	}

	@Override
    public Class<?> baseType() {
		return TreeFile.class;
    }
}
