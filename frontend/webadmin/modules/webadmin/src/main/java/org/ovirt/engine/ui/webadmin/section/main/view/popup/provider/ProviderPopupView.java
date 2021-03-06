package org.ovirt.engine.ui.webadmin.section.main.view.popup.provider;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.HasUiCommandClickHandlers;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelPasswordBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.provider.ProviderPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Image;
import com.google.inject.Inject;

public class ProviderPopupView extends AbstractModelBoundPopupView<ProviderModel> implements ProviderPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<ProviderModel, ProviderPopupView> {}

    private final Driver driver = GWT.create(Driver.class);

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ProviderPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<ProviderPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @Path(value = "name.entity")
    @WithElementId
    EntityModelTextBoxEditor nameEditor;

    @UiField(provided = true)
    @Path(value = "type.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Object> typeEditor;

    @UiField
    @Path(value = "description.entity")
    @WithElementId
    EntityModelTextBoxEditor descriptionEditor;

    @UiField
    @Path(value = "url.entity")
    @WithElementId
    EntityModelTextBoxEditor urlEditor;

    @UiField
    UiCommandButton testButton;

    @UiField
    Image testResultImage;

    @UiField(provided = true)
    @Path(value = "requiresAuthentication.entity")
    @WithElementId
    EntityModelCheckBoxEditor requiresAuthenticationEditor;

    @UiField
    @Path(value = "username.entity")
    @WithElementId
    EntityModelTextBoxEditor usernameEditor;

    @UiField
    @Path(value = "password.entity")
    @WithElementId
    EntityModelPasswordBoxEditor passwordEditor;

    @UiField
    Style style;

    private ApplicationResources resources;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Inject
    public ProviderPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);

        typeEditor = new ListModelListBoxEditor<Object>(new EnumRenderer());
        requiresAuthenticationEditor = new EntityModelCheckBoxEditor(Align.RIGHT);

        this.resources = resources;
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize(constants);
        addContentStyleName(style.contentStyle());
        driver.initialize(this);
    }

    void localize(ApplicationConstants constants) {
        nameEditor.setLabel(constants.nameProvider());
        typeEditor.setLabel(constants.typeProvider());
        descriptionEditor.setLabel(constants.descriptionProvider());
        urlEditor.setLabel(constants.urlProvider());
        testButton.setLabel(constants.testProvider());
        requiresAuthenticationEditor.setLabel(constants.requiresAuthenticationProvider());
        usernameEditor.setLabel(constants.usernameProvider());
        passwordEditor.setLabel(constants.passwordProvider());
    }

    @Override
    public void edit(ProviderModel object) {
        driver.edit(object);
    }

    @Override
    public ProviderModel flush() {
        return driver.flush();
    }

    @Override
    public void focusInput() {
        nameEditor.setFocus(true);
    }

    public void addContentStyleName(String styleName) {
        this.asWidget().addContentStyleName(styleName);
    }

    interface Style extends CssResource {
        String contentStyle();
        String testResultImageStyle();
    }

    @Override
    public HasUiCommandClickHandlers getTestButton() {
        return testButton;
    }

    @Override
    public void setTestResultImage(boolean succeeded) {
        testResultImage.setResource(succeeded ? resources.logNormalImage() : resources.logErrorImage());
        testResultImage.setStylePrimaryName(style.testResultImageStyle());
    }
}
