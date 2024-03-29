package beastfx.app.inputeditor;




import java.io.File;

import beastfx.app.util.XMLFile;
import beast.base.core.BEASTInterface;
import beast.base.core.Input;

public class XMLFileInputEditor extends FileInputEditor {

	@Override
	public Class<?> type() {
		return XMLFile.class;
	}

	public XMLFileInputEditor(BeautiDoc doc) {
		super(doc);
	}

	public XMLFileInputEditor() {
		super();
	}

	@Override
	public void init(Input<?> input, BEASTInterface plugin, int itemNr, ExpandOption bExpandOption, boolean bAddButtons) {
		init(input, plugin, itemNr, bExpandOption, bAddButtons, "BEAST XML files", "xml");
	}
	
	protected File newFile(File file) {
		return new XMLFile(file.getPath());
	}

}
