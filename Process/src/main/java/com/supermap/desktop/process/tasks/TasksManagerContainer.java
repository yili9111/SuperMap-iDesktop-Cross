package com.supermap.desktop.process.tasks;

import com.sun.org.apache.bcel.internal.generic.RET;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.Group;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class TasksManagerContainer extends JPanel implements WorkerStateChangedListener {

	/**
	 *
	 */
	private final static long serialVersionUID = 1L;
	private TasksManager tasksManager;
	private final static String TITLE_RUNNING = "正在执行";
	private final static String TITLE_READY = "准备就绪";
	private final static String TITLE_WAITING = "等待";
	private final static String TITLE_COMPLETED = "已完成";
	private final static String TITLE_CANCELLED = "已取消";
	private final static String TITLE_EXCEPTION = "异常中断";
	private final static String TITLE_PATTERN = "{0} ({1})";

	private JPanel panelRunning;
	private JPanel panelReady;
	private JPanel panelWaiting;
	private JPanel panelCompleted;
	private JPanel panelCancelled;
	private JPanel panelException;

	private Map<ProcessWorker, SingleProgressPanel> map = new ConcurrentHashMap<>();

	public TasksManagerContainer() {
		initializeComponents();
		initializeResources();
		validatePanelsTitle();
		validatePanelsVisible();
	}

	public void setTasksManager(TasksManager tasksManager) {
		if (this.tasksManager != null) {
			unloadTasksManager();
		}

		this.tasksManager = tasksManager;
		loadTasksManager();
	}

	private void loadTasksManager() {
		if (this.tasksManager != null) {
			buildTasksUI();
			validatePanelsTitle();
			validatePanelsVisible();
			this.tasksManager.addWorkerStateChangeListener(this);
		}
	}

	private void unloadTasksManager() {
		clearPanels();
		validatePanelsVisible();
		this.tasksManager.removeWorkerStateChangeListener(this);
		this.tasksManager = null;
	}

	private void initializeComponents() {
		this.panelRunning = new JPanel();
		this.panelRunning.setBorder(new TitledBorder(TITLE_RUNNING));
		this.panelRunning.setLayout(new BoxLayout(this.panelRunning, BoxLayout.Y_AXIS));

		this.panelReady = new JPanel();
		this.panelReady.setBorder(new TitledBorder(TITLE_READY));
		this.panelReady.setLayout(new BoxLayout(this.panelReady, BoxLayout.Y_AXIS));

		this.panelWaiting = new JPanel();
		this.panelWaiting.setBorder(new TitledBorder(TITLE_WAITING));
		this.panelWaiting.setLayout(new BoxLayout(this.panelWaiting, BoxLayout.Y_AXIS));

		this.panelCompleted = new JPanel();
		this.panelCompleted.setBorder(new TitledBorder(TITLE_COMPLETED));
		this.panelCompleted.setLayout(new BoxLayout(this.panelCompleted, BoxLayout.Y_AXIS));

		this.panelCancelled = new JPanel();
		this.panelCancelled.setBorder(new TitledBorder(TITLE_CANCELLED));
		this.panelCancelled.setLayout(new BoxLayout(this.panelCancelled, BoxLayout.Y_AXIS));
		this.panelCancelled.setVisible(false);

		this.panelException = new JPanel();
		this.panelException.setBorder(new TitledBorder(TITLE_EXCEPTION));
		this.panelException.setLayout(new BoxLayout(this.panelException, BoxLayout.Y_AXIS));
		this.panelException.setVisible(false);

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setAutoCreateContainerGaps(true);
		groupLayout.setAutoCreateGaps(true);
		setLayout(groupLayout);

		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(this.panelRunning, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(this.panelReady, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(this.panelWaiting, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(this.panelCompleted, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(this.panelCancelled, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(this.panelException, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

		groupLayout.setVerticalGroup(groupLayout.createSequentialGroup()
				.addComponent(this.panelRunning, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(this.panelReady, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(this.panelWaiting, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(this.panelCompleted, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(this.panelCancelled, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(this.panelException, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));
	}

	private void buildTasksUI() {
		if (this.tasksManager == null) {
			return;
		}

		int[] workerStates = TasksManager.getWorkerStates();
		for (int i = 0; i < workerStates.length; i++) {
			JPanel panel = getPanel(workerStates[i]);
			Vector<ProcessWorker> workers = this.tasksManager.getProcessWorkers(workerStates[i]);
			buildPanel(panel, workers);
		}
	}

	private void buildPanel(JPanel panel, Vector<ProcessWorker> workers) {
		if (panel == null || workers == null || workers.size() == 0) {
			return;
		}

		for (int i = 0, size = workers.size(); i < size; i++) {
			ProcessWorker worker = workers.get(i);
			if (this.map.containsKey(worker)) {
				throw new UnsupportedOperationException();
			}

			SingleProgressPanel progressPanel = new SingleProgressPanel(worker);
			this.map.put(worker, progressPanel);
			panel.add(progressPanel);
		}
	}

	private void clearPanels() {
		this.panelCancelled.removeAll();
		this.panelException.removeAll();
		this.panelRunning.removeAll();
		this.panelReady.removeAll();
		this.panelWaiting.removeAll();
		this.panelCompleted.removeAll();
	}

	private void validatePanelsTitle() {
		setPanelTitle(this.panelCancelled, MessageFormat.format(TITLE_PATTERN, TITLE_CANCELLED, this.panelCancelled.getComponentCount()));
		setPanelTitle(this.panelCompleted, MessageFormat.format(TITLE_PATTERN, TITLE_COMPLETED, this.panelCompleted.getComponentCount()));
		setPanelTitle(this.panelException, MessageFormat.format(TITLE_PATTERN, TITLE_EXCEPTION, this.panelException.getComponentCount()));
		setPanelTitle(this.panelWaiting, MessageFormat.format(TITLE_PATTERN, TITLE_WAITING, this.panelWaiting.getComponentCount()));
		setPanelTitle(this.panelReady, MessageFormat.format(TITLE_PATTERN, TITLE_READY, this.panelReady.getComponentCount()));
		setPanelTitle(this.panelRunning, MessageFormat.format(TITLE_PATTERN, TITLE_RUNNING, this.panelRunning.getComponentCount()));
	}

	private void setPanelTitle(JPanel panel, String title) {
		if (panel.getBorder() instanceof TitledBorder) {
			TitledBorder border = (TitledBorder) panel.getBorder();
			border.setTitle(title);
		}
	}

	private void validatePanelsVisible() {
		this.panelCancelled.setVisible(this.panelCancelled.getComponentCount() > 0);
		this.panelException.setVisible(this.panelCancelled.getComponentCount() > 0);
	}


	private JPanel getPanel(int panel) {
		switch (panel) {
			case TasksManager.WORKER_STATE_CANCELLED:
				return this.panelCancelled;
			case TasksManager.WORKER_STATE_COMPLETED:
				return this.panelCompleted;
			case TasksManager.WORKER_STATE_EXCEPTION:
				return this.panelException;
			case TasksManager.WORKER_STATE_READY:
				return this.panelReady;
			case TasksManager.WORKER_STATE_RUNNING:
				return this.panelRunning;
			case TasksManager.WORKER_STATE_WAITING:
				return this.panelWaiting;
			default:
				return null;
		}
	}

	private void moveWorker(ProcessWorker worker, int oldState, int newState) {
		if (oldState == newState) {
			return;
		}

		if (!this.map.containsKey(worker)) {
			return;
		}

		SingleProgressPanel progressPanel = this.map.get(worker);
		JPanel oldContainer = getPanel(oldState);
		JPanel newContainer = getPanel(newState);

		oldContainer.remove(progressPanel);
		newContainer.add(progressPanel);

		validatePanelsTitle();
		validatePanelsVisible();
	}

	private void initializeResources() {
		// this.buttonApply.setText(CommonProperties.getString("String_Button_Apply"));
	}

	public static void main(String[] args) {
		TasksManagerContainer container = new TasksManagerContainer();

		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(container, BorderLayout.CENTER);
		frame.setSize(400, 900);
		frame.setVisible(true);
	}

	@Override
	public void workerStateChanged(WorkerStateChangedEvent e) {
		if (e.getManager() != this.tasksManager) {
			return;
		}

		ProcessWorker worker = e.getProcessWorker();
		if (worker == null) {
			return;
		}

		moveWorker(worker, e.getOldState(), e.getNewState());
	}
}

