package com.supermap.desktop.WorkflowView.meta.metaProcessImplements;

import com.supermap.analyst.spatialanalyst.DistanceAnalyst;
import com.supermap.analyst.spatialanalyst.DistanceAnalystParameter;
import com.supermap.analyst.spatialanalyst.PathLineResult;
import com.supermap.analyst.spatialanalyst.SmoothMethod;
import com.supermap.data.*;
import com.supermap.desktop.Application;
import com.supermap.desktop.WorkflowView.meta.MetaKeys;
import com.supermap.desktop.WorkflowView.meta.MetaProcess;
import com.supermap.desktop.process.ProcessProperties;
import com.supermap.desktop.process.constraint.ipls.DatasourceConstraint;
import com.supermap.desktop.process.constraint.ipls.EqualDatasourceConstraint;
import com.supermap.desktop.process.events.RunningEvent;
import com.supermap.desktop.process.parameter.ParameterDataNode;
import com.supermap.desktop.process.parameter.interfaces.IParameters;
import com.supermap.desktop.process.parameter.interfaces.datas.types.DatasetTypes;
import com.supermap.desktop.process.parameter.ipls.*;
import com.supermap.desktop.properties.CommonProperties;
import com.supermap.desktop.utilities.DatasetUtilities;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created By Chens on 2017/8/7 0007
 * 最小耗费距离
 */
public class MetaProcessCostPathLine extends MetaProcess {
	private static final String INPUT_DATA = CommonProperties.getString("String_GroupBox_SourceData");
	private static final String COST_DATA = ProcessProperties.getString("String_GroupBox_CostData");
	private static final String OUTPUT_DATA = "CostPathLineResult";

	private ParameterDatasourceConstrained sourceDatasources;
	private ParameterSingleDataset sourceDataset;
	private ParameterDatasourceConstrained costDatasources;
	private ParameterSingleDataset costDataset;
	private ParameterSaveDataset resultDataset;
	private ParameterLabel labelOrigin;
	private ParameterLabel labelTarget;
	private ParameterNumber numberOriginX;
	private ParameterNumber numberOriginY;
	private ParameterNumber numberTargetX;
	private ParameterNumber numberTargetY;
	private ParameterComboBox comboBoxSmoothMethod;
	private ParameterNumber numberSmoothDegree;

	public MetaProcessCostPathLine() {
		initParameters();
		initParameterConstraint();
		initParametersState();
		registerListener();
	}

	private void initParameters() {
		sourceDatasources = new ParameterDatasourceConstrained();
		sourceDataset = new ParameterSingleDataset(DatasetType.GRID);
		costDatasources = new ParameterDatasourceConstrained();
		costDataset = new ParameterSingleDataset(DatasetType.GRID);
		resultDataset = new ParameterSaveDataset();
		labelOrigin = new ParameterLabel();
		labelOrigin.setDescribe(ProcessProperties.getString("String_OriginPoint"));
		labelTarget = new ParameterLabel();
		labelTarget.setDescribe(ProcessProperties.getString("String_TargetPoint"));
		numberOriginX = new ParameterNumber(ProcessProperties.getString("String_Xcoordinate"));
		numberOriginY = new ParameterNumber(ProcessProperties.getString("String_Ycoordinate"));
		numberTargetX = new ParameterNumber(ProcessProperties.getString("String_Xcoordinate"));
		numberTargetY = new ParameterNumber(ProcessProperties.getString("String_Ycoordinate"));
		comboBoxSmoothMethod = new ParameterComboBox(CommonProperties.getString("String_SmoothMethod"));
		numberSmoothDegree = new ParameterNumber(ProcessProperties.getString("String_Label_Smoothness"));

		ParameterCombine sourceCombine = new ParameterCombine();
		sourceCombine.setDescribe(CommonProperties.getString("String_GroupBox_SourceData"));
		sourceCombine.addParameters(sourceDatasources, sourceDataset);
		ParameterCombine costCombine = new ParameterCombine();
		costCombine.setDescribe(CommonProperties.getString("String_GroupBox_SourceData"));
		costCombine.addParameters(costDatasources, costDataset);
		ParameterCombine settingCombine = new ParameterCombine();
		settingCombine.setDescribe(CommonProperties.getString("String_GroupBox_ParamSetting"));
		settingCombine.addParameters(labelOrigin, numberOriginX, numberOriginY, labelTarget, numberTargetX, numberTargetY, comboBoxSmoothMethod, numberSmoothDegree);
		ParameterCombine outputCombine = new ParameterCombine();
		outputCombine.setDescribe(CommonProperties.getString("String_GroupBox_ResultData"));
		outputCombine.addParameters(resultDataset);

		parameters.setParameters(sourceCombine, costCombine, settingCombine, outputCombine);
		parameters.addInputParameters(INPUT_DATA, DatasetTypes.GRID, sourceCombine);
		parameters.addInputParameters(COST_DATA, DatasetTypes.GRID, costCombine);
		parameters.addOutputParameters(OUTPUT_DATA, DatasetTypes.LINE, outputCombine);
	}

	private void initParameterConstraint() {
		EqualDatasourceConstraint constraintSource = new EqualDatasourceConstraint();
		constraintSource.constrained(sourceDatasources, ParameterDatasource.DATASOURCE_FIELD_NAME);
		constraintSource.constrained(sourceDataset, ParameterSingleDataset.DATASOURCE_FIELD_NAME);

		EqualDatasourceConstraint constraintSource1 = new EqualDatasourceConstraint();
		constraintSource1.constrained(costDatasources, ParameterDatasource.DATASOURCE_FIELD_NAME);
		constraintSource1.constrained(costDataset, ParameterSingleDataset.DATASOURCE_FIELD_NAME);

		DatasourceConstraint.getInstance().constrained(resultDataset, ParameterSaveDataset.DATASOURCE_FIELD_NAME);
	}

