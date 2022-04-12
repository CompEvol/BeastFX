package beastfx.app.inputeditor;

import beastfx.app.beauti.BeautiDoc;

public class IntegerInputEditor extends InputEditor.Base {
    private static final long serialVersionUID = 1L;

    public IntegerInputEditor(BeautiDoc doc) {
        super(doc);
    }

    public IntegerInputEditor() {
    	super();
	}

	@Override
    public Class<?> type() {
        return Integer.class;
    }

} // class IntegerInputEditor
