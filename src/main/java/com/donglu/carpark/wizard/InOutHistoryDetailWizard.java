package com.donglu.carpark.wizard;

import org.eclipse.jface.wizard.Wizard;

import com.dongluhitec.card.common.ui.AbstractWizard;
import com.dongluhitec.card.domain.db.singlecarpark.SingleCarparkCarpark;
import com.dongluhitec.card.domain.db.singlecarpark.SingleCarparkInOutHistory;
import com.dongluhitec.card.domain.util.StrUtil;

public class InOutHistoryDetailWizard extends Wizard implements AbstractWizard{
	private String file;
	private SingleCarparkInOutHistory model;
	private InOutHistoryDetailWizardPage page;
	public InOutHistoryDetailWizard(SingleCarparkInOutHistory model,String file) {
		setWindowTitle("查看进出记录");
		this.model=model;
		this.file=file;
	}

	@Override
	public void addPages() {
		page = new InOutHistoryDetailWizardPage(model,file);
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		init();
		return false;
	}

	@Override
	public Object getModel() {
		
		return model;
	}

	public void init() {
		page.setBigImg();
		page.setSmallImg();
	}

}