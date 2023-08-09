package beastfx.app.treeannotator.services;

import beast.base.evolution.tree.Node;
import beast.base.util.DiscreteStatistics;
import beastfx.app.treeannotator.TreeAnnotator2;

public class MedianNodeHeightService implements NodeHeightSettingService {

	@Override
	public void setNodeHeight(Node node, double[] values, TreeAnnotator2 treeAnnotator) {
        final double median = DiscreteStatistics.median(values);
        if (node.isDirectAncestor()) {
            node.getParent().setHeight(median);
        }
        if (node.isFake() && treeAnnotator.isProcessSA()) {
            node.getDirectAncestorChild().setHeight(median);
        }
        node.setHeight(median);
	}
	
	
	@Override
	public String getServiceName() {
		return "median";
	}


	@Override
	public String getDescription() {
		return "Median heights";
	}

	
	
}
