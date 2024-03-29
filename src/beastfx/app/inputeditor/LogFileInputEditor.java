package beastfx.app.inputeditor;



import java.io.File;

import beastfx.app.util.LogFile;
import beast.base.core.BEASTInterface;
import beast.base.core.Input;

public class LogFileInputEditor extends FileInputEditor {

	@Override
	public Class<?> type() {
		return LogFile.class;
	}

	public LogFileInputEditor(BeautiDoc doc) {
		super(doc);
	}

	public LogFileInputEditor() {
		super();
	}

	@Override
	public void init(Input<?> input, BEASTInterface plugin, int itemNr, ExpandOption bExpandOption, boolean bAddButtons) {
		init(input, plugin, itemNr, bExpandOption, bAddButtons, "trace files", "log");
	}

	protected File newFile(File file) {
		return new LogFile(file.getPath());
	}

}
