package org.ovirt.engine.core.bll.storage;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.LockIdNameAttribute;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;

import java.util.Collections;
import java.util.Map;

@NonTransactiveCommandAttribute
@LockIdNameAttribute
public class RemoveStorageServerConnectionCommand<T extends StorageServerConnectionParametersBase> extends DisconnectStorageServerConnectionCommand {

    public RemoveStorageServerConnectionCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        String connectionId = getConnection().getid();
        if(StringUtils.isNotEmpty(connectionId)) {
             getDbFacade().getStorageServerConnectionDao().remove(connectionId);
        }

        //disconnect the connection from vdsm
        disconnectStorage();
        setSucceeded(true);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getConnection().getconnection(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE_CONNECTION,
                        VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

}
