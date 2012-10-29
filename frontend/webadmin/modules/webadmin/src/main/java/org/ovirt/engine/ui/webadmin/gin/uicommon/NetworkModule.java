package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.DetailTabModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainTabModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkHostListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkTemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkVmListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.PermissionsPopupPresenterWidget;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class NetworkModule extends AbstractGinModule {

    // Main List Model

    @Provides
    @Singleton
    public MainModelProvider<Network, NetworkListModel> getNetworkListProvider(ClientGinjector ginjector) {
        return new MainTabModelProvider<Network, NetworkListModel>(ginjector, NetworkListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(NetworkListModel source,
                    UICommand lastExecutedCommand, Model windowModel) {

                return super.getModelPopup(source, lastExecutedCommand, windowModel);
            }

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(NetworkListModel source,
                    UICommand lastExecutedCommand) {

                    return super.getConfirmModelPopup(source, lastExecutedCommand);
            }


        };
    }

    // Form Detail Models

    @Provides
    @Singleton
    public DetailModelProvider<NetworkListModel, NetworkGeneralModel> getNetworkGeneralProvider(ClientGinjector ginjector) {
        return new DetailTabModelProvider<NetworkListModel, NetworkGeneralModel>(ginjector,
                NetworkListModel.class,
                NetworkGeneralModel.class);
    }

    // Searchable Detail Models

    @Provides
    @Singleton
    public SearchableDetailModelProvider<VDSGroup, NetworkListModel, NetworkClusterListModel> getNetworkClusterListProvider(ClientGinjector ginjector) {
        return new SearchableDetailTabModelProvider<VDSGroup, NetworkListModel, NetworkClusterListModel>(ginjector,
                NetworkListModel.class,
                NetworkClusterListModel.class);
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<VDS, NetworkListModel, NetworkHostListModel> getNetworkHostListProvider(ClientGinjector ginjector) {
        return new SearchableDetailTabModelProvider<VDS, NetworkListModel, NetworkHostListModel>(ginjector,
                NetworkListModel.class,
                NetworkHostListModel.class);
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<VM, NetworkListModel,NetworkVmListModel> getNetworkVmModelProvider(ClientGinjector ginjector) {
        return new SearchableDetailTabModelProvider<VM, NetworkListModel, NetworkVmListModel>(ginjector,
                NetworkListModel.class,
                NetworkVmListModel.class);
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<VmTemplate, NetworkListModel, NetworkTemplateListModel> geNetworkTemplateModelProvider(ClientGinjector ginjector) {
        return new SearchableDetailTabModelProvider<VmTemplate, NetworkListModel, NetworkTemplateListModel>(ginjector,
                NetworkListModel.class,
                NetworkTemplateListModel.class);
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<permissions, NetworkListModel, PermissionListModel> getNetworkPermissionListProvider(ClientGinjector ginjector,
            final Provider<PermissionsPopupPresenterWidget> popupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<permissions, NetworkListModel, PermissionListModel>(ginjector,
                NetworkListModel.class,
                PermissionListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(PermissionListModel source,
                    UICommand lastExecutedCommand, Model windowModel) {
                if (lastExecutedCommand == getModel().getAddCommand()) {
                    return popupProvider.get();
                } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(PermissionListModel source,
                    UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getRemoveCommand()) {
                    return removeConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(source, lastExecutedCommand);
                }
            }
        };
    }

    @Override
    protected void configure() {
    }

}