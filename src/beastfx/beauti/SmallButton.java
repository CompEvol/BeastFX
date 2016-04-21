package beastfx.beauti;



import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * @author Alexei Drummond
 */
public class SmallButton extends Button {

//	public enum ButtonType {roundRect, square, toolbar}
    
    public SmallButton(String label, boolean isEnabled) {
        super(label);
        String style = 
                "-fx-background-radius: 20em; " +
                "-fx-min-width: 15px; " +
                "-fx-min-height: 15px; " +
                "-fx-max-width: 15px; " +
                "-fx-max-height: 15px; ";
        setStyle(style);

        //setEnabled(isEnabled);
        setIcon(label);
    }

	private void setIcon(String label) {
        if (label.equals("e")) {
        	setText("");
        	Image image = new Image(getClass().getResourceAsStream("edit.png"));
        	setGraphic(new ImageView(image));
        }
	}
    
    public void setImg(Image image) {
    	setGraphic(new ImageView(image));
    }

    public void setToolTipText(String text) {
	    final Tooltip tooltip = new Tooltip();
	    tooltip.setText(text);
    	setTooltip(tooltip);
    }

}
