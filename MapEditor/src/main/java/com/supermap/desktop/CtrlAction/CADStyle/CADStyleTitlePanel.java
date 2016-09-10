package com.supermap.desktop.CtrlAction.CADStyle;

import com.supermap.data.*;
import com.supermap.desktop.Application;
import com.supermap.desktop.controls.ControlsProperties;
import com.supermap.desktop.controls.utilities.ControlsResources;
import com.supermap.desktop.controls.utilities.SymbolDialogFactory;
import com.supermap.desktop.dialog.symbolDialogs.ISymbolApply;
import com.supermap.desktop.dialog.symbolDialogs.JpanelSymbols.*;
import com.supermap.desktop.dialog.symbolDialogs.SymbolDialog;
import com.supermap.desktop.mapeditor.MapEditorProperties;
import com.supermap.desktop.ui.controls.DialogResult;
import com.supermap.desktop.ui.controls.GridBagConstraintsHelper;
import com.supermap.desktop.utilities.CursorUtilities;
import com.supermap.desktop.utilities.MapUtilities;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by xie on 2016/8/26.
 */
public class CADStyleTitlePanel extends JPanel {
    private final CADStyleContainer parent;
    private int styleType;
    private Color COLOR_SYSTEM_DEFAULT;
    private Color COLOR_SYSTEM_SELECTED = new Color(185, 214, 244);
    public static final int GEOPOINTTYPE = 0;
    public static final int GEOLINETYPE = 1;
    public static final int GEOREGIONTYPE = 2;
    private JPanel panelMore;
    private JLabel buttonMoreImage;
    private JLabel labelTitle;
    private Recordset recordset;
    private EditHistory editHistory;
    private JPanelSymbols panelSymbols;
    private JScrollPane jScrollPane;

    private SymbolSelectedChangedListener panelSymbolsListener = new SymbolSelectedChangedListener() {

        @Override
        public void SymbolSelectedChangedEvent(Symbol symbol) {
            //此处若修改了recordset需要重新获取recordset
            if (null != recordset) {
                recordset.dispose();
            }
            recordset = MapUtilities.getActiveMap().findSelection(true)[0].toRecordset();
            parent.setModify(true);
            resetSymbol(symbol);
        }

        @Override
        public void SymbolSelectedDoubleClicked() {
            // Do nothing
        }
    };

    public CADStyleTitlePanel(CADStyleContainer parent, int styleType) {
        this.parent = parent;
        this.styleType = styleType;
        this.editHistory = new EditHistory();
        this.COLOR_SYSTEM_DEFAULT = getBackground();
        initComponents();
        initResources();
        registEvents();
    }

    public void enabled(boolean enabled) {
        this.panelSymbols.setVisible(enabled);
        this.buttonMoreImage.setEnabled(enabled);
        this.labelTitle.setEnabled(enabled);
        this.jScrollPane.setVisible(enabled);
        this.buttonMoreImage.setVisible(enabled);
        this.labelTitle.setVisible(enabled);
        if (null != recordset) {
            recordset.close();
            recordset.dispose();
        }
    }

    private void registEvents() {
        this.panelSymbols.addSymbolSelectedChangedListener(this.panelSymbolsListener);
    }

    private void initResources() {
        switch (styleType) {
            case GEOPOINTTYPE:
                this.setBorder(new TitledBorder(MapEditorProperties.getString("String_Point")));
                break;
            case GEOLINETYPE:
                this.setBorder(new TitledBorder(MapEditorProperties.getString("String_Line")));
                break;
            case GEOREGIONTYPE:
                this.setBorder(new TitledBorder(MapEditorProperties.getString("String_Fill")));
                break;
            default:
                break;
        }
    }

    private void initComponents() {
        Resources resources = Application.getActiveApplication().getWorkspace().getResources();
        if (styleType == GEOPOINTTYPE) {
            panelSymbols = new JPanelSymbolsPoint();
            panelSymbols.setSymbolGroup(resources, resources.getMarkerLibrary().getRootGroup());
            setLayout(panelSymbols);
            this.setPreferredSize(new Dimension(160, 220));
        } else if (styleType == GEOLINETYPE) {
            panelSymbols = new JPanelSymbolsLine();
            panelSymbols.setSymbolGroup(resources, resources.getLineLibrary().getRootGroup());
            setLayout(panelSymbols);
            this.setPreferredSize(new Dimension(160, 220));
        } else {
            panelSymbols = new JPanelSymbolsFill();
            panelSymbols.setSymbolGroup(resources, resources.getFillLibrary().getRootGroup());
            setLayout(panelSymbols);
            this.setPreferredSize(new Dimension(160, 300));
        }
    }


