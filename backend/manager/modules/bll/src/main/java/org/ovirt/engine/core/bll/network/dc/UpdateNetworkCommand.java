package org.ovirt.engine.core.bll.network.dc;

import java.util.List;

import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class UpdateNetworkCommand<T extends AddNetworkStoragePoolParameters> extends NetworkCommon<T> {
    private List<VDSGroup> _clusters;

    public UpdateNetworkCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        DbFacade.getInstance().getNetworkDao().update(getParameters().getNetwork());

        for (VDSGroup cluster : _clusters) {
            NetworkClusterHelper.setStatus(cluster.getId(), getParameters().getNetwork());
        }
        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        List<Network> networks = DbFacade.getInstance().getNetworkDao().getAll();

        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__NETWORK);

        if (getStoragePool() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST);
            return false;
        }

        if (!validateVmNetwork()) {
            return false;
        }

        if (!validateStpProperty()) {
            return false;
        }

        if (!validateMTUOverrideSupport()) {
            return false;
        }

        // check that network name not start with 'bond'
        if (getParameters().getNetwork().getname().toLowerCase().startsWith("bond")) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_CANNOT_CONTAIN_BOND_NAME);
            return false;
        }

        if (!validateVlanId(networks)) {
            return false;
        }

        // check that network not exsits
        Network oldNetwork = LinqUtils.firstOrNull(networks, new Predicate<Network>() {
            @Override
            public boolean eval(Network n) {
                return n.getId().equals(getParameters().getNetwork().getId());
            }
        });
        if (oldNetwork == null) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_NOT_EXISTS);
            return false;
        }

        // check defalut network name is not renamed
        String defaultNetwork = Config.<String> GetValue(ConfigValues.ManagementNetwork);
        if (oldNetwork.getname().equals(defaultNetwork) &&
                !getParameters().getNetwork().getname().equals(defaultNetwork)) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_CAN_NOT_REMOVE_DEFAULT_NETWORK);
            return false;
        }

        Network net = LinqUtils.firstOrNull(networks, new Predicate<Network>() {
            @Override
            public boolean eval(Network n) {
                return n.getname().trim().toLowerCase()
                        .equals(getParameters().getNetwork().getname().trim().toLowerCase())
                        && !n.getId().equals(getParameters().getNetwork().getId())
                        && getParameters().getNetwork().getstorage_pool_id().equals(n.getstorage_pool_id());
            }
        });
        if (net != null) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_IN_USE);
            return false;
        }

        // check if the network in use with running vm
        _clusters = DbFacade.getInstance().getVdsGroupDao().getAllForStoragePool(getStoragePool().getId());
        for (VDSGroup cluster : _clusters) {
            List<VmStatic> vms = DbFacade.getInstance().getVmStaticDao().getAllByGroupAndNetworkName(cluster.getId(),
                    getParameters().getNetwork().getname());
            if (vms.size() > 0) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_IN_USE_BY_VM);
                return false;
            }
        }

        return RemoveNetworkCommand.CommonNetworkValidation(oldNetwork, getReturnValue().getCanDoActionMessages());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_UPDATE_NETWORK : AuditLogType.NETWORK_UPDATE_NETWORK_FAILED;
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateEntity.class);
        return super.getValidationGroups();
    }
}