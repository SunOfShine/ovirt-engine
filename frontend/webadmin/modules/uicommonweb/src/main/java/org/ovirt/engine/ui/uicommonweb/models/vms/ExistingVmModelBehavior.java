package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;

@SuppressWarnings("unused")
public class ExistingVmModelBehavior extends VmModelBehaviorBase
{
    protected VM vm;

    public ExistingVmModelBehavior(VM vm)
    {
        this.vm = vm;
    }

    public VM getVm() {
        return vm;
    }

    public void setVm(VM vm) {
        this.vm = vm;
    }

    @Override
    public void initialize(SystemTreeItemModel systemTreeSelectedItem)
    {
        super.initialize(systemTreeSelectedItem);
        AsyncDataProvider.getDataCenterById(new AsyncQuery(getModel(),
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        UnitVmModel model = (UnitVmModel) target;
                        if (returnValue != null)
                        {
                            StoragePool dataCenter = (StoragePool) returnValue;
                            ArrayList<StoragePool> list =
                                    new ArrayList<StoragePool>(Arrays.asList(new StoragePool[] { dataCenter }));
                            model.setDataCenter(model, list);
                            model.getDataCenter().setIsChangable(false);
                        }
                        else
                        {
                            ExistingVmModelBehavior behavior = (ExistingVmModelBehavior) model.getBehavior();
                            VM currentVm = behavior.vm;
                            VDSGroup tempVar = new VDSGroup();
                            tempVar.setId(currentVm.getVdsGroupId());
                            tempVar.setname(currentVm.getVdsGroupName());
                            tempVar.setcompatibility_version(currentVm.getVdsGroupCompatibilityVersion());
                            tempVar.setStoragePoolId(currentVm.getStoragePoolId());
                            VDSGroup cluster = tempVar;
                            model.getCluster()
                                    .setItems(new ArrayList<VDSGroup>(Arrays.asList(new VDSGroup[] { cluster })));
                            model.getCluster().setSelectedItem(cluster);
                            behavior.initTemplate();
                            behavior.initCdImage();
                        }

                    }
                },
                getModel().getHash()),
                vm.getStoragePoolId());
        AsyncDataProvider.GetWatchdogByVmId(new AsyncQuery(this.getModel(), new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                UnitVmModel model = (UnitVmModel) target;
                VdcQueryReturnValue val = (VdcQueryReturnValue) returnValue;
                @SuppressWarnings("unchecked")
                Collection<VmWatchdog> watchdogs = (Collection<VmWatchdog>) val.getReturnValue();
                for(VmWatchdog watchdog: watchdogs) {
                    model.getWatchdogAction().setSelectedItem(watchdog.getAction() == null ? null
                            : watchdog.getAction().name().toLowerCase());
                    model.getWatchdogModel().setSelectedItem(watchdog.getModel() == null ? ""
                            : watchdog.getModel().name());
                }
            }
        }), vm.getId());
    }

    @Override
    public void dataCenter_SelectedItemChanged()
    {
        StoragePool dataCenter = (StoragePool) getModel().getDataCenter().getSelectedItem();

        getModel().setIsHostAvailable(dataCenter.getstorage_pool_type() != StorageType.LOCALFS);

        AsyncDataProvider.getClusterByServiceList(new AsyncQuery(new Object[] { this, getModel() },
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        Object[] array = (Object[]) target;
                        ExistingVmModelBehavior behavior = (ExistingVmModelBehavior) array[0];
                        UnitVmModel model = (UnitVmModel) array[1];
                        VM vm = ((ExistingVmModelBehavior) array[0]).vm;
                        ArrayList<VDSGroup> clusters = (ArrayList<VDSGroup>) returnValue;
                        model.setClusters(model, clusters, vm.getVdsGroupId().getValue());
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

    @Override
    public void template_SelectedItemChanged()
    {
        // This method will be called even if a VM created from Blank template.

        // Update model state according to VM properties.
        getModel().getName().setEntity(vm.getName());
        getModel().getDescription().setEntity(vm.getVmDescription());
        getModel().getMemSize().setEntity(vm.getVmMemSizeMb());
        getModel().getMinAllocatedMemory().setEntity(vm.getMinAllocatedMem());
        getModel().getOSType().setSelectedItem(vm.getVmOs());
        getModel().getDomain().setSelectedItem(vm.getVmDomain());
        getModel().getUsbPolicy().setSelectedItem(vm.getUsbPolicy());
        getModel().getNumOfMonitors().setSelectedItem(vm.getNumOfMonitors());
        getModel().getAllowConsoleReconnect().setEntity(vm.getAllowConsoleReconnect());
        getModel().setBootSequence(vm.getDefaultBootSequence());
        getModel().getIsHighlyAvailable().setEntity(vm.isAutoStartup());

        getModel().getTotalCPUCores().setEntity(Integer.toString(vm.getNumOfCpus()));
        getModel().getTotalCPUCores().setIsChangable(!vm.isRunning());

        getModel().getIsStateless().setEntity(vm.isStateless());
        getModel().getIsStateless().setIsAvailable(vm.getVmPoolId() == null);

        getModel().getIsRunAndPause().setEntity(vm.isRunAndPause());
        getModel().getIsRunAndPause().setIsChangable(!vm.isRunning());
        getModel().getIsRunAndPause().setIsAvailable(vm.getVmPoolId() == null);

        getModel().getIsSmartcardEnabled().setEntity(vm.isSmartcardEnabled());
        getModel().getIsDeleteProtected().setEntity(vm.isDeleteProtected());

        getModel().getNumOfSockets().setSelectedItem(vm.getNumOfSockets());
        getModel().getNumOfSockets().setIsChangable(!vm.isRunning());

        getModel().getCoresPerSocket().setIsChangable(!vm.isRunning());

        getModel().getKernel_parameters().setEntity(vm.getKernelParams());
        getModel().getKernel_path().setEntity(vm.getKernelUrl());
        getModel().getInitrd_path().setEntity(vm.getInitrdUrl());

        getModel().getCustomProperties().setEntity(vm.getCustomProperties());
        getModel().getCustomPropertySheet().setEntity(vm.getCustomProperties());
        getModel().getCpuPinning().setEntity(vm.getCpuPinning());

        Frontend.RunQuery(VdcQueryType.GetWatchdog, new IdQueryParameters(getVm().getId()), new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                @SuppressWarnings("unchecked")
                List<VmWatchdog> watchdogs = (List<VmWatchdog>) ((VdcQueryReturnValue)returnValue).getReturnValue();
                if(watchdogs.isEmpty()) {
                    getModel().getWatchdogAction().setSelectedItem(null);
                    getModel().getWatchdogModel().setSelectedItem(null);
                } else {
                    VmWatchdog vmWatchdog = watchdogs.get(0);
                    getModel().getWatchdogAction().setSelectedItem(vmWatchdog.getAction() == null ? null
                            : vmWatchdog.getAction().name().toLowerCase());
                    getModel().getWatchdogModel().setSelectedItem(vmWatchdog.getModel() == null ? ""
                            : vmWatchdog.getModel().name());
                }
            }
        }));


        getModel().getVncKeyboardLayout().setSelectedItem(vm.getVncKeyboardLayout());

        if (vm.isInitialized())
        {
            getModel().getTimeZone()
                    .setChangeProhibitionReason("Time Zone cannot be change since the Virtual Machine was booted at the first time."); //$NON-NLS-1$
            getModel().getTimeZone().setIsChangable(false);
        }

        updateTimeZone(vm.getTimeZone());

        // Update domain list
        updateDomain();

        updateHostPinning(vm.getMigrationSupport());
        getModel().getHostCpu().setEntity(vm.isUseHostCpuFlags());

        // Storage domain and provisioning are not available for an existing VM.
        getModel().getStorageDomain().setIsChangable(false);
        getModel().getProvisioning().setIsAvailable(false);
        getModel().getProvisioning().setEntity(Guid.Empty.equals(vm.getVmtGuid()));

        // Select display protocol.
        for (Object item : getModel().getDisplayProtocol().getItems())
        {
            EntityModel model = (EntityModel) item;
            DisplayType displayType = (DisplayType) model.getEntity();

            if (displayType == vm.getDefaultDisplayType())
            {
                getModel().getDisplayProtocol().setSelectedItem(item);
                break;
            }
        }

        initPriority(vm.getPriority());
    }

    @Override
    public void cluster_SelectedItemChanged()
    {
        updateDefaultHost();
        updateCustomPropertySheet();
        updateNumOfSockets();
        updateQuotaByCluster(vm.getQuotaId(), vm.getQuotaName());
        updateCpuPinningVisibility();
    }

    @Override
    protected void changeDefualtHost()
    {
        super.changeDefualtHost();
        doChangeDefautlHost(vm.getDedicatedVmForVds());
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

        if ((Integer) getModel().getMemSize().getEntity() < vm.getVmMemSizeMb())
        {
            double overCommitFactor = 100.0 / cluster.getmax_vds_memory_over_commit();
            getModel().getMinAllocatedMemory()
                    .setEntity((int) ((Integer) getModel().getMemSize().getEntity() * overCommitFactor));
        }
        else
        {
            getModel().getMinAllocatedMemory().setEntity(vm.getMinAllocatedMem());
        }
    }

    public void initTemplate()
    {
        setupTemplate(vm, getModel().getTemplate());
    }

    public void initCdImage()
    {
        getModel().getCdImage().setSelectedItem(vm.getIsoPath());

        boolean hasCd = !StringHelper.isNullOrEmpty(vm.getIsoPath());
        getModel().getCdImage().setIsChangable(hasCd);
        getModel().getCdAttached().setEntity(hasCd);

        updateCdImage();
    }
}
