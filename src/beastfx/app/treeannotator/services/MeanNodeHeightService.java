package beastfx.app.treeannotator.services;

import beast.base.evolution.tree.Node;
import beast.base.util.DiscreteStatistics;
import beastfx.app.treeannotator.TreeAnnotator2;

public class MeanNodeHeightService implements NodeHeightSettingService {

	@Override
	public void setNodeHeight(Node node, double[] values, TreeAnnotator2 treeAnnotator) {
        final double mean = DiscreteStatistics.mean(values);
        if (node.isDirectAncestor()) {
            node.getParent().setHeight(mean);
        }
        if (node.isFake() && treeAnnotator.isProcessSA()) {
            node.getDirectAncestorChild().setHeight(mean);
        }
        node.setHeight(mean);
	}
	
	
	@Override
	public String getServiceName() {
		return "mean";
	}


	@Override
	public String getDescription() {
		return "Mean heights";
	}

	
	
}
