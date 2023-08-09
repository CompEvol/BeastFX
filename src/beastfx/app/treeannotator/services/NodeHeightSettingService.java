package beastfx.app.treeannotator.services;


import java.io.IOException;
import java.io.PrintStream;

import beast.base.core.Description;
import beast.base.evolution.tree.Node;
import beast.base.evolution.tree.Tree;
import beastfx.app.treeannotator.TreeAnnotator2;

@Description("Service for setting node heights when running TreeAnnotator")
public interface NodeHeightSettingService {
	
	/** set node heights for tree to be annotated **/
	default void setNodeHeights(Tree tree, PrintStream progressStream, TreeAnnotator2 treeAnnotator) throws IOException {};
	
	/** set node heights for single node to be annotated **/
	default void setNodeHeight(Node tree, double [] values, TreeAnnotator2 treeAnnotator) {};
	
	/** return service name for use in TreeAnnotator interface **/
	String getServiceName();
	
	String getDescription();
}
