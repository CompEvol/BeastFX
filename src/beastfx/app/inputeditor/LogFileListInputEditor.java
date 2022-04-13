package beastfx.app.inputeditor;

import beastfx.app.inputeditor.BeautiDoc;
import beast.app.util.TreeFile;

public class LogFileListInputEditor extends FileListInputEditor {
	private static final long serialVersionUID = 1L;

	public LogFileListInputEditor(BeautiDoc doc) {
		super(doc);
	}
	
    public LogFileListInputEditor() {
		super();
	}

	@Override
    public Class<?> baseType() {
		return TreeFile.class;
    }
}
