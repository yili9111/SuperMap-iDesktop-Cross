package com.supermap.desktop.geometryoperation.editor;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import com.supermap.data.EditHistory;
import com.supermap.data.EditType;
import com.supermap.data.GeoLine;
import com.supermap.data.GeoRegion;
import com.supermap.data.GeoStyle;
import com.supermap.data.Geometrist;
import com.supermap.data.Geometry;
import com.supermap.data.Recordset;
import com.supermap.desktop.Application;
import com.supermap.desktop.geometry.Abstract.ICompoundFeature;
import com.supermap.desktop.geometry.Abstract.IGeometry;
import com.supermap.desktop.geometry.Abstract.ILineFeature;
import com.supermap.desktop.geometry.Abstract.IRegionFeature;
import com.supermap.desktop.geometry.Implements.DGeoCompound;
import com.supermap.desktop.geometry.Implements.DGeometryFactory;
import com.supermap.desktop.geometryoperation.EditEnvironment;
import com.supermap.desktop.mapeditor.MapEditorProperties;
import com.supermap.desktop.utilties.GeometryUtilties;
import com.supermap.desktop.utilties.ListUtilties;
import com.supermap.mapping.Layer;
import com.supermap.mapping.TrackingLayer;
import com.supermap.ui.Action;
import com.supermap.ui.GeometrySelectedEvent;
import com.supermap.ui.GeometrySelectedListener;
import com.supermap.ui.MapControl;

// @formatter:off
/**
 * 擦除，选中面特性对象或者CAD复合对象时，激活功能。
 * 按下 Ctrl，切换擦除内部或者外部
 * @author highsad
 *
 */
// @formatter:on
public class EraseEditor extends AbstractEditor {

	// 用于在 trackingLayer 中做结果展示的 id
	private static final String TAG_ERASE = "Tag_EraseEditorResult";

	private boolean isEraseExternal = false;
	private Geometry srRegion = new GeoRegion(); // 用来擦除的面对象
	private GeoStyle trackingStyle = new GeoStyle(); // 用来在 trackingLayer 做展示的风格

	private MouseListener mouseListener = new MouseAdapter() {

		@Override
		public void mouseEntered(MouseEvent e) {
			EraseEditor.this.mouseEntered();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			EraseEditor.this.mouseExited();
		}
	};

	private KeyListener keyListener = new KeyAdapter() {

		@Override
		public void keyReleased(KeyEvent e) {
			EraseEditor.this.keyPressed(e);
		}
	};

	private GeometrySelectedListener geometrySelectedListener = new GeometrySelectedListener() {

		@Override
		public void geometrySelected(GeometrySelectedEvent arg0) {
			EraseEditor.this.geometrySelected(arg0);
		}
	};

	public EraseEditor() {
		super();
		this.trackingStyle.setLineColor(Color.RED);
		this.trackingStyle.setLineWidth(1);
	}

	@Override
	public Action getMapControlAction() {
		return Action.SELECT;
	}

	@Override
	public void activate(EditEnvironment environment) {
		registerEvents(environment);
		getSrRegion(environment);
		setIsEraseExternal(false);
		Application.getActiveApplication().getOutput().output(MapEditorProperties.getString("String_EraseEditor_EraseGeometry"));
	}

	@Override
	public void deactivate(EditEnvironment environment) {
		unregisterEvents(environment);
		clear(environment);
	}

	@Override
	public boolean enble(EditEnvironment environment) {
		return environment.getEditProperties().getSelectedGeometryCount() > 0
				&& ListUtilties.isListContainAny(environment.getEditProperties().getSelectedGeometryTypeFeatures(), IRegionFeature.class, ICompoundFeature.class);
	}

	@Override
	public boolean check(EditEnvironment environment) {
		return environment.getEditor() instanceof EraseEditor;
	}

	private void registerEvents(EditEnvironment environment) {
		MapControl mapControl = environment.getMapControl();
		mapControl.addMouseListener(this.mouseListener);
		mapControl.addKeyListener(this.keyListener);
		mapControl.addGeometrySelectedListener(this.geometrySelectedListener);
	}

