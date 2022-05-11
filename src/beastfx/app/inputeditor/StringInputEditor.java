package beastfx.app.inputeditor;

public class StringInputEditor extends InputEditor.Base {

    //public StringInputEditor()) {}
    public StringInputEditor(BeautiDoc doc) {
        super(doc);
    }

    public StringInputEditor() {
		super();
	}

	@Override
    public Class<?> type() {
        return String.class;
    }


    @Override
    void setUpEntry() {
        super.setUpEntry();
        //Dimension size = new Dimension(200,20);
        //m_entry.setMinSize(size);
//		m_entry.setPrefSize(size);
//		m_entry.setSize(size);
    }

} // class StringInputEditor
