package com.supermap.desktop.newtheme;

import com.supermap.data.*;
import com.supermap.desktop.Application;
import com.supermap.desktop.mapview.MapViewProperties;
import com.supermap.desktop.properties.CommonProperties;
import com.supermap.desktop.ui.UICommonToolkit;
import com.supermap.desktop.ui.controls.*;
import com.supermap.desktop.utilties.MathUtilties;
import com.supermap.desktop.utilties.StringUtilties;
import com.supermap.mapping.*;
import com.supermap.ui.MapControl;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class ThemeLabelRangeContainer extends ThemeChangePanel {

	private static final long serialVersionUID = 1L;
	private transient ThemeLabel themeLabel;
	private transient Layer themeLabelLayer;
	private transient ThemeLabelPropertyPanel panelProperty;
	private transient ThemeLabelAdvancePanel panelAdvance;
	private JTabbedPane tabbedPane = new JTabbedPane();
	private JPanel panelSytle = new JPanel();

	private JLabel labelExpression = new JLabel();
	private JComboBox<String> comboBoxExpression = new JComboBox<String>();
	private JLabel labelRangeMethod = new JLabel();
	private JComboBox<String> comboBoxRangeMethod = new JComboBox<String>();
	private JLabel labelRangeCount = new JLabel();
	private JComboBox<String> comboBoxRangeCount = new JComboBox<String>();
	private JLabel labelRangeLength = new JLabel();
	private JSpinner spinnerRangeLength = new JSpinner();

	private JLabel labelRangeFormat = new JLabel();
	private JComboBox<String> comboBoxRangeFormat = new JComboBox<String>();
	private JLabel labelColorStyle = new JLabel();
	private ColorsComboBox comboBoxColorStyle = new ColorsComboBox();
	private JToolBar toolBar = new JToolBar();
	private JButton buttonMerge = new JButton();
	private JButton buttonSplit = new JButton();
	private JButton buttonVisible = new JButton();
	private JButton buttonStyle = new JButton();
	private JScrollPane scrollPane = new JScrollPane();
	private JTable tableLabelInfo = new JTable();

	private static String[] nameStrings = {MapViewProperties.getString("String_Title_Visible"), MapViewProperties.getString("String_Title_RangeValue"),
			MapViewProperties.getString("String_ThemeGraphItemManager_ClmExpression")};
	private transient DatasetVector datasetVector;
	private transient Map map;
	private String rangeExpression = "SmID";
	private transient RangeMode rangeMode = RangeMode.EQUALINTERVAL;
	private int labelCount = 5;
	private String captiontype = "<=X<";
	private boolean isRefreshAtOnce = true;
	private boolean isCustom = false;
	private boolean isNewTheme = false;
	private boolean isMergeOrSplit = false;

	private static final int TABLE_COLUMN_VISIBLE = 0;
	private static final int TABLE_COLUMN_RANGEVALUE = 1;
	private static final int TABLE_COLUMN_CAPTION = 2;

	private transient LocalActionListener actionListener = new LocalActionListener();
	private transient LocalMouseListener mouseListener = new LocalMouseListener();
	private transient LocalComboBoxItemListener itemListener = new LocalComboBoxItemListener();
	private transient LocalSpinnerChangeListener changeListener = new LocalSpinnerChangeListener();
	private transient LocalTableModelListener tableModelListener = new LocalTableModelListener();

	public ThemeLabelRangeContainer(DatasetVector datasetVector, ThemeLabel themeLabel) {
		this.datasetVector = datasetVector;
		this.themeLabel = themeLabel;
		this.map = initCurrentTheme(datasetVector);
		this.isNewTheme = true;
		initComponents();
		initResources();
		registActionListener();
	}

	/**
	 * @wbp.parser.constructor
	 */
	public ThemeLabelRangeContainer(Layer layer) {
		this.themeLabelLayer = layer;
		this.datasetVector = (DatasetVector) layer.getDataset();
		this.themeLabel = (ThemeLabel) layer.getTheme();
		this.map = ThemeGuideFactory.getMapControl().getMap();
		initComponents();
		initResources();
		registActionListener();
	}

	/**
	 * 初始化单值专题图
	 *
	 * @param dataset
	 * @return
	 */
	private Map initCurrentTheme(DatasetVector dataset) {
		MapControl mapControl = ThemeGuideFactory.getMapControl();
		if (null != mapControl) {
			this.themeLabelLayer = mapControl.getMap().getLayers().add(dataset, themeLabel, true);
			FieldInfo fieldInfo = datasetVector.getFieldInfos().get(0);
			if (fieldInfo.getType() == FieldType.INT16 || fieldInfo.getType() == FieldType.INT32 || fieldInfo.getType() == FieldType.INT64
					|| fieldInfo.getType() == FieldType.DOUBLE || fieldInfo.getType() == FieldType.SINGLE) {
				String item = datasetVector.getName() + "." + fieldInfo.getName();
				((ThemeLabel) themeLabelLayer.getTheme()).setLabelExpression(item);
				this.themeLabel = (ThemeLabel) themeLabelLayer.getTheme();
				this.themeLabel.setNumericPrecision(1);
			}
			UICommonToolkit.getLayersManager().getLayersTree().setSelectionRow(0);
			mapControl.getMap().refresh();
		}
		return mapControl.getMap();
	}

	/**
	 * 界面布局入口
	 */
	private void initComponents() {
		this.setLayout(new GridBagLayout());
		this.panelProperty = new ThemeLabelPropertyPanel(themeLabelLayer);
		this.panelAdvance = new ThemeLabelAdvancePanel(themeLabelLayer);
		this.tabbedPane.add(MapViewProperties.getString("String_Theme_Property"), this.panelProperty);
		this.tabbedPane.add(MapViewProperties.getString("String_Theme_Style"), this.panelSytle);
		this.tabbedPane.add(MapViewProperties.getString("String_Theme_Advanced"), this.panelAdvance);
		this.tabbedPane.setSelectedIndex(1);
		this.add(this.tabbedPane, new GridBagConstraintsHelper(0, 0, 1, 1).setAnchor(GridBagConstraints.CENTER).setFill(GridBagConstraints.BOTH)
				.setWeight(1, 1));
		initPanelStyle();
		this.comboBoxColorStyle.setSelectedIndex(3);
		if (isNewTheme) {
			refreshColor();
		}
	}

	/**
	 * 初始化属性界面
	 */
	private void initPanelStyle() {
		//@formatter:off
		initToolBar();
		getFieldComboBox(comboBoxExpression);
		initComboBoxRangeExpression();
		initComboBoxRangMethod();
		initComboBoxRangeCount();
		initComboBoxRangeFormat();
		this.panelSytle.setLayout(new GridBagLayout());
		this.panelSytle.add(this.labelExpression,     new GridBagConstraintsHelper(0, 0, 1, 1).setAnchor(GridBagConstraints.WEST).setInsets(2,10,2,10).setWeight(1, 0));
		this.comboBoxExpression.setEditable(true);
		this.panelSytle.add(this.comboBoxExpression,  new GridBagConstraintsHelper(1, 0, 1, 1).setAnchor(GridBagConstraints.WEST).setInsets(2,20,2,10).setWeight(1, 0).setFill(GridBagConstraints.HORIZONTAL));
		this.panelSytle.add(this.labelRangeMethod,    new GridBagConstraintsHelper(0, 1, 1, 1).setAnchor(GridBagConstraints.WEST).setInsets(2,10,2,10).setWeight(1, 0));
		this.panelSytle.add(this.comboBoxRangeMethod, new GridBagConstraintsHelper(1, 1, 1, 1).setAnchor(GridBagConstraints.WEST).setInsets(2,20,2,10).setWeight(1, 0).setFill(GridBagConstraints.HORIZONTAL));
		this.panelSytle.add(this.labelRangeCount,     new GridBagConstraintsHelper(0, 2, 1, 1).setAnchor(GridBagConstraints.WEST).setInsets(2,10,2,10).setWeight(1, 0));
		this.panelSytle.add(this.comboBoxRangeCount,  new GridBagConstraintsHelper(1, 2, 1, 1).setAnchor(GridBagConstraints.WEST).setInsets(2,20,2,10).setWeight(1, 0).setFill(GridBagConstraints.HORIZONTAL));
		this.panelSytle.add(this.labelRangeLength,    new GridBagConstraintsHelper(0, 3, 1, 1).setAnchor(GridBagConstraints.WEST).setInsets(2,10,2,10).setWeight(1, 0));
		spinnerRangeLength.setModel(new SpinnerNumberModel(new Double(0), null, null, new Double(1)));
		this.spinnerRangeLength.setEnabled(false);
		this.panelSytle.add(this.spinnerRangeLength,  new GridBagConstraintsHelper(1, 3, 1, 1).setAnchor(GridBagConstraints.WEST).setInsets(2,20,2,10).setWeight(1, 0).setFill(GridBagConstraints.HORIZONTAL));
		this.panelSytle.add(this.labelRangeFormat,      new GridBagConstraintsHelper(0, 4, 1, 1).setAnchor(GridBagConstraints.WEST).setInsets(2,10,2,10).setWeight(1, 0));
		this.panelSytle.add(this.comboBoxRangeFormat,   new GridBagConstraintsHelper(1, 4, 1, 1).setAnchor(GridBagConstraints.WEST).setInsets(2,20,2,10).setWeight(1, 0).setFill(GridBagConstraints.HORIZONTAL));
		this.panelSytle.add(this.labelColorStyle,       new GridBagConstraintsHelper(0, 5, 1, 1).setAnchor(GridBagConstraints.WEST).setInsets(2,10,2,10).setWeight(1, 0));
		this.panelSytle.add(this.comboBoxColorStyle,    new GridBagConstraintsHelper(1, 5, 1, 1).setAnchor(GridBagConstraints.WEST).setInsets(2,20,2,10).setWeight(1, 0).setFill(GridBagConstraints.HORIZONTAL));
		this.panelSytle.add(this.toolBar,               new GridBagConstraintsHelper(0, 6, 2, 1).setAnchor(GridBagConstraints.WEST).setInsets(2).setWeight(1, 0));
		this.panelSytle.add(this.scrollPane,            new GridBagConstraintsHelper(0, 7, 2, 1).setAnchor(GridBagConstraints.NORTH).setInsets(2).setWeight(1, 3).setFill(GridBagConstraints.BOTH));
		getTable();
		this.scrollPane.setViewportView(tableLabelInfo);
		//@formatter:on
	}

	/**
	 * 初始化表达式
	 */
	private void initComboBoxRangeExpression() {
		this.comboBoxExpression.setEditable(true);
		String expression = themeLabel.getRangeExpression();
		if (StringUtilties.isNullOrEmpty(expression)) {
			expression = "0";
		}
		this.comboBoxExpression.setSelectedItem(expression);
		if (!expression.equals(this.comboBoxExpression.getSelectedItem())) {
			this.comboBoxExpression.addItem(expression);
			this.comboBoxExpression.setSelectedItem(expression);
		}
	}

	/**
	 * 初始化分段方法项
	 */
	private void initComboBoxRangMethod() {
		this.comboBoxRangeMethod.setModel(new DefaultComboBoxModel<String>(new String[]{MapViewProperties.getString("String_RangeMode_EqualInterval"),
				MapViewProperties.getString("String_RangeMode_SquareRoot"), MapViewProperties.getString("String_RangeMode_StdDeviation"),
				MapViewProperties.getString("String_RangeMode_Logarithm"), MapViewProperties.getString("String_RangeMode_Quantile"),
				MapViewProperties.getString("String_RangeMode_CustomInterval") }));
		if (themeLabel.getRangeMode() == RangeMode.NONE) {
			this.comboBoxRangeMethod.setSelectedIndex(0);
		} else if (themeLabel.getRangeMode() == RangeMode.SQUAREROOT) {
			this.comboBoxRangeMethod.setSelectedIndex(1);
		} else if (themeLabel.getRangeMode() == RangeMode.STDDEVIATION) {
			this.comboBoxRangeMethod.setSelectedIndex(2);
		} else if (themeLabel.getRangeMode() == RangeMode.LOGARITHM) {
			this.comboBoxRangeMethod.setSelectedIndex(3);
		} else if (themeLabel.getRangeMode() == RangeMode.QUANTILE) {
			this.comboBoxRangeMethod.setSelectedIndex(4);
		} else if (themeLabel.getRangeMode() == RangeMode.CUSTOMINTERVAL) {
			this.comboBoxRangeMethod.setSelectedIndex(5);
		}
	}

	/**
	 * 初始化段数
	 */
	private void initComboBoxRangeCount() {
		comboBoxRangeCount.setModel(new DefaultComboBoxModel<String>(new String[]{"2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15",
				"16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32" }));
		comboBoxRangeCount.setEditable(true);
		int rangeCount = themeLabel.getCount();
		comboBoxRangeCount.setSelectedIndex(rangeCount - 2);

	}

	private void initComboBoxRangeFormat() {
		comboBoxRangeFormat.setModel(new DefaultComboBoxModel<String>(new String[] { "0-100", "0<=x<100" }));
		comboBoxRangeFormat.setSelectedIndex(1);
	}

	/**
	 * 资源化
	 */
	private void initResources() {
		this.labelExpression.setText(MapViewProperties.getString("String_label_Expression"));
		this.labelRangeMethod.setText(MapViewProperties.getString("String_Label_RangeMethed"));
		this.labelRangeCount.setText(MapViewProperties.getString("String_Label_RangeCount"));
		this.labelRangeLength.setText(MapViewProperties.getString("String_Label_RangeSize"));
		this.labelRangeFormat.setText(MapViewProperties.getString("String_Label_CaptionFormat"));
		this.labelColorStyle.setText(MapViewProperties.getString("String_Label_ColorScheme"));
		this.buttonMerge.setEnabled(false);
		this.buttonMerge.setToolTipText(MapViewProperties.getString("String_Title_Merge"));
		this.buttonSplit.setToolTipText(MapViewProperties.getString("String_Title_Split"));
		this.buttonStyle.setToolTipText(MapViewProperties.getString("String_Title_Sytle"));
		this.buttonVisible.setToolTipText(MapViewProperties.getString("String_Title_Visible"));
	}

	/**
	 * 表格初始化
	 *
	 * @return m_table
	 */
	private JTable getTable() {
		this.labelCount = this.themeLabel.getCount();
		DefaultTableModel defaultTableModel = new DefaultTableModel(new Object[this.labelCount][3], nameStrings) {
			@Override
			public Class getColumnClass(int column) {// 要这样定义table，要重写这个方法0，0的意思就是别的格子的类型都跟0,0的一样。
				if (TABLE_COLUMN_VISIBLE == column) {
					return getValueAt(0, 0).getClass();
				}
				return String.class;
			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				if (columnIndex == TABLE_COLUMN_RANGEVALUE || columnIndex == TABLE_COLUMN_CAPTION) {
					return true;
				}
				return false;
			}
		};
		this.tableLabelInfo.setModel(defaultTableModel);
		this.tableLabelInfo.setRowSelectionInterval(0, 0);
		this.tableLabelInfo.setRowHeight(20);
		TableColumn visibleColumn = this.tableLabelInfo.getColumn(MapViewProperties.getString("String_Title_Visible"));
		visibleColumn.setMaxWidth(40);
		initColumnIcon();
		this.tableLabelInfo.getModel().addTableModelListener(this.tableModelListener);
		return this.tableLabelInfo;
	}

	/**
	 * 填充图片和字段
	 */
	private void initColumnIcon() {
		for (int i = 0; i < this.labelCount; i++) {
			ThemeLabelItem rangeItem = this.themeLabel.getItem(i);
			boolean isVisible = rangeItem.isVisible();
			ImageIcon visibleIcon = InternalImageIconFactory.VISIBLE;
			if (!isVisible) {
				visibleIcon = InternalImageIconFactory.INVISIBLE;
			}
			this.tableLabelInfo.setValueAt(visibleIcon, i, TABLE_COLUMN_VISIBLE);
			if (i == labelCount - 1) {
				this.tableLabelInfo.setValueAt("Max", i, TABLE_COLUMN_RANGEVALUE);
			} else {
				this.tableLabelInfo.setValueAt(rangeItem.getEnd(), i, TABLE_COLUMN_RANGEVALUE);
			}

			String caption = rangeItem.getCaption();
			if (this.captiontype.contains("-")) {
				caption = caption.replaceAll("<= X <", "-");
				caption = caption.replaceAll("< X <", "-");
			} else if (this.captiontype.contains("<") && !caption.contains("X")) {
				caption = caption.replaceAll("-", "<= X <");
			}
			rangeItem.setCaption(caption);
			this.tableLabelInfo.setValueAt(rangeItem.getCaption(), i, TABLE_COLUMN_CAPTION);
		}
	}

	/**
	 * 表达式
	 *
	 * @return m_fieldComboBox
	 */
	private JComboBox<String> getFieldComboBox(JComboBox<String> comboBox) {
		int count = datasetVector.getFieldCount();
		for (int j = 0; j < count; j++) {
			FieldInfo fieldInfo = datasetVector.getFieldInfos().get(j);
			if (fieldInfo.getType() == FieldType.INT16 || fieldInfo.getType() == FieldType.INT32 || fieldInfo.getType() == FieldType.INT64
					|| fieldInfo.getType() == FieldType.DOUBLE || fieldInfo.getType() == FieldType.SINGLE) {
				String item = datasetVector.getName() + "." + fieldInfo.getName();
				comboBox.addItem(item);
			}
		}
		comboBox.addItem(MapViewProperties.getString("String_Combobox_Expression"));
		return comboBox;
	}

	/**
	 * 颜色方案改变时刷新颜色
	 */
	private void refreshColor() {
		if (comboBoxColorStyle != null) {
			int colorCount = ((Colors) comboBoxColorStyle.getSelectedItem()).getCount();
			Colors colors = (Colors) comboBoxColorStyle.getSelectedItem();
			int themeLabelCount = themeLabel.getCount();
			if (themeLabelCount > 0) {
				float ratio = (1f * colorCount) / (1f * themeLabelCount);
				setTextStyleColor(themeLabel.getItem(0).getStyle(), colors.get(0));
				setTextStyleColor(themeLabel.getItem(themeLabelCount - 1).getStyle(), colors.get(colorCount - 1));
				for (int i = 1; i < themeLabelCount - 1; i++) {
					int colorIndex = Math.round(i * ratio);
					if (colorIndex == colorCount) {
						colorIndex--;
					}
					setTextStyleColor(themeLabel.getItem(i).getStyle(), colors.get(colorIndex));
				}
			}
		}
	}

	/**
	 * 设置文本风格颜色
	 *
	 * @param textStyle 需要设置的风格
	 * @param color 设置的颜色
	 */
	private void setTextStyleColor(TextStyle textStyle, Color color) {
		textStyle.setForeColor(color);
	}

	/**
	 * 初始化工具条
	 */
	private void initToolBar() {
		this.toolBar.add(this.buttonMerge);
		this.toolBar.add(this.buttonSplit);
		this.toolBar.addSeparator();
		this.toolBar.add(this.buttonVisible);
		this.toolBar.add(this.buttonStyle);
		this.toolBar.addSeparator();
		this.buttonMerge.setIcon(InternalImageIconFactory.Merge);
		this.buttonSplit.setIcon(InternalImageIconFactory.Split);
		this.buttonStyle.setIcon(InternalImageIconFactory.STYLE);
		this.buttonVisible.setIcon(InternalImageIconFactory.VISIBLE);
	}

	/**
	 * 注册事件
	 */
	void registActionListener() {
		unregistActionListener();
		this.buttonVisible.addActionListener(this.actionListener);
		this.buttonStyle.addActionListener(this.actionListener);
		this.buttonMerge.addActionListener(this.actionListener);
		this.buttonSplit.addActionListener(this.actionListener);
		this.tableLabelInfo.addMouseListener(this.mouseListener);
		this.comboBoxColorStyle.addItemListener(this.itemListener);
		this.comboBoxExpression.addItemListener(this.itemListener);
		this.comboBoxRangeCount.addItemListener(this.itemListener);
		this.comboBoxRangeCount.getComponent(0).addMouseListener(this.mouseListener);
		this.comboBoxRangeMethod.addItemListener(this.itemListener);
		this.comboBoxRangeFormat.addItemListener(this.itemListener);
		this.spinnerRangeLength.addChangeListener(this.changeListener);
	}

	/**
	 * 注销事件
	 */
	public void unregistActionListener() {
		this.buttonVisible.removeActionListener(this.actionListener);
		this.buttonStyle.removeActionListener(this.actionListener);
		this.buttonMerge.removeActionListener(this.actionListener);
		this.buttonSplit.removeActionListener(this.actionListener);
		this.tableLabelInfo.removeMouseListener(this.mouseListener);
		this.comboBoxColorStyle.removeItemListener(this.itemListener);
		this.comboBoxExpression.removeItemListener(this.itemListener);
		this.comboBoxRangeCount.removeItemListener(this.itemListener);
		this.comboBoxRangeCount.getComponent(0).removeMouseListener(this.mouseListener);
		this.comboBoxRangeMethod.removeItemListener(this.itemListener);
		this.comboBoxRangeFormat.removeItemListener(this.itemListener);
		this.spinnerRangeLength.removeChangeListener(this.changeListener);
		this.tableLabelInfo.getModel().removeTableModelListener(this.tableModelListener);
	}

	class LocalActionListener implements ActionListener {

		// private TextStyleDialog textStyleDialog;

		@Override
		public void actionPerformed(ActionEvent e) {
			int[] selectedRows = tableLabelInfo.getSelectedRows();
			if (e.getSource() == buttonMerge) {
				if (selectedRows.length == tableLabelInfo.getRowCount()) {
					UICommonToolkit.showConfirmDialog(MapViewProperties.getString("String_Warning_RquiredTwoFieldForRange"));
				} else {
					// 合并选中项
					mergeItem();
				}
			} else if (e.getSource() == buttonSplit) {
				// 拆分选中项
				splitItem();
			} else if (e.getSource() == buttonVisible) {
				// 批量修改分段的可见状态
				setItemVisble();
			} else if (e.getSource() == buttonStyle) {
				// 批量修文本风格
				setItemTextSytle();
			}
			if (isRefreshAtOnce) {
				firePropertyChange("ThemeChange", null, null);
				ThemeGuideFactory.refreshMapAndLayer(map, themeLabelLayer.getName(), true);
			}
		}

		/**
		 * 拆分项
		 */
		private void splitItem() {
			int selectRow = tableLabelInfo.getSelectedRow();
			if (selectRow >= 0) {
				ThemeLabelItem item = themeLabel.getItem(selectRow);
				double splitValue = (item.getEnd() + item.getStart()) / 2;
				if (selectRow == 0) {
					// 第零条数据的拆分中值
					splitValue = (item.getEnd() + ((int) item.getEnd()) - 1) / 2;
				}
				if (selectRow == tableLabelInfo.getRowCount() - 1) {
					// 最后一条的拆分中值
					splitValue = (item.getStart() + ((int) item.getStart()) + 1) / 2;
				}
				String diff = new DecimalFormat("#.####").format(item.getEnd() - item.getStart());
				// 首尾项不同时才能进行拆分
				if (!"0.0001".equals(diff)) {
					String startCaption = MessageFormat.format(MapViewProperties.getString("String_RangeFormat"), String.valueOf(item.getStart()),
							String.valueOf(splitValue));
					String endCaption = MessageFormat.format(MapViewProperties.getString("String_RangeFormat"), String.valueOf(splitValue),
							String.valueOf(item.getEnd()));
					themeLabel.split(selectRow, splitValue, item.getStyle(), startCaption, item.getStyle(), endCaption);
					isMergeOrSplit = true;
					getTable();
					labelCount = themeLabel.getCount();
					comboBoxRangeCount.setSelectedItem(String.valueOf(labelCount));
					tableLabelInfo.setRowSelectionInterval(selectRow, selectRow);
				}
			}
		}

		/**
		 * 合并项
		 */
		private void mergeItem() {
			int[] selectedRows = tableLabelInfo.getSelectedRows();
			int startIndex = selectedRows[0];
			int endIndex = selectedRows[selectedRows.length - 1];
			ThemeLabelItem startItem = themeLabel.getItem(startIndex);
			ThemeLabelItem endItem = themeLabel.getItem(endIndex);
			// 合并后的子项的表达式
			String caption = MessageFormat.format(MapViewProperties.getString("String_RangeFormat"), String.valueOf(startItem.getStart()),
					String.valueOf(endItem.getEnd()));
			themeLabel.merge(startIndex, selectedRows.length, startItem.getStyle(), caption);
			isMergeOrSplit = true;
			labelCount = themeLabel.getCount();
			comboBoxRangeCount.setSelectedItem(String.valueOf(labelCount));
			getTable();
			tableLabelInfo.setRowSelectionInterval(selectedRows[0], selectedRows[0]);
			buttonMerge.setEnabled(false);
			buttonSplit.setEnabled(true);
		}

		/**
		 * 设置分段项是否可见
		 */
		private void setItemVisble() {
			int[] selectedRow = tableLabelInfo.getSelectedRows();
			// 有不可见的项就全部设置为不可见，全部不可见，或者全部可见就设置为相反状态
			if (hasInvisible(selectedRow) && !allItemInvisible(selectedRow)) {
				for (int i = 0; i < selectedRow.length; i++) {
					((ThemeLabel) themeLabelLayer.getTheme()).getItem(selectedRow[i]).setVisible(false);
				}
			} else {
				for (int i = 0; i < selectedRow.length; i++) {
					resetVisible(selectedRow[i]);
				}
			}
			getTable();
			for (int i = 0; i < selectedRow.length; i++) {
				tableLabelInfo.addRowSelectionInterval(selectedRow[i], selectedRow[i]);
			}
		}

		/**
		 * 判断选中项是否全部不可见
		 *
		 * @param selectedRows
		 * @return
		 */
		private boolean allItemInvisible(int[] selectedRows) {
			int count = 0;
			boolean allItemInvisible = false;
			for (int i = 0; i < selectedRows.length; i++) {
				if (!((ThemeLabel) themeLabelLayer.getTheme()).getItem(selectedRows[i]).isVisible()) {
					count++;
				}
			}
			if (count == selectedRows.length) {
				allItemInvisible = true;
			}
			return allItemInvisible;
		}

		/**
		 * 判断选中项中是否存在不可见子项
		 *
		 * @param selectedRows
		 * @return
		 */
		private boolean hasInvisible(int[] selectedRows) {
			boolean hasInvisible = false;
			for (int i = 0; i < selectedRows.length; i++) {
				if (!((ThemeLabel) themeLabelLayer.getTheme()).getItem(selectedRows[i]).isVisible()) {
					hasInvisible = true;
				}
			}
			return hasInvisible;
		}

		/**
		 * 重置可见选项
		 *
		 * @param selectRow 要重置的行
		 */
		private void resetVisible(int selectRow) {
			ThemeLabelItem tempThemeRangeItem = ((ThemeLabel) themeLabelLayer.getTheme()).getItem(selectRow);
			boolean visible = tempThemeRangeItem.isVisible();
			if (visible) {
				tempThemeRangeItem.setVisible(false);
				tableLabelInfo.setValueAt(InternalImageIconFactory.INVISIBLE, selectRow, TABLE_COLUMN_VISIBLE);
			} else {
				tempThemeRangeItem.setVisible(true);
				tableLabelInfo.setValueAt(InternalImageIconFactory.VISIBLE, selectRow, TABLE_COLUMN_VISIBLE);
			}
		}

		/**
		 * 批量设置文本风格
		 */
		private void setItemTextSytle() {
			int[] selectedRow = tableLabelInfo.getSelectedRows();
			int width = buttonStyle.getWidth();
			int height = buttonStyle.getHeight();
			int x = buttonStyle.getLocationOnScreen().x - 4 * width;
			int y = buttonStyle.getLocationOnScreen().y + height;
			TextStyle textStyle = new TextStyle();
			if (selectedRow.length == 1) {
				textStyle = themeLabel.getItem(selectedRow[0]).getStyle();
				TextStyleDialog textStyleDialog = new TextStyleDialog(textStyle, map);
				textStyleDialog.setLocation(x, y);
				textStyleDialog.setVisible(true);
			} else {
				List<TextStyle> list = new ArrayList<TextStyle>();
				for (int i = 0; i < selectedRow.length; i++) {
					list.add(themeLabel.getItem(selectedRow[i]).getStyle());
				}
				TextStyleDialog textStyleDialog = new TextStyleDialog(list, map);
				textStyleDialog.setLocation(x, y);
				textStyleDialog.setVisible(true);
			}
			for (int i = 0; i < selectedRow.length; i++) {
				tableLabelInfo.addRowSelectionInterval(selectedRow[i], selectedRow[i]);
			}
		}
	}

	class LocalMouseListener extends MouseAdapter {
		@Override
		public void mouseReleased(MouseEvent e) {
			int[] selectedRows = tableLabelInfo.getSelectedRows();
			if (selectedRows.length == 1) {
				buttonMerge.setEnabled(false);
				buttonSplit.setEnabled(true);
			} else if (selectedRows.length >= 2) {
				buttonSplit.setEnabled(false);
			}

			if (selectedRows.length >= 2 && MathUtilties.isContiuityArray(selectedRows)) {
				buttonMerge.setEnabled(true);
			} else {
				buttonMerge.setEnabled(false);
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getSource() == tableLabelInfo && 1 == e.getClickCount() && tableLabelInfo.getSelectedColumn() == TABLE_COLUMN_VISIBLE
					&& tableLabelInfo.getSelectedRows().length == 1) {
				int selectRow = tableLabelInfo.getSelectedRow();
				ThemeLabelItem item = themeLabel.getItem(selectRow);
				boolean isVisible = item.isVisible();
				if (isVisible) {
					item.setVisible(false);
					tableLabelInfo.setValueAt(InternalImageIconFactory.INVISIBLE, selectRow, TABLE_COLUMN_VISIBLE);
				} else {
					item.setVisible(true);
					tableLabelInfo.setValueAt(InternalImageIconFactory.VISIBLE, selectRow, TABLE_COLUMN_VISIBLE);
				}
				tableLabelInfo.setRowSelectionInterval(selectRow, selectRow);
				if (isRefreshAtOnce) {
					firePropertyChange("ThemeChange", null, null);
					ThemeGuideFactory.refreshMapAndLayer(map, themeLabelLayer.getName(), true);
				}
			} else if (e.getSource() == comboBoxRangeCount.getComponent(0)) {
				isMergeOrSplit = false;
			}
		}
	}

	class LocalComboBoxItemListener implements ItemListener {

		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				if (e.getSource() == comboBoxColorStyle) {
					// 修改颜色方案
					refreshColor();
					getTable();
				} else if (e.getSource() == comboBoxExpression) {
					// sql表达式
					getSqlExpression(comboBoxExpression);
					// 修改表达式
					setFieldInfo();
				} else if (e.getSource() == comboBoxRangeCount && !isCustom && !isMergeOrSplit) {
					// 修改段数
					setRangeCount();
				} else if (e.getSource() == comboBoxRangeMethod) {
					// 设置分段方法
					setRangeMethod();
				} else if (e.getSource() == comboBoxRangeFormat) {
					// 设置标题格式
					setRangeFormat();
				}
				if (isRefreshAtOnce) {
					firePropertyChange("ThemeChange", null, null);
					ThemeGuideFactory.refreshMapAndLayer(map, themeLabelLayer.getName(), true);
				}
			}
		}

		/**
		 * 设置标题格式
		 */
		private void setRangeFormat() {
			int count = comboBoxRangeFormat.getSelectedIndex();
			if (0 == count) {
				captiontype = "-";
			} else {
				captiontype = "<=x<";
			}
			getTable();
		}

		private void setRangeCount() {
			labelCount = Integer.valueOf(comboBoxRangeCount.getSelectedItem().toString());
			resetThemeInfo();
		}

		/**
		 * 设置分段方法
		 */
		private void setRangeMethod() {
			String rangeMethod = comboBoxRangeMethod.getSelectedItem().toString();
			if (rangeMethod.equals(MapViewProperties.getString("String_RangeMode_EqualInterval"))) {
				// 等距分段
				rangeMode = RangeMode.EQUALINTERVAL;
				comboBoxRangeCount.setEnabled(true);
				spinnerRangeLength.setEnabled(false);
				isCustom = false;
			} else if (rangeMethod.equals(MapViewProperties.getString("String_RangeMode_SquareRoot"))) {
				// 平方根分度
				rangeMode = RangeMode.SQUAREROOT;
				comboBoxRangeCount.setEnabled(true);
				spinnerRangeLength.setEnabled(false);
				isCustom = false;
			} else if (rangeMethod.equals(MapViewProperties.getString("String_RangeMode_StdDeviation"))) {
				// 标准差分段
				rangeMode = RangeMode.STDDEVIATION;
				comboBoxRangeCount.setEnabled(false);
				spinnerRangeLength.setEnabled(false);
				isCustom = false;
			} else if (rangeMethod.equals(MapViewProperties.getString("String_RangeMode_Logarithm"))) {
				// 对数分度
				rangeMode = RangeMode.LOGARITHM;
				comboBoxRangeCount.setEnabled(true);
				spinnerRangeLength.setEnabled(false);
				isCustom = false;
			} else if (rangeMethod.equals(MapViewProperties.getString("String_RangeMode_Quantile"))) {
				// 等计数分段
				rangeMode = RangeMode.QUANTILE;
				comboBoxRangeCount.setEnabled(true);
				spinnerRangeLength.setEnabled(false);
				isCustom = false;
			}
			resetThemeInfo();
			if (rangeMethod.equals(MapViewProperties.getString("String_RangeMode_CustomInterval"))) {
				// 自定义分段
				rangeMode = RangeMode.CUSTOMINTERVAL;
				double defaultRangeCount = 0;
				if (themeLabel.getCount() > 2) {
					defaultRangeCount = Double.valueOf(new DecimalFormat("0").format(themeLabel.getItem(1).getEnd() - themeLabel.getItem(1).getStart()));
				} else {
					defaultRangeCount = Double.valueOf(new DecimalFormat("0").format(themeLabel.getItem(0).getEnd()));
				}
				spinnerRangeLength.setValue(defaultRangeCount);
				comboBoxRangeCount.setEnabled(false);
				spinnerRangeLength.setEnabled(true);
				makeDefaultAsCustom();
			}
		}

		/**
		 * 字段表达式
		 */
		private void setFieldInfo() {
			rangeExpression = comboBoxExpression.getSelectedItem().toString();
			resetThemeInfo();
		}

		/**
		 * 重建专题图
		 */
		private void resetThemeInfo() {
			if (rangeExpression.isEmpty()) {
				comboBoxExpression.setSelectedIndex(0);
			}
			if (UniqueValueCountUtil.hasNegative(datasetVector, rangeExpression) && rangeMode == RangeMode.SQUAREROOT) {
				// 有负数且为平方根分段
				JOptionPane.showMessageDialog(
						null,
						MessageFormat.format(MapViewProperties.getString("String_MakeTheme_Error1"), rangeExpression,
								MapViewProperties.getString("String_RangeMode_SquareRoot")), CommonProperties.getString("String_Error"),
						JOptionPane.ERROR_MESSAGE);
				comboBoxRangeMethod.setSelectedIndex(0);
			} else if (UniqueValueCountUtil.hasNegative(datasetVector, rangeExpression) && rangeMode == RangeMode.LOGARITHM) {
				// 有负数且为对数分段
				JOptionPane.showMessageDialog(
						null,
						MessageFormat.format(MapViewProperties.getString("String_MakeTheme_Error1"), rangeExpression,
								MapViewProperties.getString("String_RangeMode_Logarithm")), CommonProperties.getString("String_Error"),
						JOptionPane.ERROR_MESSAGE);
				comboBoxRangeMethod.setSelectedIndex(0);
			} else if (labelCount < 2 || labelCount > 32) {
				// 段数小于2，或者段数大于最大值
				comboBoxRangeCount.setSelectedItem(String.valueOf(themeLabel.getCount()));
			} else {
				ThemeLabel theme = ThemeLabel.makeDefault(datasetVector, rangeExpression, rangeMode, labelCount, ColorGradientType.GREENRED);
				if (null == theme) {
					// 专题图为空，提示专题图更新失败
					JOptionPane.showMessageDialog(null, MapViewProperties.getString("String_Theme_UpdataFailed"), CommonProperties.getString("String_Error"),
							JOptionPane.ERROR_MESSAGE);
				} else {
					refreshThemeLabel(theme);
				}
			}
		}

		/**
		 * 获取表达式项
		 *
		 * @param jComboBoxField
		 */
		private void getSqlExpression(JComboBox<String> jComboBoxField) {
			// 判断是否为“表达式”项
			if (MapViewProperties.getString("String_Combobox_Expression").equals(jComboBoxField.getSelectedItem())) {
				SQLExpressionDialog sqlDialog = new SQLExpressionDialog();
				int allItems = jComboBoxField.getItemCount();
				Dataset[] datasets = new Dataset[1];
				datasets[0] = datasetVector;
				ArrayList<FieldType> fieldTypes = new ArrayList<FieldType>();
				fieldTypes.add(FieldType.INT16);
				fieldTypes.add(FieldType.INT32);
				fieldTypes.add(FieldType.INT64);
				fieldTypes.add(FieldType.DOUBLE);
				fieldTypes.add(FieldType.SINGLE);

				DialogResult dialogResult = sqlDialog.showDialog(datasets, fieldTypes, themeLabel.getRangeExpression());
				if (dialogResult == DialogResult.OK) {
					String filter = sqlDialog.getQueryParameter().getAttributeFilter();
					if (filter != null && !filter.isEmpty()) {
						jComboBoxField.insertItemAt(filter, allItems - 1);
						jComboBoxField.setSelectedIndex(allItems - 1);
					} else {
						jComboBoxField.setSelectedItem(themeLabel.getRangeExpression());
					}
				} else {
					jComboBoxField.setSelectedItem(themeLabel.getRangeExpression());
				}

			}
		}

	}

	/**
	 * 刷新theme
	 *
	 * @param theme
	 */
	private void refreshThemeLabel(ThemeLabel theme) {
		if (null != theme) {
			((ThemeLabel) themeLabelLayer.getTheme()).clear();
			if (0 < theme.getCount()) {
				for (int i = 0; i < theme.getCount(); i++) {
					if (null != theme.getItem(i)) {
						((ThemeLabel) themeLabelLayer.getTheme()).addToTail(theme.getItem(i), true);
					}
				}
			}
			this.themeLabel = (ThemeLabel) themeLabelLayer.getTheme();
			this.themeLabel.setRangeExpression(rangeExpression);
			refreshColor();
			getTable();
		} else {
			UICommonToolkit.showConfirmDialog(MapViewProperties.getString("String_Theme_UpdataFailed"));
		}
		if (2 <= this.themeLabel.getCount()) {
			this.labelCount = this.themeLabel.getCount();
			this.comboBoxRangeCount.setSelectedIndex(this.labelCount - 2);
		}
	}

	/**
	 * 创建自定义的分段专题图
	 */
	private void makeDefaultAsCustom() {
		double rangeLength = (double) spinnerRangeLength.getValue();
		if (rangeLength > 0) {
			ThemeLabel theme = ThemeLabel.makeDefault(datasetVector, rangeExpression, rangeMode, rangeLength, ColorGradientType.GREENRED);
			if (null == theme || theme.getCount() == 0) {
				// 专题图为空，提示专题图更新失败
				JOptionPane.showMessageDialog(null, MapViewProperties.getString("String_Theme_UpdataFailed"), CommonProperties.getString("String_Error"),
						JOptionPane.ERROR_MESSAGE);
			} else {
				this.isCustom = true;
				refreshThemeLabel(theme);
			}
		}
	}

	class LocalSpinnerChangeListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent e) {
			makeDefaultAsCustom();
		}

	}

	/**
	 * 判断段值是否合法
	 *
	 * @return
	 */
	public boolean isRightRangeValue(String rangeValue, int selectRow) {
		boolean isRightValue = false;
		double range = Double.parseDouble(rangeValue);
		if (selectRow == 0) {
			double nextValue = themeLabel.getItem(selectRow + 1).getEnd();
			if (nextValue - range > 0.0) {
				isRightValue = true;
			}
		} else if (selectRow != tableLabelInfo.getRowCount() - 1) {
			double prewValue = themeLabel.getItem(selectRow - 1).getEnd();
			double nextValue = themeLabel.getItem(selectRow + 1).getEnd();
			if (nextValue - range > 0.0 && range - prewValue > 0.0) {
				isRightValue = true;
			}
		}
		return isRightValue;
	}

	class LocalTableModelListener implements TableModelListener {

		@Override
		public void tableChanged(TableModelEvent arg0) {
			int selectRow = arg0.getFirstRow();
			int selectColumn = arg0.getColumn();
			try {
				if (selectColumn == TABLE_COLUMN_RANGEVALUE && !StringUtilties.isNullOrEmptyString(tableLabelInfo.getValueAt(selectRow, selectColumn))) {
					String rangeValue = tableLabelInfo.getValueAt(selectRow, selectColumn).toString();
					if ((StringUtilties.isNumber(rangeValue) && isRightRangeValue(rangeValue, selectRow))
							&& (selectRow != tableLabelInfo.getRowCount() - 1)) {
						// 如果输入为数值且段值合法时修改段值
						themeLabel.getItem(selectRow).setEnd(Double.valueOf(rangeValue));
						String end = String.valueOf(themeLabel.getItem(selectRow).getEnd());
						String caption = themeLabel.getItem(selectRow).getCaption();
						caption = caption.replace(caption.substring(caption.lastIndexOf("<") + 1, caption.length()), end);
						themeLabel.getItem(selectRow).setCaption(caption);
						if (selectRow != themeLabel.getCount() - 1) {
							String nextCaption = themeLabel.getItem(selectRow + 1).getCaption();
							nextCaption = nextCaption.replace(nextCaption.substring(0, nextCaption.indexOf("<")), end);
							themeLabel.getItem(selectRow + 1).setCaption(nextCaption);
						}
					}
				} else if (selectColumn == TABLE_COLUMN_CAPTION && !StringUtilties.isNullOrEmptyString(tableLabelInfo.getValueAt(selectRow, selectColumn))) {
					String caption = tableLabelInfo.getValueAt(selectRow, selectColumn).toString();
					themeLabel.getItem(selectRow).setCaption(caption);
				}
				if (isRefreshAtOnce) {
					firePropertyChange("ThemeChange", null, null);
					ThemeGuideFactory.refreshMapAndLayer(map, themeLabelLayer.getName(), true);
				}
				getTable();
				tableLabelInfo.addRowSelectionInterval(selectRow, selectRow);
			} catch (Exception e) {
				Application.getActiveApplication().getOutput().output(e);
			}
		}

	}

	public boolean isRefreshAtOnece() {
		return isRefreshAtOnce;
	}

	public void setRefreshAtOnece(boolean isRefreshAtOnece) {
		this.isRefreshAtOnce = isRefreshAtOnece;
	}

	public Layer getThemeLabelLayer() {
		return themeLabelLayer;
	}

	public void setThemeLabelLayer(Layer themeLabelLayer) {
		this.themeLabelLayer = themeLabelLayer;
	}

	@Override
	public Theme getCurrentTheme() {
		return themeLabel;
	}

}
