package org.ovirt.engine.ui.common.widget.uicommon.popup;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.AdvancedParametersExpander;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTab;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTabPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelRadioButtonEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.form.key_value.KeyValueWidget;
import org.ovirt.engine.ui.common.widget.parser.MemorySizeParser;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.MemorySizeRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.common.widget.uicommon.storage.DisksAllocationView;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.TimeZoneModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.external.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.ValueLabel;

public abstract class AbstractVmPopupWidget extends AbstractModelBoundPopupWidget<UnitVmModel> {

    interface Driver extends SimpleBeanEditorDriver<UnitVmModel, AbstractVmPopupWidget> {
    }

    interface ViewUiBinder extends UiBinder<DialogTabPanel, AbstractVmPopupWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    protected interface Style extends CssResource {
        String longCheckboxContent();

        String provisioningEditorContent();

        String provisioningRadioContent();

        String cdAttachedLabelWidth();

        String assignedVmsLabel();

        String labelDisabled();

        String generalTabExtendedRightWidgetWidth();
    }

    @UiField
    protected Style style;

    // ==General Tab==
    @UiField
    protected DialogTab generalTab;

    @UiField(provided = true)
    @Path(value = "dataCenter.selectedItem")
    @WithElementId("dataCenter")
    public ListModelListBoxEditor<Object> dataCenterEditor;

    @UiField(provided = true)
    @Path(value = "cluster.selectedItem")
    @WithElementId("cluster")
    public ListModelListBoxEditor<Object> clusterEditor;

    @UiField(provided = true)
    @Path(value = "quota.selectedItem")
    @WithElementId("quota")
    public ListModelListBoxEditor<Object> quotaEditor;

    @UiField
    @Ignore
    public Label nameLabel;

    @UiField
    @Path(value = "name.entity")
    @WithElementId("name")
    public EntityModelTextBoxOnlyEditor nameEditor;

    @UiField(provided = true)
    @Ignore
    public InfoIcon poolNameIcon;

    @UiField
    @Path(value = "description.entity")
    @WithElementId("description")
    public EntityModelTextBoxEditor descriptionEditor;

    @UiField(provided = true)
    @Path(value = "template.selectedItem")
    @WithElementId("template")
    public ListModelListBoxEditor<Object> templateEditor;

    @UiField(provided = true)
    @Path(value = "memSize.entity")
    @WithElementId("memSize")
    public EntityModelTextBoxEditor memSizeEditor;

    @UiField
    @Ignore
    HTML cpuPinningLabel;

    @UiField
    @Path(value = "totalCPUCores.entity")
    @WithElementId("totalCPUCores")
    public EntityModelTextBoxEditor totalvCPUsEditor;

    @UiField
    @Path(value = "numOfSockets.selectedItem")
    @WithElementId("numOfSockets")
    public ListModelListBoxEditor<Object> numOfSocketsEditor;

    @UiField
    @Path(value = "coresPerSocket.selectedItem")
    @WithElementId("coresPerSocket")
    public ListModelListBoxEditor<Object> corePerSocketEditor;

    @UiField(provided = true)
    @Path(value = "oSType.selectedItem")
    @WithElementId("osType")
    public ListModelListBoxEditor<Object> oSTypeEditor;

    @UiField(provided = true)
    @Path(value = "isDeleteProtected.entity")
    @WithElementId("isDeleteProtected")
    public EntityModelCheckBoxEditor isDeleteProtectedEditor;

    @UiField
    @Ignore
    Label generalWarningMessage;

    // == Pools ==
    @UiField
    protected DialogTab poolTab;

    @UiField(provided = true)
    @Path(value = "poolType.selectedItem")
    @WithElementId("poolType")
    public ListModelListBoxEditor<Object> poolTypeEditor;

    @UiField(provided = true)
    @Ignore
    public InfoIcon newPoolPrestartedVmsIcon;

    @UiField(provided = true)
    @Ignore
    public InfoIcon editPoolPrestartedVmsIcon;

    @UiField(provided = true)
    @Ignore
    public InfoIcon newPoolMaxAssignedVmsPerUserIcon;

    @UiField(provided = true)
    @Ignore
    public InfoIcon editPoolMaxAssignedVmsPerUserIcon;

    @UiField
    @Path(value = "prestartedVms.entity")
    @WithElementId("prestartedVms")
    public EntityModelTextBoxOnlyEditor prestartedVmsEditor;

    @UiField
    @Path("maxAssignedVmsPerUser.entity")
    @WithElementId("maxAssignedVmsPerUser")
    public EntityModelTextBoxOnlyEditor maxAssignedVmsPerUserEditor;

    @UiField
    @Ignore
    public FlowPanel newPoolEditVmsPanel;

    @UiField
    @Ignore
    public FlowPanel newPoolEditMaxAssignedVmsPerUserPanel;

    @UiField
    @Ignore
    public Label prestartedLabel;

    @UiField(provided = true)
    @Path("numOfDesktops.entity")
    @WithElementId("numOfVms")
    public EntityModelTextBoxEditor numOfVmsEditor;

