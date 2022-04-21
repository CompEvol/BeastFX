package beastfx.app.inputeditor;


import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;

abstract public class Beauti extends TabPane {

	public Pane frame;
	
    abstract public void autoSetClockRate(boolean flag);
    abstract public void allowLinking(boolean flag);
    abstract public void autoUpdateFixMeanSubstRate(boolean flag);

}
