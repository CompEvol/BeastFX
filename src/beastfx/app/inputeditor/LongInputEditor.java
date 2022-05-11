package beastfx.app.inputeditor;

public class LongInputEditor extends InputEditor.Base {

    public LongInputEditor(BeautiDoc doc) {
        super(doc);
    }
    //public IntegerInputEditor() {}

    public LongInputEditor() {
		super();
	}

	@Override
    public Class<?> type() {
        return Long.class;
    }

} // class LongInputEditor
