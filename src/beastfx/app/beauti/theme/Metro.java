package beastfx.app.beauti.theme;

import beastfx.app.beauti.ThemeProvider;
import javafx.scene.Scene;

public class Metro extends ThemeProvider {
	public Metro() {}
	public String getThemeName() {return "Metro";}
	public boolean loadMyStyleSheet(Scene scene) {return ThemeProvider.loadStyleSheet(scene, "/BeastFX/themes/metro.css");}
}