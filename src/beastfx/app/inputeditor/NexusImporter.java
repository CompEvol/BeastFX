package beastfx.app.inputeditor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import beastfx.app.util.Alert;

import beast.base.core.BEASTInterface;
import beast.base.core.Description;
import beast.base.evolution.alignment.Alignment;
import beast.base.evolution.alignment.FilteredAlignment;
import beast.base.parser.NexusParser;

@Description("NEXUS file importer")
public class NexusImporter implements AlignmentImporter {

	@Override
	public String[] getFileExtensions() {
		return new String[]{"nex","nxs","nexus"};
	}

	@Override
	public List<BEASTInterface> loadFile(File file) {
		List<BEASTInterface> selectedBEASTObjects = new ArrayList<>();
		NexusParser parser = new NexusParser();
		try {
			parser.parseFile(file);
			if (parser.filteredAlignments.size() > 0) {
				/**
				 * sanity check: make sure the filters do not
				 * overlap
				 **/
				int[] used = new int[parser.m_alignment.getSiteCount()];
				Set<Integer> overlap = new HashSet<>();
				int partitionNr = 1;
				for (Alignment data : parser.filteredAlignments) {
					int[] indices = ((FilteredAlignment) data).indices();
					for (int i : indices) {
						if (used[i] > 0) {
							overlap.add(used[i] * 10000 + partitionNr);
						} else {
							used[i] = partitionNr;
						}
					}
					partitionNr++;
				}
				if (overlap.size() > 0) {
					String overlaps = "<html>Warning: The following partitions overlap:<br/>";
					for (int i : overlap) {
						overlaps += parser.filteredAlignments.get(i / 10000 - 1).getID()
								+ " overlaps with "
								+ parser.filteredAlignments.get(i % 10000 - 1).getID() + "<br/>";
					}
					overlaps += "The first thing you might want to do is delete some of these partitions.</html>";
					Alert.showMessageDialog(null, overlaps);
				}
				/** add alignments **/
				for (Alignment data : parser.filteredAlignments) {
					BeautiAlignmentProvider.sortByTaxonName(data.sequenceInput.get());
					selectedBEASTObjects.add(data);
				}
				if (parser.calibrations != null) {
					selectedBEASTObjects.addAll(parser.calibrations);
				}
			} else {
				selectedBEASTObjects.add(parser.m_alignment);
				if (parser.calibrations != null) {
					selectedBEASTObjects.addAll(parser.calibrations);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			Alert.showMessageDialog(null, "Loading of " + file.getPath() + " failed: " + ex.getMessage());
			return null;
		}
		return selectedBEASTObjects;
	}
}