package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VmReplicateDiskParameters;

public class VmReplicateDiskFinishVDSCommand<P extends VmReplicateDiskParameters> extends VmReplicateDiskVDSCommand<P> {

    public VmReplicateDiskFinishVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status = getBroker().diskReplicateFinish(
                getParameters().getVmId().toString(), getSrcDiskXmlRpc(), getDstDiskXmlRpc());
        ProceedProxyReturnValue();
    }
}