    @UiField
    @Ignore
    public FlowPanel editPoolEditVmsPanel;

    @UiField
    public FlowPanel editIncreaseVmsPanel;

    @UiField
    @Ignore
    public FlowPanel editPoolIncraseNumOfVmsPanel;

    @UiField
    public FlowPanel editPrestartedVmsPanel;
    @UiField
    @Ignore
    public FlowPanel editPoolEditMaxAssignedVmsPerUserPanel;

    @UiField
    @Ignore
    public Label editPrestartedVmsLabel;

    @UiField
    @Path("prestartedVms.entity")
    @WithElementId("editPrestartedVms")
    public EntityModelTextBoxOnlyEditor editPrestartedVmsEditor;

    @UiField(provided = true)
    @Path("numOfDesktops.entity")
    @WithElementId("incraseNumOfVms")
    public EntityModelTextBoxOnlyEditor incraseNumOfVmsEditor;

    @UiField
    @Path("maxAssignedVmsPerUser.entity")
    @WithElementId("editMaxAssignedVmsPerUser")
    public EntityModelTextBoxOnlyEditor editMaxAssignedVmsPerUserEditor;

    @UiField(provided = true)
    @Path("assignedVms.entity")
    public ValueLabel<Object> outOfxInPool;

    // ==Initial run Tab==
    @UiField
    protected DialogTab initialRunTab;

    @UiField(provided = true)
    @Path(value = "domain.selectedItem")
    @WithElementId("domain")
    public ListModelListBoxEditor<Object> domainEditor;

    @UiField(provided = true)
    @Path(value = "timeZone.selectedItem")
    @WithElementId("timeZone")
    public ListModelListBoxEditor<Object> timeZoneEditor;

    // ==Console Tab==
    @UiField
    protected DialogTab consoleTab;

    @UiField(provided = true)
    @Path(value = "displayProtocol.selectedItem")
    @WithElementId("displayProtocol")
    public ListModelListBoxEditor<Object> displayProtocolEditor;

    @UiField(provided = true)
    @Path(value = "vncKeyboardLayout.selectedItem")
    @WithElementId("vncKeyboardLayout")
    public ListModelListBoxEditor<Object> vncKeyboardLayoutEditor;

    @UiField(provided = true)
    @Path(value = "usbPolicy.selectedItem")
    @WithElementId("usbPolicy")
    public ListModelListBoxEditor<Object> usbSupportEditor;

    @UiField(provided = true)
    @Path(value = "numOfMonitors.selectedItem")
    @WithElementId("numOfMonitors")
    public ListModelListBoxEditor<Object> numOfMonitorsEditor;

    @UiField(provided = true)
    @Path(value = "isStateless.entity")
    @WithElementId("isStateless")
    public EntityModelCheckBoxEditor isStatelessEditor;

    @UiField(provided = true)
    @Path(value = "isRunAndPause.entity")
    @WithElementId("isRunAndPause")
    public EntityModelCheckBoxEditor isRunAndPauseEditor;

    @UiField(provided = true)
    @Path(value = "isSmartcardEnabled.entity")
    @WithElementId("isSmartcardEnabled")
    public EntityModelCheckBoxEditor isSmartcardEnabledEditor;

    @UiField(provided = true)
    @Path(value = "allowConsoleReconnect.entity")
    @WithElementId("allowConsoleReconnect")
    public EntityModelCheckBoxEditor allowConsoleReconnectEditor;

    // ==Host Tab==
    @UiField
    protected DialogTab hostTab;

    @UiField(provided = true)
    @Path(value = "hostCpu.entity")
    @WithElementId("hostCpu")
    public EntityModelCheckBoxEditor hostCpuEditor;

    @UiField(provided = true)
    @Path(value = "migrationMode.selectedItem")
    @WithElementId("migrationMode")
    public ListModelListBoxEditor<Object> migrationModeEditor;

    @UiField(provided = true)
    @Ignore
    @WithElementId("specificHost")
    public RadioButton specificHost;

    @UiField
    @Ignore
    public Label specificHostLabel;

    @UiField(provided = true)
    @Path(value = "defaultHost.selectedItem")
    @WithElementId("defaultHost")
    public ListModelListBoxEditor<Object> defaultHostEditor;

    @UiField(provided = true)
    @Path(value = "isAutoAssign.entity")
    @WithElementId("isAutoAssign")
    public EntityModelRadioButtonEditor isAutoAssignEditor;

    @UiField
    @Path(value = "cpuPinning.entity")
    @WithElementId("cpuPinning")
    public EntityModelTextBoxEditor cpuPinning;

    // ==High Availability Tab==
    @UiField
    protected DialogTab highAvailabilityTab;

    @UiField(provided = true)
    @Path(value = "isHighlyAvailable.entity")
    @WithElementId("isHighlyAvailable")
    public EntityModelCheckBoxEditor isHighlyAvailableEditor;

    // TODO: Priority is a ListModel which is rendered as RadioBox
    @UiField(provided = true)
    @Ignore
    @WithElementId("priority")
    public EntityModelCellTable<ListModel> priorityEditor;

