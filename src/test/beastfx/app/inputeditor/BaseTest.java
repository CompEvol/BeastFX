package test.beastfx.app.inputeditor;

import beastfx.app.inputeditor.BEASTObjectInputEditor;
import beastfx.app.inputeditor.BeautiConfig;
import beastfx.app.inputeditor.BeautiDoc;
import beastfx.app.inputeditor.InputEditor;
import beastfx.app.inputeditor.InputEditor.ExpandOption;
import beast.base.core.BEASTObject;
import beast.base.core.Input;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class BaseTest extends javafx.application.Application {
	public enum e {value1,value2,value3};

	public class InputClass2 extends BEASTObject {
		final public Input<InputClass> InputClassInput = new Input<>("InputClass", "Input class", new InputClass());
		@Override
		public void initAndValidate() {}
	}
	public class InputClass extends BEASTObject {
		final public Input<e> eInput = new Input<>("enum", "integer valued input for test class", e.value2, e.values());
		final public Input<Integer> intInput = new Input<>("int", "integer valued input for test class", 3);
		final public Input<Boolean> boolInput = new Input<>("bool", "boolean valued input for test class", true);
		final public Input<Double> doubleInput = new Input<>("double", "double valued input for test class", 3.12);
		final public Input<Long> longInput = new Input<>("long", "long valued input for test class", 123L);
		final public Input<String> strInput = new Input<>("string", "string valued input for test class", "string");
		
		@Override
		public void initAndValidate() {
			System.out.println("Value of int = " + intInput.get());
		}
		
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		// create BeautiDoc and beauti configuration
		BeautiDoc doc = new BeautiDoc();
		doc.beautiConfig = new BeautiConfig();
		doc.beautiConfig.initAndValidate();
	    
		VBox box = new VBox();
		InputClass2 beastObject = new InputClass2();
		beastObject.setID("inputClass2");
		beastObject.InputClassInput.get().setID("inputClass");
		InputEditor e = new BEASTObjectInputEditor(doc);
		e.init(beastObject.InputClassInput, beastObject, -1, ExpandOption.TRUE, true);
		box.getChildren().add((Pane) e);
		
		for (Input<?> input : beastObject.listInputs()) {
			Object o = doc.getInputEditorFactory().createInputEditor(input, beastObject, doc);
			Pane node = (Pane) o;
			box.getChildren().add(node);
		}

		ScrollPane root = new ScrollPane();
        root.setContent(box);
 
        // Set the Style-properties of the VBox
        root.setStyle("-fx-padding: 10;" +
                "-fx-border-style: solid inside;" +
                "-fx-border-width: 2;" +
                "-fx-border-insets: 5;" +
                "-fx-border-radius: 5;" +
                "-fx-border-color: blue;");				    

        
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.show();

		primaryStage.setOnCloseRequest((event) -> {
		    System.exit(0);
		});
	}       

	public static void main(String[] args) {
	    launch();
	}
	
}
