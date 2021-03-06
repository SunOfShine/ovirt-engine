package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.storage.StoragePoolValidator;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RemoveAllVmImagesParameters;
import org.ovirt.engine.core.common.action.RemoveVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@LockIdNameAttribute
@NonTransactiveCommandAttribute(forceCompensation = true)
public class RemoveVmCommand<T extends RemoveVmParameters> extends VmCommand<T> implements QuotaStorageDependent{

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected RemoveVmCommand(Guid commandId) {
        super(commandId);
    }

    public RemoveVmCommand(T parameters) {
        super(parameters);
        parameters.setEntityId(getVmId());
        if (getVm() != null) {
            setStoragePoolId(getVm().getStoragePoolId());
        }
    }

    @Override
    protected void executeVmCommand() {
        if (getVm().getStatus() != VMStatus.ImageLocked) {
            VmHandler.LockVm(getVm().getDynamicData(), getCompensationContext());
        }
        freeLock();
        setSucceeded(removeVm());
    }

    private boolean removeVm() {
        final List<DiskImage> diskImages = ImagesHandler.filterImageDisks(getVm().getDiskList(),
                true,
                false);

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                removeVmFromDb();
                if (getParameters().isRemoveDisks()) {
                    for (DiskImage image : diskImages) {
                        getCompensationContext().snapshotEntityStatus(image.getImage(), ImageStatus.ILLEGAL);
                        ImagesHandler.updateImageStatus(image.getImage().getId(), ImageStatus.LOCKED);
                    }
                    getCompensationContext().stateChanged();
                }
                return null;
            }
        });

        if (getParameters().isRemoveDisks() && !diskImages.isEmpty()) {
            Collection<DiskImage> unremovedDisks = (Collection<DiskImage>)removeVmImages(diskImages).getActionReturnValue();
            if (!unremovedDisks.isEmpty()) {
                processUnremovedDisks(unremovedDisks);
                return false;
            }
        }

        return true;
    }

    @Override
    protected boolean canDoAction() {
        if (getVm() == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (getVm().isDeleteProtected()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_DELETE_PROTECTION_ENABLED);
        }

        VmHandler.updateDisksFromDb(getVm());

        if (!getParameters().isRemoveDisks() && !canRemoveVmWithDetachDisks()) {
            return false;
        }

        return (super.canDoAction() && canRemoveVm());
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
    }

    public static boolean IsVmRunning(Guid vmId) {
        VM vm = DbFacade.getInstance().getVmDao().get(vmId);
        if (vm != null) {
            return vm.isRunningOrPaused() || vm.getStatus() == VMStatus.Unknown;
        }
        return false;
    }

    private boolean isVmInPool(Guid vmId) {
        return getVm().getVmPoolId() != null;
    }

    private boolean canRemoveVm() {
        if (IsVmRunning(getVmId()) || (getVm().getStatus() == VMStatus.NotResponding)) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_RUNNING);
        }
        if (getVm().getStatus() == VMStatus.Suspended) {
            return failCanDoAction(VdcBllMessages.VM_CANNOT_REMOVE_VM_WHEN_STATUS_IS_NOT_DOWN);
        }
        if (isVmInPool(getVmId())) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_ATTACHED_TO_POOL);
        }

        // enable to remove vms without images
        SnapshotsValidator snapshotsValidator = new SnapshotsValidator();
        if (!validate(snapshotsValidator.vmNotDuringSnapshot(getVmId()))) {
            return false;
        }

        if (getParameters().getForce() && !validate(snapshotsValidator.vmNotInPreview(getVmId()))) {
            return false;
        }

        if (!validate(new StoragePoolValidator(getStoragePool()).isUp())) {
            return false;
        }

        Collection<Disk> vmDisks = getVm().getDiskMap().values();
        List<DiskImage> vmImages = ImagesHandler.filterImageDisks(vmDisks, true, false);
        if (!vmImages.isEmpty()) {
            Set<Guid> storageIds = ImagesHandler.getAllStorageIdsForImageIds(vmImages);
            MultipleStorageDomainsValidator storageValidator = new MultipleStorageDomainsValidator(getVm().getStoragePoolId(), storageIds);
            if (!validate(storageValidator.allDomainsExistAndActive())) {
                return false;
            }

            DiskImagesValidator diskImagesValidator = new DiskImagesValidator(vmImages);
            if (!getParameters().getForce() && !validate(diskImagesValidator.diskImagesNotLocked())) {
                return false;
            }
        }

        // Handle VM status with ImageLocked
        VmValidator vmValidator = new VmValidator(getVm());
        ValidationResult vmLockedValidatorResult = vmValidator.vmNotLocked();
        if (!vmLockedValidatorResult.isValid()) {
            // without force remove, we can't remove the VM
            if (!getParameters().getForce()) {
                return failCanDoAction(vmLockedValidatorResult.getMessage());
            }

            // If it is force, we cannot remove if there are task
            if (AsyncTaskManager.getInstance().HasTasksByStoragePoolId(getVm().getStoragePoolId())) {
                return failCanDoAction(VdcBllMessages.VM_CANNOT_REMOVE_HAS_RUNNING_TASKS);
            }
        }

        return true;
    }

    private boolean canRemoveVmWithDetachDisks() {
        if (!Guid.Empty.equals(getVm().getVmtGuid())) {
            return failCanDoAction(VdcBllMessages.VM_CANNOT_REMOVE_WITH_DETACH_DISKS_BASED_ON_TEMPLATE);
        }


        boolean isSnapshotsPresent = false;
        for (Disk disk : getVm().getDiskList()) {
            List<DiskImage> diskImageList = getDiskImageDao().getAllSnapshotsForImageGroup(disk.getId());
            if (diskImageList.size() > 1) {
                isSnapshotsPresent = true;
                break;
            }
        }

        if (isSnapshotsPresent) {
            return failCanDoAction(VdcBllMessages.VM_CANNOT_REMOVE_WITH_DETACH_DISKS_SNAPSHOTS_EXIST);
        }

        return true;
    }

    protected VdcReturnValueBase removeVmImages(List<DiskImage> images) {
        RemoveAllVmImagesParameters tempVar = new RemoveAllVmImagesParameters(getVmId(), images);
        tempVar.setParentCommand(getActionType());
        tempVar.setEntityId(getParameters().getEntityId());
        tempVar.setParentParameters(getParameters());
        VdcReturnValueBase vdcRetValue =
                Backend.getInstance().runInternalAction(VdcActionType.RemoveAllVmImages,
                        tempVar,
                        ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));

        if (vdcRetValue.getSucceeded()) {
            getReturnValue().getTaskIdList().addAll(vdcRetValue.getInternalTaskIdList());
        }

        return vdcRetValue;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_VM_FINISHED : AuditLogType.USER_REMOVE_VM_FINISHED_WITH_ILLEGAL_DISKS;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getVmId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    protected void removeVmFromDb() {
        removeLunDisks();
        removeVmUsers();
        removeVmNetwork();
        new SnapshotsManager().removeSnapshots(getVmId());
        removeVmStatic();
    }

    /**
     * The following method will perform a removing of all lunDisks from vm.
     * These is only DB operation
     */
    private void removeLunDisks() {
        List<LunDisk> lunDisks =
                ImagesHandler.filterDiskBasedOnLuns(getVm().getDiskMap().values());
        for (LunDisk lunDisk : lunDisks) {
            ImagesHandler.removeLunDisk(lunDisk);
        }
    }

    @Override
    protected void endVmCommand() {
        // no audit log print here as the vm was already removed during the execute phase.
        setCommandShouldBeLogged(false);

        setSucceeded(true);
    }

    private void processUnremovedDisks(Collection<DiskImage> diskImages) {
        List<String> disksLeftInVm = new ArrayList<String>();
        for (DiskImage diskImage : diskImages) {
            disksLeftInVm.add(diskImage.getDiskAlias());
        }
        addCustomValue("DisksNames", StringUtils.join(disksLeftInVm, ","));
    }

    @Override
    public void addQuotaPermissionSubject(List<PermissionSubject> quotaPermissionList) {
        //
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();
        for (DiskImage disk : getVm().getDiskList()){
            if (disk.getQuotaId() != null && !Guid.Empty.equals(disk.getQuotaId())) {
                list.add(new QuotaStorageConsumptionParameter(
                        disk.getQuotaId(),
                        null,
                        QuotaStorageConsumptionParameter.QuotaAction.RELEASE,
                        disk.getStorageIds().get(0),
                        (double)disk.getSizeInGigabytes()));
            }
        }
        return list;
    }
}
