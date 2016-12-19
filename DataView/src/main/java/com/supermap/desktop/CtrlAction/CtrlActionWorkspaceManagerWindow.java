package com.supermap.desktop.CtrlAction;

import com.sun.org.apache.xpath.internal.functions.FuncFalse;
import com.supermap.desktop.Application;
import com.supermap.desktop.CtrlAction.SQLQuery.components.PanelSaveSearchResult;
import com.supermap.desktop.Interface.IBaseItem;
import com.supermap.desktop.Interface.IForm;
import com.supermap.desktop.Interface.IFormManager;
import com.supermap.desktop.ui.FormBaseChild;
import com.supermap.desktop.workspacemanagerwindow.WorkspaceManagerWindow;
import com.supermap.desktop.implement.CtrlAction;

/**
 * @author YuanR
 *         存留问题：1、当打开或者关闭工作空间时，因为closeAll()方法的执行，会导致窗口被关闭，应该保持窗口的常在，当工作空间被改变时，刷新窗口显示
 *
 */
public class CtrlActionWorkspaceManagerWindow extends CtrlAction {
	public CtrlActionWorkspaceManagerWindow(IBaseItem caller, IForm formClass) {
		super(caller, formClass);
	}

	FormBaseChild workspaceManagerWindow;
	IFormManager formManager;

	public void run() {
		try {
			formManager = Application.getActiveApplication().getMainFrame().getFormManager();
			if (workspaceManagerWindow == null) {
				//此处存疑：将IForm当做参数传递，目的是：当关闭窗口时，其workspaceManagerWindow不为空
				workspaceManagerWindow = new WorkspaceManagerWindow();
				formManager.showChildForm(workspaceManagerWindow);
				//当show出窗口时，设置其显示为true
				workspaceManagerWindow.setVisible(true);
			} else {
				if (workspaceManagerWindow.isClosed()) {
					//当窗口为关闭状态时，show出来
					formManager.showChildForm(workspaceManagerWindow);
					//当show出窗口时，设置其显示为true
					workspaceManagerWindow.setVisible(true);
				} else {
					//当窗口为打开状态时，关闭窗口，并重写close（），添加formWorkspaceManagerWindow.setVisible(false);
					formManager.close(workspaceManagerWindow);
					workspaceManagerWindow.setVisible(false);
				}
			}

		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}
	}

	@Override
	public boolean enable() {
		// TODO Auto-generated method stub
		return true;
	}
}
