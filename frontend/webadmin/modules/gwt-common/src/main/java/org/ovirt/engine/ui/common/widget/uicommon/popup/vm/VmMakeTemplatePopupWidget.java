package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.common.widget.uicommon.storage.DisksAllocationView;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class VmMakeTemplatePopupWidget extends AbstractModelBoundPopupWidget<UnitVmModel> {

    interface Driver extends SimpleBeanEditorDriver<UnitVmModel, VmMakeTemplatePopupWidget> {
    }

    interface ViewUiBinder extends UiBinder<FlowPanel, VmMakeTemplatePopupWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<VmMakeTemplatePopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @Path(value = "name.entity")
    @WithElementId("name")
    EntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "description.entity")
    @WithElementId("description")
    EntityModelTextBoxEditor descriptionEditor;

    @UiField(provided = true)
    @Path(value = "cluster.selectedItem")
    @WithElementId("cluster")
    ListModelListBoxEditor<Object> clusterEditor;

    @UiField(provided = true)
    @Ignore
    @WithElementId("disksAllocation")
    DisksAllocationView disksAllocationView;

    @UiField(provided = true)
    @Path(value = "quota.selectedItem")
    @WithElementId("quota")
    ListModelListBoxEditor<Object> quotaEditor;

    @UiField(provided = true)
    @Path(value = "isTemplatePublic.entity")
    @WithElementId("isTemplatePublic")
    EntityModelCheckBoxEditor isTemplatePublicEditor;

    @UiField
    @Ignore
    FlowPanel messagePanel;

    @UiField
    @Ignore
    Label disksAllocationLabel;

    interface WidgetStyle extends CssResource {
        String editorLabel();
    }

    @UiField
    WidgetStyle style;

    private final Driver driver = GWT.create(Driver.class);

    public VmMakeTemplatePopupWidget(CommonApplicationConstants constants) {
        initListBoxEditors();
        initCheckBoxEditors();
        disksAllocationView = new DisksAllocationView(constants);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
        addStyle();
    }

    void addStyle() {
        isTemplatePublicEditor.setContentWidgetStyleName(style.editorLabel());
    }

    void initListBoxEditors() {
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
    }

    void initCheckBoxEditors() {
        isTemplatePublicEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    void localize(CommonApplicationConstants constants) {
        nameEditor.setLabel(constants.makeTemplatePopupNameLabel());
        descriptionEditor.setLabel(constants.makeTemplatePopupDescriptionLabel());
        clusterEditor.setLabel(constants.makeTemplateClusterLabel());
        quotaEditor.setLabel(constants.makeTemplateQuotaLabel());
        isTemplatePublicEditor.setLabel(constants.makeTemplateIsTemplatePublicEditorLabel());
        disksAllocationLabel.setText(constants.disksAllocation());
    }

    @Override
    public void edit(final UnitVmModel model) {
        driver.edit(model);

        model.getStorageDomain().getItemsChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                addDiskAllocation(model);
            }
        });

        model.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).PropertyName;
                if ("Message".equals(propName)) { //$NON-NLS-1$
                    appendMessage(model.getMessage());
                }
            }
        });
    }

    private void addDiskAllocation(UnitVmModel model) {
        disksAllocationView.edit(model.getDisksAllocationModel());
        model.getDisksAllocationModel().setDisks(model.getDisks());
    }

    @Override
    public UnitVmModel flush() {
        return driver.flush();
    }

    @Override
    public void focusInput() {
        nameEditor.setFocus(true);
    }

    @Override
    public int setTabIndexes(int nextTabIndex) {
        nameEditor.setTabIndex(nextTabIndex++);
        descriptionEditor.setTabIndex(nextTabIndex++);
        clusterEditor.setTabIndex(nextTabIndex++);
        quotaEditor.setTabIndex(nextTabIndex++);
        nextTabIndex = disksAllocationView.setTabIndexes(nextTabIndex);
        isTemplatePublicEditor.setTabIndex(nextTabIndex++);
        return nextTabIndex;
    }

    public void appendMessage(String message) {
        if (message == null) {
            return;
        }

        messagePanel.add(new Label(message));
    }

}
