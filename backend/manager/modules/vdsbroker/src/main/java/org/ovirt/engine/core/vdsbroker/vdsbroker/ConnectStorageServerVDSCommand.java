package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.ConnectStorageServerVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.collections.DefaultValueMap;

public class ConnectStorageServerVDSCommand<P extends ConnectStorageServerVDSCommandParameters>
        extends VdsBrokerCommand<P> {
    protected ServerConnectionStatusReturnForXmlRpc _result;

    public ConnectStorageServerVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        _result = getBroker().connectStorageServer(getParameters().getStorageType().getValue(),
                getParameters().getStoragePoolId().toString(), BuildStructFromConnectionListObject());
        ProceedProxyReturnValue();
        Map<String, String> returnValue = _result.convertToStatusList();
        setReturnValue(returnValue);
        logFailedStorageConnections(returnValue);
    }

    @Override
    protected void ProceedProxyReturnValue() {
        VdcBllErrors returnStatus = GetReturnValueFromStatus(getReturnStatus());
        switch (returnStatus) {
        case StorageServerConnectionRefIdAlreadyInUse:
        case StorageServerConnectionRefIdDoesNotExist:
            VDSExceptionBase outEx = new VDSErrorException(String.format("Failed in vdscommand %1$s, error = %2$s",
                    getCommandName(), getReturnStatus().mMessage));
            InitializeVdsError(returnStatus);
            getVDSReturnValue().setSucceeded(false);
            throw outEx;
        default:
            super.ProceedProxyReturnValue();
            break;
        }
    }

    @SuppressWarnings("unchecked")
    protected Map<String, String>[] BuildStructFromConnectionListObject() {
        final StoragePool storagePool =
                DbFacade.getInstance().getStoragePoolDao().getForVds(getParameters().getVdsId());
        final Map<String, String>[] result = new HashMap[getParameters().getConnectionList().size()];
        int i = 0;
        for (StorageServerConnections connection : getParameters().getConnectionList()) {
            result[i] = CreateStructFromConnection(connection, storagePool);
            i++;
        }
        return result;
    }

    public static Map<String, String> CreateStructFromConnection(final StorageServerConnections connection,
            final StoragePool storagePool) {
        // for information, see _connectionDict2ConnectionInfo in vdsm/storage/hsm.py
        DefaultValueMap con = new DefaultValueMap();
        con.put("id", connection.getid(), Guid.Empty.toString());
        con.put("connection", connection.getconnection(), "");
        con.put("portal", connection.getportal(), "");
        con.put("port", connection.getport(), "");
        con.put("iqn", connection.getiqn(), "");
        con.put("user", connection.getuser_name(), "");
        con.put("password", connection.getpassword(), "");

        // storage_pool can be null when discovering iscsi send targets or when connecting
        // through vds which has no storage pool
        if (storagePool == null || Config.<Boolean> GetValue(ConfigValues.AdvancedNFSOptionsEnabled,
                storagePool.getcompatibility_version().getValue())) {
            // For mnt_options, vfs_type, and protocol_version - if they are null
            // or empty we should not send a key with an empty value
            con.putIfNotEmpty("mnt_options", connection.getMountOptions());
            con.putIfNotEmpty("vfs_type", connection.getVfsType());
            if (connection.getNfsVersion() != null) {
                con.put("protocol_version", connection.getNfsVersion().getValue());
            }
            con.putIfNotEmpty("timeout", connection.getNfsTimeo());
            con.putIfNotEmpty("retrans", connection.getNfsRetrans());
        }
        return con;
    }

    private void logFailedStorageConnections(Map<String, String> returnValue) {
        StringBuilder failedDomainNames = new StringBuilder();
        String namesSeparator = ",";
        for (Entry<String, String> result : returnValue.entrySet()) {
            if (!"0".equals(result.getValue())) {
                List<StorageDomain> domains =
                        DbFacade.getInstance().getStorageDomainDao().getAllByConnectionId(new Guid(result.getKey()));
                if (!domains.isEmpty()) {
                    for (StorageDomain domain : domains) {
                        if (failedDomainNames.length() > 0) {
                            failedDomainNames.append(namesSeparator);
                        }
                        failedDomainNames.append(domain.getStorageName());
                    }
                }
            }
        }

        if (failedDomainNames.length() > 0) {
            AuditLogableBase logable = new AuditLogableBase(getParameters().getVdsId());
            logable.addCustomValue("failedStorageDomains", failedDomainNames.toString());
            AuditLogDirector.log(logable, AuditLogType.VDS_STORAGES_CONNECTION_FAILED);
        }
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _result.mStatus;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _result;
    }
}
