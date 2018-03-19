package com.donglu.carpark.ui.wizard;

import org.eclipse.jface.wizard.Wizard;

import com.donglu.carpark.ui.view.SearchErrorCarPresenter;
import com.dongluhitec.card.common.ui.AbstractWizard;
import com.dongluhitec.card.common.ui.uitl.JFaceUtil;


public class SearchHistoryByHandWizard extends Wizard implements AbstractWizard{
	private SearchErrorCarPresenter searchErrorCarPresenter;
	public SearchHistoryByHandWizard(SearchErrorCarPresenter searchErrorCarPresenter) {
		setWindowTitle("人工查找");
		this.searchErrorCarPresenter=searchErrorCarPresenter;
	}

	@Override
	public void addPages() {
		addPage(new SearchHistoryByHandWizardPage(searchErrorCarPresenter));
		getShell().setSize(900, 600);
		getShell().setImage(JFaceUtil.getImage("carpark_32"));
	}

	@Override
	public boolean performFinish() {
		return true;
	}

	@Override
	public Object getModel() {
		
		return "1";
	}

}
