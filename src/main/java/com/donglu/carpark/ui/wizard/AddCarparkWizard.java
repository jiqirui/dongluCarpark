package com.donglu.carpark.ui.wizard;

import org.eclipse.jface.wizard.Wizard;

import com.donglu.carpark.service.CarparkDatabaseServiceProvider;
import com.dongluhitec.card.common.ui.AbstractWizard;
import com.dongluhitec.card.domain.db.singlecarpark.SingleCarparkCarpark;
import com.dongluhitec.card.domain.util.StrUtil;

public class AddCarparkWizard extends Wizard implements AbstractWizard{
	
	private SingleCarparkCarpark model;
	private AddCarparkWizardPage page;
	CarparkDatabaseServiceProvider sp;
	public AddCarparkWizard(SingleCarparkCarpark model, CarparkDatabaseServiceProvider sp) {
		this.model=model;
		this.sp=sp;
		if (StrUtil.isEmpty(model.getCode())) {
			setWindowTitle("添加停车场");
		}else{
			setWindowTitle("修改停车场");
		}
		
	}

	@Override
	public void addPages() {
		page = new AddCarparkWizardPage(model);
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		if (StrUtil.isEmpty(model.getCode())||StrUtil.isEmpty(model.getName())) {
			page.setErrorMessage("请填写完整信息");
			return false;
		}
		String code = model.getCode();
		try {
			int parseInt = Integer.parseInt(code);
			if (parseInt<0||parseInt>99) {
				page.setErrorMessage("编码只能是0-99的数字");
				return false;
			}
			if (parseInt>=0&&parseInt<=9) {
				model.setCode("0"+parseInt);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			page.setErrorMessage("编码只能是0-99的数字");
			return false;
		}
		if (checkCode()) {
			page.setErrorMessage("编码已存在");
			return false;
		}
		if (model.getName().length()>100) {
			page.setErrorMessage("名称不能太长");
			return false;
		}
		return true;
	}

	private boolean checkCode() {
		SingleCarparkCarpark findCarparkById = sp.getCarparkService().findCarparkByCode(model.getCode());
		if (!StrUtil.isEmpty(findCarparkById)&&findCarparkById.getId()!=model.getId()) {
			return true;
		}
		return false;
	}

	@Override
	public Object getModel() {
		
		return model;
	}

}