	private void unregisterEvents(EditEnvironment environment) {
		MapControl mapControl = environment.getMapControl();
		mapControl.removeMouseListener(this.mouseListener);
		mapControl.removeKeyListener(this.keyListener);
		mapControl.removeGeometrySelectedListener(this.geometrySelectedListener);
	}

	private void mouseEntered() {

	}

	private void mouseExited() {

	}

	private void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
			setIsEraseExternal(!this.isEraseExternal);
		}
	}

	private void geometrySelected(GeometrySelectedEvent e) {
		MapControl mapControl = (MapControl) e.getSource();
		mapControl.getEditHistory().batchBegin();
		Recordset recordset = null; // 有选中可擦除几何对象（面特性、线特性）的可编辑记录集
		Geometry eraseResult = null; // 擦除结果

		try {
			Layer[] editableLayers = mapControl.getEditableLayers();

			if (editableLayers != null && editableLayers.length > 0) {
				for (int i = 0; i < editableLayers.length; i++) {
					Layer layer = editableLayers[i];

					if (layer.getSelection() != null && layer.getSelection().getCount() > 0) {

						// 单选模式，拿到一个就可以撤了
						recordset = layer.getSelection().toRecordset();
						break;
					}
				}
			}

			if (recordset != null) {
				eraseResult = erase(mapControl.getEditHistory(), recordset);

				if (eraseResult != null) {
					addResultTracking(mapControl, eraseResult);
				}
			} else {
				Application.getActiveApplication().getOutput().output(MapEditorProperties.getString("String_EraseEditor_LayerCannotEdit"));
			}
		} catch (Exception e2) {
			Application.getActiveApplication().getOutput().output(e2);
		} finally {
			mapControl.getEditHistory().batchEnd();
			mapControl.getMap().refresh();

			if (recordset != null) {
				recordset.close();
				recordset.dispose();
			}

			if (eraseResult != null) {
				eraseResult.dispose();
			}
		}
	}

	/**
	 * 擦除对象
	 * 
	 * @param geometry
	 * @return
	 */
	private Geometry erase(EditHistory editHistory, Recordset recordset) {
		Geometry result = null; // 擦除结果
		IGeometry geometry = null; // 被擦除对象

		try {
			recordset.moveFirst();
			geometry = DGeometryFactory.create(recordset.getGeometry());
			if (isEraseExternal) {
				result = eraseExternal(editHistory, recordset, geometry);
			} else {
				result = eraseInside(editHistory, recordset, geometry);
			}

			if (result != null) {
				editHistory.add(EditType.MODIFY, recordset, true);
				recordset.edit();
				recordset.setGeometry(result);
				recordset.update();
			}
		} finally {
			if (geometry != null) {
				geometry.dispose();
			}
		}
		return result;
	}

	/**
	 * 擦除内部
	 * 
	 * @param editHistory
	 * @param recordset
	 * @param geometry
	 *            被擦除对象
	 * @return
	 */
	private Geometry eraseInside(EditHistory editHistory, Recordset recordset, IGeometry geometry) {
		Geometry eraseResult = null;

		try {
			if (geometry instanceof IRegionFeature || geometry instanceof ILineFeature) {
				eraseResult = Geometrist.erase(this.srRegion, geometry.getGeometry());

				// 没有擦除结果，删除被擦除对象
				if (eraseResult == null) {
					editHistory.add(EditType.DELETE, recordset, true);
					recordset.delete();
					recordset.update();
				}
			} else if (geometry instanceof DGeoCompound) {
				eraseCompound((DGeoCompound) geometry);
			} else {
				Application.getActiveApplication().getOutput().output(MapEditorProperties.getString("String_EraseEditor_InvalidGeometryType"));
			}
		} catch (Exception e) {
			Application.getActiveApplication().getOutput().output(e);
		}
		return eraseResult;
	}

	/**
	 * 擦除外部
	 * 
	 * @param editHistory
	 * @param recordset
	 * @param geometry
	 *            被擦除对象
	 * @return
	 */
	private Geometry eraseExternal(EditHistory editHistory, Recordset recordset, IGeometry geometry) {
		Geometry eraseResult = null;

		try {
			if (geometry instanceof IRegionFeature || geometry instanceof ILineFeature) {
				eraseResult = Geometrist.clip(this.srRegion, geometry.getGeometry());

				// 没有擦除结果，删除被擦除对象
				if (eraseResult == null) {
					editHistory.add(EditType.DELETE, recordset, true);
					recordset.delete();
					recordset.update();
				}
			} else if (geometry instanceof DGeoCompound) {
				eraseCompound((DGeoCompound) geometry);
			} else {
				Application.getActiveApplication().getOutput().output(MapEditorProperties.getString("String_EraseEditor_InvalidGeometryType"));
			}
		} catch (Exception e) {
			Application.getActiveApplication().getOutput().output(e);
		}
		return eraseResult;
	}

	/**
	 * 擦除复合对象
	 * 
	 * @param geoCompound
	 * @return
	 */
	private Geometry eraseCompound(DGeoCompound geoCompound) {
		Geometry eraseResult = null;

		for (int i = geoCompound.getPartCount() - 1; i >= 0; i--) {
			IGeometry part = DGeometryFactory.create(geoCompound.getPart(i));
			Geometry partEraseResult = null; // 子对象擦除结果

			if (part instanceof DGeoCompound) {
				partEraseResult = eraseCompound(geoCompound);
			} else if (part instanceof IRegionFeature || part instanceof ILineFeature) {
				if (isEraseExternal) {
					partEraseResult = Geometrist.clip(this.srRegion, part.getGeometry());
				} else {
					partEraseResult = Geometrist.erase(this.srRegion, part.getGeometry());
				}
			}

			if (partEraseResult == null) {

				// 擦除结果为空，移除该子项
				geoCompound.removePart(i);
			} else {
				geoCompound.setPart(i, partEraseResult);
			}
		}
		return eraseResult;
	}

	private void setIsEraseExternal(boolean isEraseExternal) {
		this.isEraseExternal = isEraseExternal;

		if (isEraseExternal) {
			Application.getActiveApplication().getOutput().output(MapEditorProperties.getString("String_EraseEditor_EraseTipExternal"));
		} else {
			Application.getActiveApplication().getOutput().output(MapEditorProperties.getString("String_EraseEditor_EraseTipInside"));
		}
	}

	private void addResultTracking(MapControl mapControl, Geometry geometry) {
		if (mapControl != null) {
			mapControl.getMap().getTrackingLayer().add(geometry, TAG_ERASE);
		}
	}

	private void clearResultTracking(MapControl mapControl) {
		if (mapControl != null) {
			TrackingLayer trackingLayer = mapControl.getMap().getTrackingLayer();

			int index = trackingLayer.indexOf(TAG_ERASE);
			while (index >= 0) {
				trackingLayer.remove(index);
				index = trackingLayer.indexOf(TAG_ERASE);
			}
		}
	}

	// @formatter:off
	/**
	 * 获取用来擦除的面对象，将所有选中的面对象合并。只要重叠，做擦除就可能产生岛洞关系与预期不一致。
	 * 如果选中的对象中本就有复杂对象，则维持复杂对象的岛洞关系。
	 * 
	 * @param environment
	 */
	// @formatter:on
	private void getSrRegion(EditEnvironment environment) {
		List<Layer> selectedLayers = environment.getEditProperties().getSelectedLayers();

		for (int i = 0; i < selectedLayers.size(); i++) {
			Geometry unionLayer = GeometryUtilties.union(selectedLayers.get(i)); // 将该图层中选中的面对象进行合并处理
			this.srRegion = GeometryUtilties.union(this.srRegion, unionLayer, true);
		}
	}

	/**
	 * 编辑结束清理资源
	 */
	private void clear(EditEnvironment environment) {
		if (this.srRegion != null) {
			this.srRegion.dispose();
			this.srRegion = null;
		}

		clearResultTracking(environment.getMapControl());
	}
}