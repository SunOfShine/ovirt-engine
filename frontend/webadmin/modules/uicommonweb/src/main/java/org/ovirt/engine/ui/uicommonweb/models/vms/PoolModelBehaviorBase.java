package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;

public abstract class PoolModelBehaviorBase extends VmModelBehaviorBase<PoolModel> {

    private Event poolModelBehaviorInitializedEvent = new Event("PoolModelBehaviorInitializedEvent", //$NON-NLS-1$
            NewPoolModelBehavior.class);

    public Event getPoolModelBehaviorInitializedEvent()
    {
        return poolModelBehaviorInitializedEvent;
    }

    private void setPoolModelBehaviorInitializedEvent(Event value)
    {
        poolModelBehaviorInitializedEvent = value;
    }

    @Override
    public void initialize(SystemTreeItemModel systemTreeSelectedItem)
    {
        super.initialize(systemTreeSelectedItem);

        getModel().getDisksAllocationModel().setIsVolumeFormatAvailable(false);
        getModel().getDisksAllocationModel().setIsAliasChangable(true);

        getModel().getProvisioning().setIsAvailable(false);
        getModel().getProvisioning().setEntity(false);

        AsyncDataProvider.getDataCenterByClusterServiceList(new AsyncQuery(getModel(), new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {

                UnitVmModel model = (UnitVmModel) target;
                ArrayList<StoragePool> list = new ArrayList<StoragePool>();
                for (StoragePool a : (ArrayList<StoragePool>) returnValue) {
                    if (a.getstatus() == StoragePoolStatus.Up) {
                        list.add(a);
                    }
                }
                model.setDataCenter(model, list);

                getPoolModelBehaviorInitializedEvent().raise(this, EventArgs.Empty);

            }
        }, getModel().getHash()), true, false);
    }

    @Override
    public void dataCenter_SelectedItemChanged()
    {
        StoragePool dataCenter = (StoragePool) getModel().getDataCenter().getSelectedItem();

        if (dataCenter == null)
            return;

        getModel().setIsHostAvailable(dataCenter.getstorage_pool_type() != StorageType.LOCALFS);

        AsyncDataProvider.getClusterByServiceList(new AsyncQuery(new Object[] { this, getModel() }, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {

                Object[] array = (Object[]) target;
                PoolModelBehaviorBase behavior = (PoolModelBehaviorBase) array[0];
                UnitVmModel model = (UnitVmModel) array[1];
                ArrayList<VDSGroup> clusters = (ArrayList<VDSGroup>) returnValue;
                model.setClusters(model, clusters, null);
                behavior.initTemplate();
                behavior.initCdImage();

            }
        }, getModel().getHash()), dataCenter.getId(), true, false);

        if (dataCenter.getQuotaEnforcementType() != QuotaEnforcementTypeEnum.DISABLED) {
            getModel().getQuota().setIsAvailable(true);
        } else {
            getModel().getQuota().setIsAvailable(false);
        }
    }

    protected void setupWindowModelFrom(VmBase vmBase) {
        if (vmBase != null) {
            updateQuotaByCluster(vmBase.getQuotaId(), vmBase.getQuotaName());
            // Copy VM parameters from template.
            getModel().getOSType().setSelectedItem(vmBase.getOs());
            getModel().getTotalCPUCores().setEntity(Integer.toString(vmBase.getNumOfCpus()));
            getModel().getNumOfSockets().setSelectedItem(vmBase.getNumOfSockets());
            getModel().getNumOfMonitors().setSelectedItem(vmBase.getNumOfMonitors());
            getModel().getDomain().setSelectedItem(vmBase.getDomain());
            getModel().getMemSize().setEntity(vmBase.getMemSizeMb());
            getModel().getUsbPolicy().setSelectedItem(vmBase.getUsbPolicy());
            getModel().setBootSequence(vmBase.getDefaultBootSequence());
            getModel().getIsHighlyAvailable().setEntity(vmBase.isAutoStartup());
            getModel().getIsDeleteProtected().setEntity(vmBase.isDeleteProtected());
            getModel().getIsSmartcardEnabled().setEntity(vmBase.isSmartcardEnabled());
            getModel().getVncKeyboardLayout().setSelectedItem(vmBase.getVncKeyboardLayout());
            getModel().getIsRunAndPause().setEntity(false);

            boolean hasCd = !StringHelper.isNullOrEmpty(vmBase.getIsoPath());

            getModel().getCdImage().setIsChangable(hasCd);
            getModel().getCdAttached().setEntity(hasCd);
            if (hasCd) {
                getModel().getCdImage().setSelectedItem(vmBase.getIsoPath());
            }

            updateTimeZone(vmBase.getTimeZone());

            // Update domain list
            updateDomain();

            ArrayList<VDSGroup> clusters = (ArrayList<VDSGroup>) getModel().getCluster().getItems();
            VDSGroup selectCluster =
                    Linq.firstOrDefault(clusters, new Linq.ClusterPredicate(vmBase.getVdsGroupId()));

            getModel().getCluster().setSelectedItem((selectCluster != null) ? selectCluster
                    : Linq.firstOrDefault(clusters));

            // Update display protocol selected item
            EntityModel displayProtocol = null;
            boolean isFirst = true;
            for (Object item : getModel().getDisplayProtocol().getItems())
            {
                EntityModel a = (EntityModel) item;
                if (isFirst)
                {
                    displayProtocol = a;
                    isFirst = false;
                }
                DisplayType dt = (DisplayType) a.getEntity();
                if (dt == extractDisplayType(vmBase))
                {
                    displayProtocol = a;
                    break;
                }
            }
            getModel().getDisplayProtocol().setSelectedItem(displayProtocol);

            // By default, take kernel params from template.
            getModel().getKernel_path().setEntity(vmBase.getKernelUrl());
            getModel().getKernel_parameters().setEntity(vmBase.getKernelParams());
            getModel().getInitrd_path().setEntity(vmBase.getInitrdUrl());

            if (!vmBase.getId().equals(Guid.Empty))
            {
                getModel().getStorageDomain().setIsChangable(true);

                getModel().setIsBlankTemplate(false);
                initDisks();
            }
            else
            {
                getModel().getStorageDomain().setIsChangable(false);

                getModel().setIsBlankTemplate(true);
                getModel().setIsDisksAvailable(false);
                getModel().setDisks(null);
            }

            getModel().getProvisioning().setEntity(false);

            initPriority(vmBase.getPriority());
            initStorageDomains();

            // use min. allocated memory from the template, if specified
            if (vmBase.getMinAllocatedMem() == 0) {
                updateMinAllocatedMemory();
            } else {
                getModel().getMinAllocatedMemory().setEntity(vmBase.getMinAllocatedMem());
            }
        }
    }

    @Override
    public void template_SelectedItemChanged() {
        // override if there is a need to do some actions
    }

    protected abstract DisplayType extractDisplayType(VmBase vmBase);

    @Override
    public void cluster_SelectedItemChanged()
    {
        updateDefaultHost();
        updateCustomPropertySheet();
        updateMinAllocatedMemory();
        updateNumOfSockets();
        if ((VmTemplate) getModel().getTemplate().getSelectedItem() != null) {
            VmTemplate template = (VmTemplate) getModel().getTemplate().getSelectedItem();
            updateQuotaByCluster(template.getQuotaId(), template.getQuotaName());
        }
    }

    @Override
    public void defaultHost_SelectedItemChanged()
    {
        updateCdImage();
    }

    @Override
    public void provisioning_SelectedItemChanged()
    {
    }

    @Override
    public void updateMinAllocatedMemory()
    {
        VDSGroup cluster = (VDSGroup) getModel().getCluster().getSelectedItem();
        if (cluster == null)
        {
            return;
        }

        double overCommitFactor = 100.0 / cluster.getmax_vds_memory_over_commit();
        getModel().getMinAllocatedMemory()
                .setEntity((int) ((Integer) getModel().getMemSize().getEntity() * overCommitFactor));
    }

    private void initTemplate()
    {
        StoragePool dataCenter = (StoragePool) getModel().getDataCenter().getSelectedItem();

        AsyncDataProvider.getTemplateListByDataCenter(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target1, Object returnValue1) {

                ArrayList<VmTemplate> loadedTemplates = (ArrayList<VmTemplate>) returnValue1;

                ArrayList<VmTemplate> templates = new ArrayList<VmTemplate>();
                for (VmTemplate template : loadedTemplates) {
                    if (!template.getId().equals(Guid.Empty)) {
                        templates.add(template);
                    }
                }
                getModel().getTemplate().setItems(templates);
                setupSelectedTemplate(getModel().getTemplate(), templates);
            }
        }), dataCenter.getId());

    }

    protected abstract void setupSelectedTemplate(ListModel model, List<VmTemplate> templates);

    private void postInitTemplate(ArrayList<VmTemplate> templates)
    {
        // If there was some template selected before, try select it again.
        VmTemplate oldTemplate = (VmTemplate) getModel().getTemplate().getSelectedItem();

        getModel().getTemplate().setItems(templates);

        getModel().getTemplate().setSelectedItem(Linq.firstOrDefault(templates,
                oldTemplate != null ? new Linq.TemplatePredicate(oldTemplate.getId())
                        : new Linq.TemplatePredicate(Guid.Empty)));
    }

    public void initCdImage()
    {
        updateCdImage();
    }

    @Override
    public void updateIsDisksAvailable()
    {
        getModel().setIsDisksAvailable(getModel().getDisks() != null
                && getModel().getProvisioning().getIsChangable());
    }

    @Override
    public boolean validate() {
        boolean isNew = getModel().getIsNew();
        int maxAllowedVms = getMaxVmsInPool();
        int assignedVms = getModel().getAssignedVms().asConvertible().integer();

        getModel().getNumOfDesktops().validateEntity(

                new IValidation[]
                {
                        new NotEmptyValidation(),
                        new LengthValidation(4),
                        new IntegerValidation(isNew ? 1 : 0, isNew ? maxAllowedVms : maxAllowedVms - assignedVms)
                });

        getModel().getPrestartedVms().validateEntity(
                new IValidation[]
                {
                        new NotEmptyValidation(),
                        new IntegerValidation(0, assignedVms)
                });

        getModel().getMaxAssignedVmsPerUser().validateEntity(
                new IValidation[]
                {
                        new NotEmptyValidation(),
                        new IntegerValidation(1, assignedVms)
                });

        getModel().setIsGeneralTabValid(getModel().getIsGeneralTabValid()
                && getModel().getName().getIsValid()
                && getModel().getNumOfDesktops().getIsValid()
                && getModel().getPrestartedVms().getIsValid()
                && getModel().getMaxAssignedVmsPerUser().getIsValid());

        getModel().setIsPoolTabValid(true);

        return super.validate()
                && getModel().getName().getIsValid()
                && getModel().getNumOfDesktops().getIsValid()
                && getModel().getPrestartedVms().getIsValid()
                && getModel().getMaxAssignedVmsPerUser().getIsValid();
    }
}
