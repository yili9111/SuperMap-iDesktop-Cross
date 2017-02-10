package com.supermap.desktop.process.tasks;

import com.supermap.desktop.process.tasks.callable.CallableFactory;
import com.supermap.desktop.process.tasks.callable.UpdateProgressCallable;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.Group;
import java.awt.*;
import java.util.ArrayList;

public class TasksManagerContainer extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private ArrayList<ITask> items;
    private ArrayList<UpdateProgressCallable> callables;

    GroupLayout groupLayout;
    Group horizontalGroup = null;
    Group verticalGroup = null;

    public TasksManagerContainer() {
        items = new ArrayList();
        callables = new ArrayList<>();
        initializeComponents();
        initializeResources();
    }

    private void initializeComponents() {

        groupLayout = new GroupLayout(this);
        groupLayout.setAutoCreateContainerGaps(true);
        groupLayout.setAutoCreateGaps(true);
        this.setLayout(groupLayout);

        updateItems();
    }

    private void initializeResources() {
        // this.buttonApply.setText(CommonProperties.getString("String_Button_Apply"));
    }

    private void updateItems() {
        removeAll();
        horizontalGroup = groupLayout.createParallelGroup(Alignment.CENTER);
        groupLayout.setHorizontalGroup(horizontalGroup);

        verticalGroup = groupLayout.createSequentialGroup();
        groupLayout.setVerticalGroup(verticalGroup);

        for (ITask item : items) {
            horizontalGroup.addComponent((Component) item);
            verticalGroup.addComponent((Component) item);
        }
    }

    public void run() {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int size = items.size();
                int i = 0;
                for (int j = 0; j < size; j++) {
                    items.get(j).doWork(callables.get(j));
                }
            }
        });
        thread.start();
    }

    public void stop() {
        int size = items.size();
        for (int i = 0; i < size; i++) {
            items.get(i).setCancel(true);
        }
    }

    public void addItem(ITask task) {
        CallableFactory factory = new CallableFactory();
        UpdateProgressCallable callable = factory.createCallable(task);
        callables.add(callable);
        items.add(task);
        updateItems();
    }

    public void removeItem(ITask task) {
        for (ITask item : items) {
            if (item.equals(task)) {
                items.remove(item);
                groupLayout.removeLayoutComponent((Component) item);
                break;
            }
        }
        updateItems();
    }
}
