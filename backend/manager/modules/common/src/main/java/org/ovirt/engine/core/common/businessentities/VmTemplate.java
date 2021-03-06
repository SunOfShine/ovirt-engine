package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.Size;
import javax.validation.groups.Default;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.annotation.ValidI18NName;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.ImportClonedEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

public class VmTemplate extends VmBase {
    private static final long serialVersionUID = -5238366659716600486L;

    private List<VmNetworkInterface> interfaces = new ArrayList<VmNetworkInterface>();

    private int childCount;

    private VmTemplateStatus status = VmTemplateStatus.OK;

    private String vdsGroupName;

    private NGuid storagePoolId;

    private String storagePoolName;

    private Map<Guid, DiskImage> diskMap = new HashMap<Guid, DiskImage>();

    private ArrayList<DiskImage> diskList = new ArrayList<DiskImage>();

    private HashMap<Guid, DiskImage> diskTemplateMap = new HashMap<Guid, DiskImage>();

    private double bootDiskSizeGB;

    private double actualDiskSize = 0;

    public VmTemplate() {
        setAutoSuspend(false);
        setNiceLevel(0);
        diskTemplateMap = new HashMap<Guid, DiskImage>();
    }

    private boolean disabled;

    public VmTemplate(int childCount, Date creationDate, String description, int memSizeMb, String name,
            int numOfSockets, int cpuPerSocket, VmOsType os, Guid vdsGroupId, Guid vmtGuid, String domain,
            int numOfMonitors, int status, int usbPolicy, String timeZone, boolean autoSuspend, int niceLevel,
            boolean failBack, BootSequence defaultBootSequence, VmType vmType,
            boolean smartcardEnabled, boolean deleteProtected, Boolean tunnelMigration, String vncKeyboardLayout,
            int minAllocatedMem, boolean stateless, boolean runAndPause) {
        super(
                vmtGuid,
                vdsGroupId,
                os,
                creationDate,
                description,
                memSizeMb,
                numOfSockets,
                cpuPerSocket,
                numOfMonitors,
                domain,
                timeZone,
                vmType,
                UsbPolicy.forValue(usbPolicy),
                failBack,
                defaultBootSequence,
                niceLevel,
                autoSuspend,
                0,
                false,
                stateless,
                null,
                OriginType.valueOf(Config.<String> GetValue(ConfigValues.OriginType)),
                null,
                null,
                null,
                null,
                smartcardEnabled,
                deleteProtected,
                tunnelMigration,
                vncKeyboardLayout,
                minAllocatedMem,
                runAndPause);

        diskTemplateMap = new HashMap<Guid, DiskImage>();

        this.childCount = childCount;
        setName(name);
        this.setNumOfMonitors(numOfMonitors);
        this.setStatus(VmTemplateStatus.forValue(status));
    }

    public int getChildCount() {
        return this.childCount;
    }

    public void setChildCount(int value) {
        this.childCount = value;
    }

    public VmTemplateStatus getStatus() {
        return status;
    }

    public void setStatus(VmTemplateStatus value) {
        status = value;
    }

    public String getVdsGroupName() {
        return vdsGroupName;
    }

    public void setVdsGroupName(String value) {
        vdsGroupName = value;
    }

    @Override
    public List<VmNetworkInterface> getInterfaces() {
        return interfaces;
    }

    @Override
    public void setInterfaces(List<VmNetworkInterface> value) {
        interfaces = value;
    }

    public NGuid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(NGuid value) {
        storagePoolId = value;
    }

    public String getStoragePoolName() {
        return storagePoolName;
    }

    public void setStoragePoolName(String value) {
        storagePoolName = value;
    }

    public double getSizeGB() {
        return bootDiskSizeGB;
    }

    public void setSizeGB(double value) {
        bootDiskSizeGB = value;
    }

    @JsonIgnore
    public HashMap<Guid, DiskImage> getDiskMap() {
        return diskTemplateMap;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    public double getActualDiskSize() {
        if (actualDiskSize == 0 && getDiskImageMap() != null) {
            for (Disk disk : getDiskImageMap().values()) {
                if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
                    actualDiskSize += ((DiskImage) disk).getActualSize();
                }
            }
        }
        return actualDiskSize;
    }

    /**
     * empty setters to fix CXF issue
     */
    public void setActualDiskSize(double actualDiskSize) {
        // Purposely empty
    }

    @JsonIgnore
    public Map<Guid, DiskImage> getDiskImageMap() {
        return diskMap;
    }

    public void setDiskImageMap(Map<Guid, DiskImage> value) {
        diskMap = value;
    }

    @Override
    @JsonIgnore
    public ArrayList<DiskImage> getDiskList() {
        return diskList;
    }

    public void setDiskList(ArrayList<DiskImage> disks) {
        diskList = disks;
    }

    @Override
    public boolean equals(Object obj) {
        boolean returnValue = super.equals(obj);
        if (!returnValue && obj != null && obj instanceof VmTemplate) {
            returnValue = getId()
                    .equals(((VmTemplate) obj).getId());
        }
        return returnValue;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    @Size(min = 1, max = BusinessEntitiesDefinitions.VM_TEMPLATE_NAME_SIZE,
            message = "VALIDATION.VM_TEMPLATE.NAME.MAX",
            groups = { Default.class, ImportClonedEntity.class })
    @ValidI18NName(message = "ACTION_TYPE_FAILED_NAME_MAY_NOT_CONTAIN_SPECIAL_CHARS", groups = { CreateEntity.class,
            UpdateEntity.class, ImportClonedEntity.class })
    public String getName() {
        return super.getName();
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

}