    private GeoStyle getGeoStyle() {
        GeoStyle geoStyle = new GeoStyle();
        if (null != recordset) {
            recordset.dispose();
        }
        recordset = MapUtilities.getActiveMap().findSelection(true)[0].toRecordset();
        if (null == recordset.getGeometry().getStyle()) {
            return geoStyle;
        }
        recordset.moveFirst();
        while (!recordset.isEOF()) {
            if (styleType == GEOPOINTTYPE) {
                geoStyle = recordset.getGeometry().getStyle().clone();
                break;
            } else if (styleType == GEOLINETYPE) {
                geoStyle = recordset.getGeometry().getStyle().clone();
                break;
            } else if (styleType == GEOREGIONTYPE) {
                geoStyle = recordset.getGeometry().getStyle().clone();
                break;
            } else {
                recordset.moveNext();
            }
        }
        return geoStyle;
    }

    private void resetRecordsetGeoStyle() {
        GeoStyle beforeGeoStyle = getGeoStyle();
        SymbolType symbolType = null;
        if (styleType == GEOPOINTTYPE) {
            symbolType = SymbolType.MARKER;
        } else if (styleType == GEOLINETYPE) {
            symbolType = SymbolType.LINE;
        } else {
            symbolType = SymbolType.FILL;
        }

        GeoStyle geostyle = changeGeoStyle(beforeGeoStyle, symbolType, new ISymbolApply() {
            @Override
            public void apply(GeoStyle geoStyle) {
                if (MapUtilities.getActiveMap().findSelection(true).length > 0) {
                    parent.setModify(false);
                    resetGeoStyle(recordset, geoStyle);
                }
            }
        });
        if (geostyle != null) {
            if (MapUtilities.getActiveMap().findSelection(true).length > 0) {
                parent.setModify(false);
                resetGeoStyle(MapUtilities.getActiveMap().findSelection(true)[0].toRecordset(), geostyle);
            }
        }
    }

    private GeoStyle changeGeoStyle(GeoStyle beforeStyle, SymbolType symbolType, ISymbolApply symbolApply) {
        GeoStyle result = null;
        SymbolDialog symbolDialog = null;
        try {
            CursorUtilities.setWaitCursor();
            symbolDialog = SymbolDialogFactory.getSymbolDialog(symbolType);
            DialogResult dialogResult = symbolDialog.showDialog(beforeStyle, symbolApply);
            if (dialogResult == DialogResult.OK) {
                result = symbolDialog.getCurrentGeoStyle();
                panelMore.removeMouseListener(null);
            }
        } catch (Exception ex) {
            Application.getActiveApplication().getOutput().output(ex);
        } finally {
            CursorUtilities.setDefaultCursor();
        }
        return result;
    }

