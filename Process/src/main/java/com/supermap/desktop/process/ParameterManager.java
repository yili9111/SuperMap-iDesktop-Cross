package com.supermap.desktop.process;

import com.supermap.desktop.process.core.IProcess;
import com.supermap.desktop.process.tasks.ProcessTask;
import com.supermap.desktop.process.tasks.TaskStore;
import com.supermap.desktop.ui.controls.GridBagConstraintsHelper;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;

/**
 * @author XiaJT
 */
public class ParameterManager extends JPanel {
    private JPanel mainPanel = new JPanel();

    public ParameterManager() {
        Field[] fields = mainPanel.getClass().getFields();
        this.setLayout(new GridBagLayout());
        mainPanel.setLayout(new GridBagLayout());
        this.add(mainPanel, new GridBagConstraintsHelper(0, 0, 1, 1).setWeight(1, 1).setFill(GridBagConstraints.BOTH));
    }

    public void setProcess(IProcess process) {
        mainPanel.removeAll();
        if (process != null && process.getComponent().getPanel() instanceof Component) {
            JComponent component = (JComponent) process.getComponent().getPanel();
            ProcessTask task = new ProcessTask(process);
            TaskStore.addTask(task);
            mainPanel.add(component, new GridBagConstraintsHelper(0, 0, 1, 1).setWeight(1, 1).setFill(GridBagConstraints.BOTH));
            mainPanel.add(new JPanel(), new GridBagConstraintsHelper(0, 1, 1, 1).setWeight(1, 1).setFill(GridBagConstraintsHelper.BOTH));
            mainPanel.add(task, new GridBagConstraintsHelper(0, 2, 1, 1).setWeight(1, 0).setFill(GridBagConstraintsHelper.HORIZONTAL));
            int count = task.getComponentCount();
            for (int i = 0; i < count; i++) {
                if ("ProcessTask_buttonRemove".equals(task.getComponent(i).getName())) {
                    task.getComponent(i).setVisible(false);
                }
            }
        }
        mainPanel.revalidate();
        mainPanel.repaint();
        this.revalidate();
        this.repaint();
    }
}
