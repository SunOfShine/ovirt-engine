package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.action.AddVmAndAttachToPoolParameters;
import org.ovirt.engine.core.common.action.AddVmFromScratchParameters;
import org.ovirt.engine.core.common.action.AddVmToPoolParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.VmStatic;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class AddVmAndAttachToPoolCommand<T extends AddVmAndAttachToPoolParameters> extends AddVmCommand<T> {
    public AddVmAndAttachToPoolCommand(T parameters) {
        super(parameters);
    }

    /**
     * This operation may take much time.
     */
    @Override
    protected void executeCommand() {
        VmStatic vmStatic = getParameters().getVmStaticData();
        VdcReturnValueBase returnValueFromAddVm;

        if (VmTemplateHandler.BlankVmTemplateId.equals(vmStatic.getVmtGuid())) {
            returnValueFromAddVm = addVmFromScratch(vmStatic);
        } else {
            returnValueFromAddVm = addVm(vmStatic);
        }

        if (returnValueFromAddVm.getSucceeded()) {
            getTaskIdList().addAll(returnValueFromAddVm.getInternalTaskIdList());
            addVmToPool(vmStatic);
        }
    }

    private VdcReturnValueBase addVmFromScratch(VmStatic vmStatic) {
        AddVmFromScratchParameters parameters = new AddVmFromScratchParameters(vmStatic, getParameters()
                .getDiskInfoList(), getParameters().getStorageDomainId());
        parameters.setSessionId(getParameters().getSessionId());
        parameters.setDontAttachToDefaultTag(true);

        return Backend.getInstance().runInternalAction(VdcActionType.AddVmFromScratch,
                        parameters,
                        ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));
    }

    private VdcReturnValueBase addVm(VmStatic vmStatic) {
        VmManagementParametersBase parameters = new VmManagementParametersBase(vmStatic);
        parameters.setSessionId(getParameters().getSessionId());
        parameters.setDontCheckTemplateImages(true);
        parameters.setDontAttachToDefaultTag(true);
        parameters.setDiskInfoDestinationMap(diskInfoDestinationMap);

        return Backend.getInstance().runInternalAction(VdcActionType.AddVm,
                        parameters,
                        ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));
    }

    private void addVmToPool(VmStatic vmStatic) {
        AddVmToPoolParameters parameters = new AddVmToPoolParameters(getParameters().getPoolId(),
                vmStatic.getId());
        parameters.setShouldBeLogged(false);
        setSucceeded(Backend.getInstance().runInternalAction(VdcActionType.AddVmToPool, parameters).getSucceeded());
        addVmPermission();
    }
}