    private void resetSymbol(Symbol symbol) {
        if (null == symbol) {
            if (styleType == GEOREGIONTYPE) {
                parent.setSpinnerFillOpaqueEnable(true);
            } else if (styleType == GEOPOINTTYPE) {
                parent.setSymstemPointEnable(false);
            }
        } else {
            if (styleType == GEOREGIONTYPE) {
                parent.setSpinnerFillOpaqueEnable(false);
            } else if (styleType == GEOPOINTTYPE) {
                parent.setSymstemPointEnable(true);
            }
        }
        recordset.moveFirst();
        while (!recordset.isEOF()) {
            editHistory.add(EditType.MODIFY, recordset, true);
            if (!recordset.isReadOnly()) {
                recordset.edit();
                Geometry tempGeometry = recordset.getGeometry().clone();
                GeoStyle geoStyle = tempGeometry.getStyle().clone();
                if (null == tempGeometry.getStyle()) {
                    geoStyle = new GeoStyle();
                }
                if (tempGeometry instanceof GeoPoint && styleType == GEOPOINTTYPE) {
                    // 修改点符号
                    geoStyle.setMarkerSymbolID(panelSymbols.getCurrentGeoStyle().getMarkerSymbolID());
                    geoStyle.setMarkerAngle(panelSymbols.getCurrentGeoStyle().getMarkerAngle());
                    geoStyle.setMarkerSize(panelSymbols.getCurrentGeoStyle().getMarkerSize());
                }
                if ((tempGeometry instanceof GeoLine || tempGeometry instanceof GeoRegion) && styleType == GEOLINETYPE) {
                    geoStyle.setLineSymbolID(panelSymbols.getCurrentGeoStyle().getLineSymbolID());
                    geoStyle.setLineColor(panelSymbols.getCurrentGeoStyle().getLineColor());
                    geoStyle.setLineWidth(panelSymbols.getCurrentGeoStyle().getLineWidth());
                }
                if (tempGeometry instanceof GeoRegion && styleType == GEOREGIONTYPE) {
                    geoStyle.setFillSymbolID(panelSymbols.getCurrentGeoStyle().getFillSymbolID());
                    geoStyle.setFillForeColor(panelSymbols.getCurrentGeoStyle().getFillForeColor());
                    geoStyle.setFillGradientMode(panelSymbols.getCurrentGeoStyle().getFillGradientMode());
                    geoStyle.setFillGradientOffsetRatioX(panelSymbols.getCurrentGeoStyle().getFillGradientOffsetRatioX());
                    geoStyle.setFillGradientAngle(panelSymbols.getCurrentGeoStyle().getFillGradientAngle());
                    geoStyle.setFillGradientOffsetRatioY(panelSymbols.getCurrentGeoStyle().getFillGradientOffsetRatioY());
                    geoStyle.setFillOpaqueRate(panelSymbols.getCurrentGeoStyle().getFillOpaqueRate());
                    geoStyle.setFillBackOpaque(panelSymbols.getCurrentGeoStyle().getFillBackOpaque());
                }
                tempGeometry.setStyle(geoStyle);
                recordset.setGeometry(tempGeometry);
                tempGeometry.dispose();
                recordset.update();
                recordset.moveNext();
            }
        }
        recordset.close();
        recordset.dispose();
        editHistory.batchEnd();
        MapUtilities.getActiveMap().refresh();
    }

    private void resetGeoStyle(Recordset tempRecordset, GeoStyle newGeoStyle) {
        tempRecordset.moveFirst();
        while (!tempRecordset.isEOF()) {
            editHistory.add(EditType.MODIFY, tempRecordset, true);
            if (!tempRecordset.isReadOnly()) {
                tempRecordset.edit();
                Geometry tempGeometry = tempRecordset.getGeometry();
                tempGeometry.setStyle(newGeoStyle);
                tempRecordset.setGeometry(tempGeometry);
                tempGeometry.dispose();
                tempRecordset.update();
                tempRecordset.moveNext();
            }
        }
        editHistory.batchEnd();
        tempRecordset.close();
        tempRecordset.dispose();
        MapUtilities.getActiveMap().refresh();
    }

    private void setLayout(JPanelSymbols panelSymbols) {
        panelMore = new JPanel();
        buttonMoreImage = new JLabel(ControlsResources.getIcon("/controlsresources/Image_SymbolDictionary.png"));
        labelTitle = new JLabel(ControlsProperties.getString("String_SymbolLibraryManager"));
        panelMore.setLayout(new FlowLayout(FlowLayout.LEFT));
        panelMore.add(buttonMoreImage);
        panelMore.add(labelTitle);
        jScrollPane = new JScrollPane();
        jScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        this.setLayout(new GridBagLayout());
        this.add(jScrollPane, new GridBagConstraintsHelper(0, 0, 1, 2).setWeight(1, 1).setFill(GridBagConstraints.BOTH).setAnchor(GridBagConstraints.CENTER).setWeight(1, 2));
        this.add(panelMore, new GridBagConstraintsHelper(0, 2, 1, 1).setWeight(1, 1).setFill(GridBagConstraints.BOTH).setAnchor(GridBagConstraints.CENTER).setWeight(0, 0));
        jScrollPane.setViewportView(panelSymbols);
        GeoStyle geoStyle = getGeoStyle();
        panelSymbols.setGeoStyle(geoStyle);
        panelMore.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                resetRecordsetGeoStyle();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                panelMore.setBackground(CADStyleTitlePanel.this.COLOR_SYSTEM_SELECTED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panelMore.setBackground(CADStyleTitlePanel.this.COLOR_SYSTEM_DEFAULT);
            }
        });
    }

}