	private void initParametersState() {
		DatasetGrid datasetGrid = DatasetUtilities.getDefaultDatasetGrid();
		if (datasetGrid != null) {
			sourceDatasources.setSelectedItem(datasetGrid.getDatasource());
			sourceDataset.setSelectedItem(datasetGrid);
			costDatasources.setSelectedItem(datasetGrid.getDatasource());
			costDataset.setSelectedItem(datasetGrid);
		}
		resultDataset.setDatasetName("result_costPathLine");
		comboBoxSmoothMethod.setItems(new ParameterDataNode(CommonProperties.getString("String_SmoothMethod_NONE"), SmoothMethod.NONE),
				new ParameterDataNode(CommonProperties.getString("String_SmoothMethod_BSLine"), SmoothMethod.BSPLINE),
				new ParameterDataNode(CommonProperties.getString("String_SmoothMethod_POLISH"), SmoothMethod.POLISH));
		numberOriginX.setSelectedItem(0.0);
		numberOriginY.setSelectedItem(0.0);
		numberTargetX.setSelectedItem(0.0);
		numberTargetY.setSelectedItem(0.0);
		numberSmoothDegree.setSelectedItem(2);
		numberSmoothDegree.setEnabled(false);
	}

	private void registerListener() {
		comboBoxSmoothMethod.addPropertyListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (comboBoxSmoothMethod.getSelectedData().equals(SmoothMethod.BSPLINE)||comboBoxSmoothMethod.getSelectedData().equals(SmoothMethod.POLISH)) {
					numberSmoothDegree.setEnabled(true);
					numberSmoothDegree.setMinValue(2);
					numberSmoothDegree.setMaxValue(10);
					numberSmoothDegree.setMaxBit(0);
				} else {
					numberSmoothDegree.setEnabled(false);
				}
			}
		});
	}

	@Override
	public IParameters getParameters() {
		return super.getParameters();
	}

	@Override
	public String getKey() {
		return MetaKeys.COST_PATH_LINE;
	}

	@Override
	public String getTitle() {
		return ProcessProperties.getString("String_Title_CostPathLine");
	}

	@Override
	public boolean execute() {
		boolean isSuccessful = false;
		try {
			fireRunning(new RunningEvent(this, 0, "start"));
			DistanceAnalyst.addSteppedListener(steppedListener);

			DatasetGrid src = null;
			if (parameters.getInputs().getData(INPUT_DATA).getValue() != null) {
				src = (DatasetGrid) parameters.getInputs().getData(INPUT_DATA).getValue();
			} else {
				src = (DatasetGrid) sourceDataset.getSelectedItem();
			}
			DatasetGrid srcCost = null;
			if (parameters.getInputs().getData(INPUT_DATA).getValue() != null) {
				srcCost = (DatasetGrid) parameters.getInputs().getData(INPUT_DATA).getValue();
			} else {
				srcCost = (DatasetGrid) sourceDataset.getSelectedItem();
			}

			DistanceAnalystParameter distanceAnalystParameter = new DistanceAnalystParameter();
			distanceAnalystParameter.setSourceDataset(src);
			distanceAnalystParameter.setCostGrid(srcCost);
			distanceAnalystParameter.setPathLineSmoothMethod((SmoothMethod) comboBoxSmoothMethod.getSelectedData());
			if (numberSmoothDegree.isEnabled() == true && numberSmoothDegree.getSelectedItem() != null) {
				distanceAnalystParameter.setPathLineSmoothDegree(Integer.parseInt(numberSmoothDegree.getSelectedItem().toString()));
			}

			double originX = Double.parseDouble(numberOriginX.getSelectedItem().toString());
			double originY = Double.parseDouble(numberOriginY.getSelectedItem().toString());
			double targetX = Double.parseDouble(numberTargetX.getSelectedItem().toString());
			double targetY = Double.parseDouble(numberTargetY.getSelectedItem().toString());
			Point2D pointOrigin = new Point2D(originX, originY);
			Point2D pointTarget = new Point2D(targetX, targetY);
			PathLineResult pathLineResult = DistanceAnalyst.costPathLine(pointOrigin, pointTarget, distanceAnalystParameter);
			DatasetVectorInfo datasetVectorInfo = new DatasetVectorInfo();
			datasetVectorInfo.setName(resultDataset.getResultDatasource().getDatasets().getAvailableDatasetName(resultDataset.getDatasetName()));
			datasetVectorInfo.setType(DatasetType.LINE);
			DatasetVector result = resultDataset.getResultDatasource().getDatasets().create(datasetVectorInfo);
			result.setPrjCoordSys(src.getPrjCoordSys());
			if (pathLineResult != null) {
				Recordset recordset = result.getRecordset(false, CursorType.DYNAMIC);
				recordset.addNew(pathLineResult.getPathLine());
				recordset.getBatch().setMaxRecordCount(2000);
				recordset.getBatch().begin();
				recordset.getBatch().update();
				recordset.dispose();
			}
			isSuccessful = pathLineResult != null;
			parameters.getOutputs().getData(OUTPUT_DATA).setValue(result);

			fireRunning(new RunningEvent(this, 100, "finished"));
		} catch (Exception e) {
			Application.getActiveApplication().getOutput().output(e);
		}finally {
			DistanceAnalyst.removeSteppedListener(steppedListener);
		}

		return isSuccessful;
	}
}