    @UiField(provided = true)
    @Path(value = "watchdogModel.selectedItem")
    @WithElementId("watchdogModel")
    public ListModelListBoxEditor<Object> watchdogModelEditor;

    @UiField(provided = true)
    @Path(value = "watchdogAction.selectedItem")
    @WithElementId("watchdogAction")
    public ListModelListBoxEditor<Object> watchdogActionEditor;


    // ==Resource Allocation Tab==
    @UiField
    protected DialogTab resourceAllocationTab;

    @UiField
    protected FlowPanel storageAllocationPanel;

    @UiField
    protected HorizontalPanel provisionSelectionPanel;

    @UiField
    protected FlowPanel disksAllocationPanel;

    @UiField
    @Ignore
    @WithElementId("provisioning")
    public ListModelListBoxEditor<Object> provisioningEditor;

    @UiField(provided = true)
    @Path(value = "minAllocatedMemory.entity")
    @WithElementId("minAllocatedMemory")
    public EntityModelTextBoxEditor minAllocatedMemoryEditor;

    @UiField(provided = true)
    @Path(value = "provisioningThin_IsSelected.entity")
    @WithElementId("provisioningThin")
    public EntityModelRadioButtonEditor provisioningThinEditor;

    @UiField(provided = true)
    @Path(value = "provisioningClone_IsSelected.entity")
    @WithElementId("provisioningClone")
    public EntityModelRadioButtonEditor provisioningCloneEditor;

    @UiField
    @Ignore
    Label disksAllocationLabel;

    @UiField(provided = true)
    @Ignore
    @WithElementId("disksAllocation")
    public DisksAllocationView disksAllocationView;

    // ==Boot Options Tab==
    @UiField
    protected DialogTab bootOptionsTab;

    @UiField(provided = true)
    @Path(value = "firstBootDevice.selectedItem")
    @WithElementId("firstBootDevice")
    public ListModelListBoxEditor<Object> firstBootDeviceEditor;

    @UiField(provided = true)
    @Path(value = "secondBootDevice.selectedItem")
    @WithElementId("secondBootDevice")
    public ListModelListBoxEditor<Object> secondBootDeviceEditor;

    @UiField(provided = true)
    @Path(value = "cdImage.selectedItem")
    @WithElementId("cdImage")
    public ListModelListBoxEditor<Object> cdImageEditor;

    @UiField(provided = true)
    @Path(value = "cdAttached.entity")
    @WithElementId("cdAttached")
    public EntityModelCheckBoxEditor cdAttachedEditor;

    @UiField
    protected FlowPanel linuxBootOptionsPanel;

    @UiField
    @Path(value = "kernel_path.entity")
    @WithElementId("kernelPath")
    public EntityModelTextBoxEditor kernel_pathEditor;

    @UiField
    @Path(value = "initrd_path.entity")
    @WithElementId("initrdPath")
    public EntityModelTextBoxEditor initrd_pathEditor;

    @UiField
    @Path(value = "kernel_parameters.entity")
    @WithElementId("kernelParameters")
    public EntityModelTextBoxEditor kernel_parametersEditor;

    @UiField
    @Ignore
    Label nativeUsbWarningMessage;

    // ==Custom Properties Tab==
    @UiField
    protected DialogTab customPropertiesTab;

    @UiField
    @Ignore
    protected KeyValueWidget customPropertiesSheetEditor;

    private final CommonApplicationMessages messages;

    @UiField
    @Ignore
    protected AdvancedParametersExpander expander;

    @UiField
    @Ignore
    Panel expanderContent;

    @UiField
    @Ignore
    AdvancedParametersExpander generalAdvancedParameterExpander;

    @UiField
    @Ignore
    Panel generalAdvancedParameterExpanderContent;

    private final Driver driver = GWT.create(Driver.class);

