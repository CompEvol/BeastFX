package beastfx.app.treeannotator.services;


import java.io.IOException;
import java.io.PrintStream;

import beast.base.core.Description;
import beast.base.evolution.tree.Tree;
import beastfx.app.treeannotator.TreeAnnotator2;
import beastfx.app.treeannotator.TreeAnnotator2.TreeSet;

@Description("Service for setting topology when running TreeAnnotator")
public interface TopologySettingService {
	
	/** set node heights for tree to be annotated **/
	Tree setTopology(TreeSet trees, PrintStream progressStream, TreeAnnotator2 annotator) throws IOException;
	
	/** return service name for use in TreeAnnotator interface **/
	String getServiceName();

	String getDescription();
}
