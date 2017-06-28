package com.supermap.desktop.process.ctrlAction;

import com.supermap.desktop.Application;
import com.supermap.desktop.Interface.IBaseItem;
import com.supermap.desktop.Interface.IForm;
import com.supermap.desktop.implement.CtrlAction;
import com.supermap.desktop.process.FormWorkflow;
import com.supermap.desktop.process.core.DirectConnect;
import com.supermap.desktop.process.core.IProcess;
import com.supermap.desktop.process.core.MatrixExecutor;
import com.supermap.desktop.process.core.NodeMatrix;
import com.supermap.desktop.process.graphics.GraphCanvas;
import com.supermap.desktop.process.graphics.connection.IGraphConnection;
import com.supermap.desktop.process.graphics.graphs.IGraph;
import com.supermap.desktop.process.graphics.graphs.OutputGraph;
import com.supermap.desktop.process.graphics.graphs.ProcessGraph;
import com.supermap.desktop.process.graphics.storage.IConnectionManager;
import com.supermap.desktop.process.graphics.storage.IGraphStorage;
import com.supermap.desktop.process.tasks.ProcessTask;
import com.supermap.desktop.process.tasks.TasksManagerContainer;
import com.supermap.desktop.process.util.TaskUtil;

import java.util.Vector;

/**
 * Created by highsad on 2017/2/28.
 */
public class CtrlActionRun extends CtrlAction {
	private final static String TASKS = "com.supermap.desktop.process.tasks.TasksManagerContainer";

	public CtrlActionRun(IBaseItem caller, IForm formClass) {
		super(caller, formClass);
	}

	@Override
	public void run() {
		NodeMatrix nodeMatrix = new NodeMatrix();
		FormWorkflow formWorkflow = (FormWorkflow) Application.getActiveApplication().getMainFrame().getFormManager().getActiveForm();
		GraphCanvas canvas = formWorkflow.getCanvas();
		IGraphStorage graphStorage = canvas.getGraphStorage();
		IConnectionManager graphConnection = canvas.getConnection();
		IGraph[] graphs = graphStorage.getGraphs();
		for (IGraph graph : graphs) {
			if (graph instanceof ProcessGraph) {
				nodeMatrix.addNode(((ProcessGraph) graph).getProcess());
			}
		}

		IGraphConnection[] connections = graphConnection.getConnections();
		for (IGraphConnection connection : connections) {
			IGraph startGraph = connection.getStart().getConnector();
			IGraph endGraph = connection.getEnd().getConnector();

			if (endGraph instanceof ProcessGraph && startGraph instanceof OutputGraph) {
				IProcess end = ((ProcessGraph) connection.getEnd().getConnector()).getProcess();
				IProcess start = ((OutputGraph) startGraph).getProcessGraph().getProcess();
				nodeMatrix.addRelation(start, end, DirectConnect.class);
			}
		}

		TasksManagerContainer container = TaskUtil.getManagerContainer(true);
//		container.clear();
		Vector list = nodeMatrix.getNodes();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) instanceof IProcess) {
				ProcessTask task = TaskUtil.getTask((IProcess) list.get(i));
				task.setCancel(false);
//				container.addItem(task);
			}
		}

		MatrixExecutor executor = new MatrixExecutor(nodeMatrix);
		executor.run();
	}

	@Override
	public boolean enable() {
		return Application.getActiveApplication().getMainFrame().getFormManager().getActiveForm() instanceof FormWorkflow;
	}
}
