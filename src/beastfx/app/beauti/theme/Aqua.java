package beastfx.app.beauti.theme;

import com.aquafx_project.AquaFx;

import beast.app.util.Utils;
import beastfx.app.beauti.ThemeProvider;
import javafx.scene.Scene;

public class Aqua extends ThemeProvider {
	public Aqua() {}
	public String getThemeName() {if (Utils.isMac() || System.getProperty("beast.debug")!= null) return "Aqua"; else return "";}
	public boolean loadMyStyleSheet(Scene scene) {
		AquaFx.style();
		return true;
	}
}