    private final CommonApplicationTemplates applicationTemplates;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public AbstractVmPopupWidget(CommonApplicationConstants constants,
            CommonApplicationResources resources,
            final CommonApplicationMessages messages,
            CommonApplicationTemplates applicationTemplates) {

        this.messages = messages;
        this.applicationTemplates = applicationTemplates;

        initListBoxEditors();
        // Contains a special parser/renderer
        memSizeEditor = new EntityModelTextBoxEditor(
                new MemorySizeRenderer(constants), new MemorySizeParser());
        minAllocatedMemoryEditor = new EntityModelTextBoxEditor(
                new MemorySizeRenderer(constants), new MemorySizeParser());

        // TODO: How to align right without creating the widget manually?
        hostCpuEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        isHighlyAvailableEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        watchdogModelEditor = new ListModelListBoxEditor<Object>();
        watchdogActionEditor = new ListModelListBoxEditor<Object>();
        isStatelessEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        isRunAndPauseEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        isDeleteProtectedEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        isSmartcardEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        cdAttachedEditor = new EntityModelCheckBoxEditor(Align.LEFT);
        allowConsoleReconnectEditor = new EntityModelCheckBoxEditor(Align.RIGHT);

        priorityEditor = new EntityModelCellTable<ListModel>(
                (Resources) GWT.create(ButtonCellTableResources.class));
        disksAllocationView = new DisksAllocationView(constants);

        initPoolSpecificWidgets(resources, messages);

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        expander.initWithContent(expanderContent.getElement());
        generalAdvancedParameterExpander.initWithContent(generalAdvancedParameterExpanderContent.getElement());
        editPrestartedVmsEditor.setKeepTitleOnSetEnabled(true);

        applyStyles();

        poolTab.setVisible(false);

        localize(constants);

        generateIds();

        hidePoolSpecificFields();

        priorityEditor.addEntityModelColumn(new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel model) {
                return model.getTitle();
            }
        }, ""); //$NON-NLS-1$

        driver.initialize(this);
    }

    protected void initPoolSpecificWidgets(CommonApplicationResources resources,
            final CommonApplicationMessages messages) {
        createNumOfDesktopEditors();

        incraseNumOfVmsEditor.setKeepTitleOnSetEnabled(true);
        numOfVmsEditor.setKeepTitleOnSetEnabled(true);

        newPoolPrestartedVmsIcon =
                new InfoIcon(applicationTemplates.italicText(messages.prestartedHelp()), resources);

        editPoolPrestartedVmsIcon =
                new InfoIcon(applicationTemplates.italicText(messages.prestartedHelp()), resources);

        poolNameIcon =
                new InfoIcon(applicationTemplates.italicText(messages.poolNameHelp()), resources);

        poolNameIcon.setVisible(false);

        newPoolMaxAssignedVmsPerUserIcon =
                new InfoIcon(applicationTemplates.italicText(messages.maxAssignedVmsPerUserHelp()), resources);

        editPoolMaxAssignedVmsPerUserIcon =
                new InfoIcon(applicationTemplates.italicText(messages.maxAssignedVmsPerUserHelp()), resources);

        outOfxInPool = new ValueLabel<Object>(new AbstractRenderer<Object>() {

            @Override
            public String render(Object object) {
                return messages.outOfXVMsInPool(object.toString());
            }

        });
    }

    /**
     * There are two editors which edits the same entity - in the correct subclass make sure that the correct one's
     * value is used to edit the model
     * <p>
     * The default implementation just creates the simple editors
     */
    protected void createNumOfDesktopEditors() {
        incraseNumOfVmsEditor = new EntityModelTextBoxOnlyEditor();
        numOfVmsEditor = new EntityModelTextBoxEditor();
    }

    private void hidePoolSpecificFields() {
        numOfVmsEditor.setVisible(false);
        newPoolEditVmsPanel.setVisible(false);
        newPoolEditMaxAssignedVmsPerUserPanel.setVisible(false);
        editPoolEditVmsPanel.setVisible(false);
        editPoolIncraseNumOfVmsPanel.setVisible(false);
        editPoolEditMaxAssignedVmsPerUserPanel.setVisible(false);
    }

    protected abstract void generateIds();

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void initListBoxEditors() {
        // General tab
        dataCenterEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((StoragePool) object).getname();
            }
        });

        clusterEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((VDSGroup) object).getname();
            }
        });

        quotaEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((Quota) object).getQuotaName();
            }
        });

        templateEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((VmTemplate) object).getName();
            }
        });

        oSTypeEditor = new ListModelListBoxEditor<Object>(new EnumRenderer());

        // Pools
        poolTypeEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((EntityModel) object).getTitle();
            }
        });

        // Windows Sysprep
        domainEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return object.toString();
            }
        });

        timeZoneEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                TimeZoneModel timeZone = (TimeZoneModel) object;
                if (timeZone.isDefault()) {
                    return messages.defaultTimeZoneCaption(timeZone.getDisplayValue());//$NON-NLS-1$
                } else {
                    return timeZone.getDisplayValue();
                }
            }
        });

        // Console tab
        displayProtocolEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((EntityModel) object).getTitle();
            }
        });

        usbSupportEditor = new ListModelListBoxEditor<Object>(new EnumRenderer());
        numOfMonitorsEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return object.toString();
            }
        });

        vncKeyboardLayoutEditor = new ListModelListBoxEditor<Object>(new AbstractRenderer<Object>() {

            final String globalLayout = (String)AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.VncKeyboardLayout);

            @Override
            public String render(Object object) {
                if (object == null) {
                    return messages.globalVncKeyboardLayoutCaption(globalLayout);
                } else {
                    return object.toString();
                }
            }

        });

        // Host Tab
        specificHost = new RadioButton("runVmOnHostGroup"); //$NON-NLS-1$
        isAutoAssignEditor = new EntityModelRadioButtonEditor("runVmOnHostGroup"); //$NON-NLS-1$
        defaultHostEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((VDS) object).getName();
            }
        });

        migrationModeEditor = new ListModelListBoxEditor<Object>(new EnumRenderer());

        // Resource Allocation
        provisioningThinEditor = new EntityModelRadioButtonEditor("provisioningGroup"); //$NON-NLS-1$
        provisioningCloneEditor = new EntityModelRadioButtonEditor("provisioningGroup"); //$NON-NLS-1$

        // Boot Options Tab
        firstBootDeviceEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((EntityModel) object).getTitle();
            }
        });

        secondBootDeviceEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((EntityModel) object).getTitle();
            }
        });

        cdImageEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return (String) object;
            }
        });
    }

    protected void localize(CommonApplicationConstants constants) {
        // Tabs
        highAvailabilityTab.setLabel(constants.highAvailVmPopup());
        resourceAllocationTab.setLabel(constants.resourceAllocVmPopup());
        bootOptionsTab.setLabel(constants.bootOptionsVmPopup());
        customPropertiesTab.setLabel(constants.customPropsVmPopup());

        // General Tab
        generalTab.setLabel(constants.GeneralVmPopup());
        dataCenterEditor.setLabel(constants.dcVmPopup());
        clusterEditor.setLabel(constants.hostClusterVmPopup());
        quotaEditor.setLabel(constants.quotaVmPopup());
        nameLabel.setText(constants.nameVmPopup());
        descriptionEditor.setLabel(constants.descriptionVmPopup());
        templateEditor.setLabel(constants.basedOnTemplateVmPopup());
        memSizeEditor.setLabel(constants.memSizeVmPopup());
        totalvCPUsEditor.setLabel(constants.numOfVCPUs());
        corePerSocketEditor.setLabel(constants.coresPerSocket());
        numOfSocketsEditor.setLabel(constants.numOfSockets());

        oSTypeEditor.setLabel(constants.osVmPopup());
        isStatelessEditor.setLabel(constants.statelessVmPopup());
        isRunAndPauseEditor.setLabel(constants.runAndPauseVmPopup());
        isDeleteProtectedEditor.setLabel(constants.deleteProtectionPopup());
        isSmartcardEnabledEditor.setLabel(constants.smartcardVmPopup());

        // Pools Tab
        poolTab.setLabel(constants.poolVmPopup());
        poolTypeEditor.setLabel(constants.poolTypeVmPopup());
        editPrestartedVmsLabel.setText(constants.prestartedVms());

        prestartedLabel.setText(constants.prestartedPoolPopup());
        numOfVmsEditor.setLabel(constants.numOfVmsPoolPopup());
        maxAssignedVmsPerUserEditor.setLabel(constants.maxAssignedVmsPerUser());
        editMaxAssignedVmsPerUserEditor.setLabel(constants.maxAssignedVmsPerUser());

        // initial run Tab
        initialRunTab.setLabel(constants.initialRunVmPopup());
        domainEditor.setLabel(constants.domainVmPopup());
        timeZoneEditor.setLabel(constants.tzVmPopup());

        // Console Tab
        consoleTab.setLabel(constants.consoleVmPopup());
        displayProtocolEditor.setLabel(constants.protocolVmPopup());
        vncKeyboardLayoutEditor.setLabel(constants.vncKeyboardLayoutVmPopup());
        usbSupportEditor.setLabel(constants.usbPolicyVmPopup());
        numOfMonitorsEditor.setLabel(constants.monitorsVmPopup());
        allowConsoleReconnectEditor.setLabel(constants.allowConsoleReconnect());

        // Host Tab
        hostTab.setLabel(constants.hostVmPopup());
        isAutoAssignEditor.setLabel(constants.anyHostInClusterVmPopup());
        // specificHostEditor.setLabel("Specific");
        hostCpuEditor.setLabel(constants.useHostCpu());
        cpuPinning.setLabel(constants.cpuPinningLabel());
        cpuPinningLabel.setHTML(constants.cpuPinningLabelExplanation());

        // High Availability Tab
        isHighlyAvailableEditor.setLabel(constants.highlyAvailableVmPopup());

        // watchdog
        watchdogActionEditor.setLabel(constants.watchdogAction());
        watchdogModelEditor.setLabel(constants.watchdogModel());

        // Resource Allocation Tab
        provisioningEditor.setLabel(constants.templateProvisVmPopup());
        provisioningThinEditor.setLabel(constants.thinVmPopup());
        provisioningCloneEditor.setLabel(constants.cloneVmPopup());
        minAllocatedMemoryEditor.setLabel(constants.physMemGuarVmPopup());

        // Boot Options
        firstBootDeviceEditor.setLabel(constants.firstDeviceVmPopup());
        secondBootDeviceEditor.setLabel(constants.secondDeviceVmPopup());
        kernel_pathEditor.setLabel(constants.kernelPathVmPopup());
        initrd_pathEditor.setLabel(constants.initrdPathVmPopup());
        kernel_parametersEditor.setLabel(constants.kernelParamsVmPopup());
    }

    protected void applyStyles() {
        hostCpuEditor.addContentWidgetStyleName(style.longCheckboxContent());
        allowConsoleReconnectEditor.addContentWidgetStyleName(style.longCheckboxContent());
        provisioningEditor.addContentWidgetStyleName(style.provisioningEditorContent());
        provisioningThinEditor.addContentWidgetStyleName(style.provisioningRadioContent());
        provisioningCloneEditor.addContentWidgetStyleName(style.provisioningRadioContent());
        cdAttachedEditor.addContentWidgetStyleName(style.cdAttachedLabelWidth());
    }

    @Override
    public void edit(UnitVmModel object) {
        priorityEditor.setRowData(new ArrayList<EntityModel>());
        priorityEditor.edit(object.getPriority());
        driver.edit(object);
        initTabAvailabilityListeners(object);
        initListeners(object);
        initCustomPropertySheet(object);

        // numOfVmsLabel.setVisible(false);
    }

    private void initCustomPropertySheet(final UnitVmModel object) {
        object.getCustomPropertySheet().getKeyValueLines().getItemsChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                customPropertiesSheetEditor.edit(object.getCustomPropertySheet());
            }
        });
    }

    protected void setupHostTabAvailability(UnitVmModel model) {
        hostTab.setVisible(model.getIsHostAvailable());
    }

    protected void setupCustomPropertiesAvailability(UnitVmModel model) {
        customPropertiesTab.setVisible(model.getIsCustomPropertiesTabAvailable());
    }

    private void initListeners(final UnitVmModel object) {
        // TODO should be handled by the core framework
        object.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).PropertyName;
                if ("IsHostAvailable".equals(propName)) { //$NON-NLS-1$
                    setupHostTabAvailability(object);
                } else if ("IsHostTabValid".equals(propName)) { //$NON-NLS-1$
                    if (object.getIsHostTabValid()) {
                        hostTab.markAsValid();
                    } else {
                        hostTab.markAsInvalid(null);
                    }
                } else if ("IsCustomPropertiesTabAvailable".equals(propName)) { //$NON-NLS-1$
                    setupCustomPropertiesAvailability(object);
                } else if ("IsDisksAvailable".equals(propName)) { //$NON-NLS-1$
                    addDiskAllocation(object);
                }
            }
        });

        object.getIsAutoAssign().getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                boolean isAutoAssign = (Boolean) object.getIsAutoAssign().getEntity();
                defaultHostEditor.setEnabled(!isAutoAssign);

                // only this is not bind to the model, so needs to listen to the change explicitly
                specificHost.setValue(!isAutoAssign);
            }
        });

        // only for non local storage available
        setupHostTabAvailability(object);

        object.getProvisioning().getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                boolean isProvisioningChangable = object.getProvisioning().getIsChangable();
                provisioningThinEditor.setEnabled(isProvisioningChangable);
                provisioningCloneEditor.setEnabled(isProvisioningChangable);

                boolean isProvisioningAvailable = object.getProvisioning().getIsAvailable();
                provisionSelectionPanel.setVisible(isProvisioningAvailable);

                boolean isDisksAvailable = object.getIsDisksAvailable();
                disksAllocationPanel.setVisible(isDisksAvailable);

                storageAllocationPanel.setVisible(isProvisioningAvailable || isDisksAvailable);
            }
        });

        object.getUsbPolicy().getPropertyChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                PropertyChangedEventArgs e = (PropertyChangedEventArgs) args;

                if (e.PropertyName == "SelectedItem") { //$NON-NLS-1$
                    updateUsbNativeMessageVisibility(object);
                }
            }
        });

        updateUsbNativeMessageVisibility(object);
    }

    /**
     * This raises a warning for USB devices that won't persist a VM migration when using Native USB with SPICE in
     * certain, configurable cluster version.
     */
    protected void updateUsbNativeMessageVisibility(final UnitVmModel object) {
        VDSGroup vdsGroup = (VDSGroup) object.getCluster().getSelectedItem();
        nativeUsbWarningMessage.setVisible(object.getUsbPolicy().getSelectedItem() == UsbPolicy.ENABLED_NATIVE
                && vdsGroup != null
                && vdsGroup.getcompatibility_version() != null
                && !(Boolean) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.MigrationSupportForNativeUsb,
                        vdsGroup.getcompatibility_version().getValue()));
    }

    private void addDiskAllocation(UnitVmModel model) {
        if (!model.getIsDisksAvailable()) {
            return;
        }
        disksAllocationView.edit(model.getDisksAllocationModel());
        model.getDisksAllocationModel().setDisks(model.getDisks());
    }

    private void initTabAvailabilityListeners(final UnitVmModel vm) {
        // TODO should be handled by the core framework
        vm.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).PropertyName;
                if ("IsWindowsOS".equals(propName)) { //$NON-NLS-1$
                    domainEditor.setEnabled(vm.getIsWindowsOS());
                } else if ("IsGeneralTabValid".equals(propName)) { //$NON-NLS-1$
                    if (vm.getIsGeneralTabValid()) {
                        generalTab.markAsValid();
                    } else {
                        generalTab.markAsInvalid(null);
                    }
                } else if ("IsDisplayTabValid".equals(propName)) { //$NON-NLS-1$
                    if (vm.getIsDisplayTabValid()) {
                        consoleTab.markAsValid();
                    } else {
                        consoleTab.markAsInvalid(null);
                    }
                } else if ("IsAllocationTabValid".equals(propName)) { //$NON-NLS-1$
                    if (vm.getIsAllocationTabValid()) {
                        resourceAllocationTab.markAsValid();
                    } else {
                        resourceAllocationTab.markAsInvalid(null);
                    }
                } else if ("IsHighlyAvailable".equals(propName)) { //$NON-NLS-1$
                    highAvailabilityTab.setVisible((Boolean) vm.getIsHighlyAvailable().getEntity());
                } else if ("IsBootSequenceTabValid".equals(propName)) { //$NON-NLS-1$
                    if ((Boolean) vm.getIsHighlyAvailable().getEntity()) {
                        bootOptionsTab.markAsValid();
                    } else {
                        bootOptionsTab.markAsInvalid(null);
                    }
                } else if ("IsCustomPropertiesTabValid".equals(propName)) { //$NON-NLS-1$
                    if (vm.getIsCustomPropertiesTabValid()) {
                        customPropertiesTab.markAsValid();
                    } else {
                        customPropertiesTab.markAsInvalid(null);
                    }
                }
                else if ("IsDisksAvailable".equals(propName)) { //$NON-NLS-1$
                    boolean isDisksAvailable = vm.getIsDisksAvailable();
                    disksAllocationPanel.setVisible(isDisksAvailable);

                    boolean isProvisioningAvailable = vm.getProvisioning().getIsAvailable();
                    storageAllocationPanel.setVisible(isProvisioningAvailable || isDisksAvailable);

                    if (isDisksAvailable) {
                        // Update warning message by disks status
                        updateDisksWarningByImageStatus(vm.getDisks(), ImageStatus.ILLEGAL);
                        updateDisksWarningByImageStatus(vm.getDisks(), ImageStatus.LOCKED);
                    }
                    else {
                        // Clear warning message
                        generalWarningMessage.setText(""); //$NON-NLS-1$
                    }
                }
            }
        });

        // High Availability only avail in server mode
        highAvailabilityTab.setVisible(vm.getVmType().equals(VmType.Server));

        // TODO: Move to a more appropriate method
        vm.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).PropertyName;
                if ("IsLinuxOS".equals(propName)) { //$NON-NLS-1$
                    linuxBootOptionsPanel.setVisible(vm.getIsLinuxOS());
                }
            }
        });

        // only avail for desktop mode
        isStatelessEditor.setVisible(vm.getVmType().equals(VmType.Desktop));
        numOfMonitorsEditor.setVisible(vm.getVmType().equals(VmType.Desktop));

        defaultHostEditor.setEnabled(false);
        specificHost.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                defaultHostEditor.setEnabled(specificHost.getValue());
                ValueChangeEvent.fire(isAutoAssignEditor.asRadioButton(), false);
            }
        });

        // TODO: This is a hack and should be handled cleanly via model property availability
        isAutoAssignEditor.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                defaultHostEditor.setEnabled(false);
            }
        }, ClickEvent.getType());

        vm.getIsAutoAssign().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (!isAutoAssignEditor.asRadioButton().getValue())
                    specificHost.setValue(true, true);
            }
        });

        cpuPinningLabel.setVisible(vm.getCpuPinning().getIsChangable());
        vm.getCpuPinning().getPropertyChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                cpuPinningLabel.setVisible(vm.getCpuPinning().getIsChangable());
            }
        });

    }

    private void updateDisksWarningByImageStatus(List<DiskModel> disks, ImageStatus imageStatus) {
        ArrayList<String> disksAliases =
                getDisksAliasesByImageStatus(disks, imageStatus);

        if (!disksAliases.isEmpty()) {
            generalWarningMessage.setText(messages.disksStatusWarning(
                    EnumTranslator.createAndTranslate(imageStatus),
                    (StringUtils.join(disksAliases, ", ")))); //$NON-NLS-1$
        }
    }

    private ArrayList<String> getDisksAliasesByImageStatus(List<DiskModel> disks, ImageStatus status) {
        ArrayList<String> disksAliases = new ArrayList<String>();

        if (disks == null) {
            return disksAliases;
        }

        for (DiskModel diskModel : disks) {
            Disk disk = diskModel.getDisk();
            if (disk.getDiskStorageType() == DiskStorageType.IMAGE &&
                    ((DiskImage) disk).getImageStatus() == status) {

                disksAliases.add(disk.getDiskAlias());
            }
        }

        return disksAliases;
    }

    @Override
    public UnitVmModel flush() {
        priorityEditor.flush();
        return driver.flush();
    }

    @Override
    public void focusInput() {
        nameEditor.setFocus(true);
    }

    public interface ButtonCellTableResources extends CellTable.Resources {
        interface TableStyle extends CellTable.Style {
        }

        @Override
        @Source({ CellTable.Style.DEFAULT_CSS, "org/ovirt/engine/ui/common/css/ButtonCellTable.css" })
        TableStyle cellTableStyle();
    }

    @Override
    public int setTabIndexes(int nextTabIndex) {
        // ==General Tab==
        nextTabIndex = generalTab.setTabIndexes(nextTabIndex);
        dataCenterEditor.setTabIndex(nextTabIndex++);
        clusterEditor.setTabIndex(nextTabIndex++);
        quotaEditor.setTabIndex(nextTabIndex++);
        nameEditor.setTabIndex(nextTabIndex++);
        descriptionEditor.setTabIndex(nextTabIndex++);

        numOfVmsEditor.setTabIndex(nextTabIndex++);
        prestartedVmsEditor.setTabIndex(nextTabIndex++);
        editPrestartedVmsEditor.setTabIndex(nextTabIndex++);
        incraseNumOfVmsEditor.setTabIndex(nextTabIndex++);
        maxAssignedVmsPerUserEditor.setTabIndex(nextTabIndex++);
        editMaxAssignedVmsPerUserEditor.setTabIndex(nextTabIndex++);

        templateEditor.setTabIndex(nextTabIndex++);
        memSizeEditor.setTabIndex(nextTabIndex++);
        totalvCPUsEditor.setTabIndex(nextTabIndex++);

        nextTabIndex = generalAdvancedParameterExpander.setTabIndexes(nextTabIndex);
        corePerSocketEditor.setTabIndex(nextTabIndex++);
        numOfSocketsEditor.setTabIndex(nextTabIndex++);

        oSTypeEditor.setTabIndex(nextTabIndex++);
        isStatelessEditor.setTabIndex(nextTabIndex++);
        isRunAndPauseEditor.setTabIndex(nextTabIndex++);
        isDeleteProtectedEditor.setTabIndex(nextTabIndex++);

        // == Pools ==
        nextTabIndex = poolTab.setTabIndexes(nextTabIndex);
        poolTypeEditor.setTabIndex(nextTabIndex++);

        // ==Initial run Tab==
        nextTabIndex = initialRunTab.setTabIndexes(nextTabIndex);
        timeZoneEditor.setTabIndex(nextTabIndex++);
        domainEditor.setTabIndex(nextTabIndex++);

        // ==Console Tab==
        nextTabIndex = consoleTab.setTabIndexes(nextTabIndex);
        displayProtocolEditor.setTabIndex(nextTabIndex++);
        vncKeyboardLayoutEditor.setTabIndex(nextTabIndex++);
        usbSupportEditor.setTabIndex(nextTabIndex++);
        numOfMonitorsEditor.setTabIndex(nextTabIndex++);
        isSmartcardEnabledEditor.setTabIndex(nextTabIndex++);
        nextTabIndex = expander.setTabIndexes(nextTabIndex);
        allowConsoleReconnectEditor.setTabIndex(nextTabIndex++);

        // ==Host Tab==
        nextTabIndex = hostTab.setTabIndexes(nextTabIndex);
        isAutoAssignEditor.setTabIndex(nextTabIndex++);
        specificHost.setTabIndex(nextTabIndex++);
        defaultHostEditor.setTabIndex(nextTabIndex++);
        migrationModeEditor.setTabIndex(nextTabIndex++);
        hostCpuEditor.setTabIndex(nextTabIndex++);
        cpuPinning.setTabIndex(nextTabIndex++);

        // ==High Availability Tab==
        nextTabIndex = highAvailabilityTab.setTabIndexes(nextTabIndex);
        isHighlyAvailableEditor.setTabIndex(nextTabIndex++);
        priorityEditor.setTabIndex(nextTabIndex++);

        watchdogModelEditor.setTabIndex(nextTabIndex++);
        watchdogActionEditor.setTabIndex(nextTabIndex++);

        // ==Resource Allocation Tab==
        nextTabIndex = resourceAllocationTab.setTabIndexes(nextTabIndex);
        minAllocatedMemoryEditor.setTabIndex(nextTabIndex++);
        provisioningEditor.setTabIndex(nextTabIndex++);
        provisioningThinEditor.setTabIndex(nextTabIndex++);
        provisioningCloneEditor.setTabIndex(nextTabIndex++);
        nextTabIndex = disksAllocationView.setTabIndexes(nextTabIndex);

        // ==Boot Options Tab==
        nextTabIndex = bootOptionsTab.setTabIndexes(nextTabIndex);
        firstBootDeviceEditor.setTabIndex(nextTabIndex++);
        secondBootDeviceEditor.setTabIndex(nextTabIndex++);
        cdAttachedEditor.setTabIndex(nextTabIndex++);
        cdImageEditor.setTabIndex(nextTabIndex++);
        kernel_pathEditor.setTabIndex(nextTabIndex++);
        initrd_pathEditor.setTabIndex(nextTabIndex++);
        kernel_parametersEditor.setTabIndex(nextTabIndex++);

        // ==Custom Properties Tab==
        nextTabIndex = customPropertiesTab.setTabIndexes(nextTabIndex);
//        customPropertiesSheetEditor.setTabIndex(nextTabIndex++);

        return nextTabIndex;
    }
}
