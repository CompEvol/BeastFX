package beastfx.app.methodsection.implementation;

import beast.base.core.BEASTInterface;
import beast.base.core.BEASTObject;
import beast.base.core.Input;
import beast.base.evolution.sitemodel.SiteModel;
import beast.base.evolution.tree.MRCAPrior;
import beast.base.evolution.tree.coalescent.ConstantPopulation;
import beast.base.inference.distribution.ParametricDistribution;
import beast.base.inference.distribution.Prior;
import beast.base.inference.parameter.Parameter;
import beast.base.inference.parameter.RealParameter;
import beastfx.app.inputeditor.BeautiDoc;
import beastfx.app.methodsection.MethodsText;
import beastfx.app.methodsection.MethodsTextFactory;
import beastfx.app.methodsection.Phrase;

import java.util.*;

public class SiteModelMethodsText implements MethodsText {

	@Override
	public Class<?> type() {
		return SiteModel.class;
	}

	
	@Override
	public List<Phrase> getModelDescription(Object o, BEASTInterface parent, Input<?> input2, BeautiDoc doc) {
		SiteModel sm = (SiteModel) o; 
		List<Phrase> b = new ArrayList<>();
		//SubstitutionModel subst = sm.substModelInput.get();
		b.add(new Phrase(sm, parent, input2, " gamma site model "));
		b.add(new Phrase("and "));
		List<Phrase> substModel = MethodsTextFactory.getModelDescription(sm.substModelInput.get(), sm, sm.substModelInput, doc);
		b.addAll(substModel);
		if (sm.gammaCategoryCount.get() > 1) {
			b.add(new Phrase(" with gamma rate heterogeneity using " + sm.gammaCategoryCount.get() + " categories "));
			RealParameter shape = sm.shapeParameterInput.get();
			if (shape.isEstimatedInput.get()) {
				b.add(new Phrase(" and shape "));
				b.addAll(describePriors(shape, sm, sm.shapeParameterInput, doc));
			} else {
				b.add(new Phrase(" and shape = " + shape.getValue() + ""));
			}
		}
		if (sm.invarParameterInput.get() != null && sm.invarParameterInput.get().getValue() > 0) {
			b.add(new Phrase(" and a category proportion invarible "));
		}
		return b;
	}
}
