package com.donglu.carpark.ui.view.user;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Group;

import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.donglu.carpark.ui.common.Presenter;
import com.donglu.carpark.ui.common.View;
import com.dongluhitec.card.domain.db.singlecarpark.SingleCarparkSystemUser;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.nebula.widgets.datechooser.DateChooserCombo;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;

public class CarparkPayHistoryView extends Composite implements View {
	private Text text_pay_userName;

	private Composite listComposite;
	private Presenter presenter;
	private ComboViewer comboViewer;
	private Text txt_plate;
	private Text txt_address;

	public CarparkPayHistoryView(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, false));

		Group group = new Group(this, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		group.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.NORMAL));
		group.setText("查询");
		group.setLayout(new GridLayout(15, false));

		Label label = new Label(group, SWT.NONE);
		label.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.NORMAL));
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("用户");

		text_pay_userName = new Text(group, SWT.BORDER);
		text_pay_userName.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.NORMAL));
		text_pay_userName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		Label label_4 = new Label(group, SWT.NONE);
		label_4.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12, SWT.NORMAL));
		label_4.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_4.setText("车牌");
		
		txt_plate = new Text(group, SWT.BORDER);
		txt_plate.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12, SWT.NORMAL));
		txt_plate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		Label label_5 = new Label(group, SWT.NONE);
		label_5.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12, SWT.NORMAL));
		label_5.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_5.setText("住址");
		
		txt_address = new Text(group, SWT.BORDER);
		txt_address.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12, SWT.NORMAL));
		txt_address.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		Label label_1 = new Label(group, SWT.NONE);
		label_1.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.NORMAL));
		label_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_1.setText("操作员");

		comboViewer = new ComboViewer(group, SWT.READ_ONLY);
		Combo combo = comboViewer.getCombo();
		comboViewer.setContentProvider(new ArrayContentProvider());
		comboViewer.setLabelProvider(new LabelProvider());

		combo.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.NORMAL));
		GridData gd_combo = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_combo.widthHint = 66;
		combo.setLayoutData(gd_combo);

		Label label_2 = new Label(group, SWT.NONE);
		label_2.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.NORMAL));
		label_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_2.setText("起始时间");

		DateChooserCombo dateChooserCombo = new DateChooserCombo(group, SWT.BORDER | SWT.READ_ONLY);
		dateChooserCombo.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.NORMAL));
		dateChooserCombo.setValue(new org.joda.time.DateTime(new Date()).minusDays(1).toDate());

		Label label_3 = new Label(group, SWT.NONE);
		label_3.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.NORMAL));
		label_3.setText("终止时间");

		DateChooserCombo dateChooserCombo_1 = new DateChooserCombo(group, SWT.BORDER | SWT.READ_ONLY);
		dateChooserCombo_1.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.NORMAL));
		dateChooserCombo_1.setValue(new Date());
		Button button = new Button(group, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					String text = comboViewer.getCombo().getText();
					if (text.equals("全部")) {
						text = null;
					}
					getPresenter().searchCharge(text_pay_userName.getText(),txt_plate.getText(),txt_address.getText(), text, dateChooserCombo.getValue(), dateChooserCombo_1.getValue());
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		button.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.NORMAL));
		button.setText("查询");

		Button button_1 = new Button(group, SWT.NONE);
		button_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getPresenter().export();
			}
		});
		button_1.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.NORMAL));
		button_1.setText("导出");
		
		Button button_2 = new Button(group, SWT.NONE);
		button_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getPresenter().split(dateChooserCombo.getValue(), dateChooserCombo_1.getValue());
			}
		});
		button_2.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.NORMAL));
		button_2.setText("分账");

		listComposite = new Composite(this, SWT.NONE);
		listComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		listComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
	}

	public void setCarparkPayHistoryPresenter(CarparkPayHistoryPresenter carparkPayHistoryPresenter) {
		this.presenter = carparkPayHistoryPresenter;
	}

	public Composite getListComposite() {
		return listComposite;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public CarparkPayHistoryPresenter getPresenter() {
		return (CarparkPayHistoryPresenter) this.presenter;
	}

	public void setComboValue(List<SingleCarparkSystemUser> list) {
		comboViewer.setInput(list);
		comboViewer.getCombo().select(0);
	}
}
