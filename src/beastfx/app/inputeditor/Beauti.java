package beastfx.app.inputeditor;


import javax.swing.JFrame;

import apple.laf.JRSUIUtils.TabbedPane;

abstract public class Beauti extends TabbedPane {

	public JFrame frame;
	
    abstract public void autoSetClockRate(boolean flag);
    abstract public void allowLinking(boolean flag);
    abstract public void autoUpdateFixMeanSubstRate(boolean flag);

}
