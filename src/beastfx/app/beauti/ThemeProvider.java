package beastfx.app.beauti;

/** returns list of stylesheets **/
/** this is a service: if a package wants to add themes, it can do so by
 * implementing ThemeProvider and add an entry in the version.xml file of the package,
 * e.g. like so:

<service type="beastfx.app.beauti.ThemeProvider">
	<provider classname="mypackage.ThemeProvider"/>
</service>

where the class `mypackage.ThemeProvider` derives from ThemeProvider and implements getThemes()
 */
public class ThemeProvider {

	/*
	 * return semicolon delimited list of files containing stylesheet file locations
	 * with respect to the BEAST package directory
	 */
	public String getThemes() {
    	String themes = "/BeastFX/themes/default.css;/BeastFX/themes/dark.css;/BeastFX/themes/bootstrap.css";
    	return themes;
	}
}
