package beastfx.app.inputeditor;

import beastfx.app.inputeditor.BeautiDoc;
import beast.app.util.TreeFile;

public class TreeFileListInputEditor extends FileListInputEditor {
	private static final long serialVersionUID = 1L;

	public TreeFileListInputEditor(BeautiDoc doc) {
		super(doc);
	}
	
    public TreeFileListInputEditor() {
		super();
	}

	@Override
    public Class<?> baseType() {
		return TreeFile.class;
    }
}
