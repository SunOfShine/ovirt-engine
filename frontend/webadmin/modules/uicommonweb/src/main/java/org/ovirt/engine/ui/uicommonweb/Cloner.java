package org.ovirt.engine.ui.uicommonweb;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatistics;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkStatistics;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.compat.NotImplementedException;
import org.ovirt.engine.core.compat.Version;

@SuppressWarnings("unused")
public final class Cloner
{
    public static Object clone(Object instance)
    {
        if (instance instanceof VM)
        {
            return cloneVM((VM) instance);
        }
        if (instance instanceof VDS)
        {
            return cloneVDS((VDS) instance);
        }
        if (instance instanceof VDSGroup)
        {
            return cloneVDSGroup((VDSGroup) instance);
        }
        if (instance instanceof StoragePool)
        {
            return cloneStorage_pool((StoragePool) instance);
        }
        if (instance instanceof Network)
        {
            return cloneNetwork((Network) instance);
        }
        if (instance instanceof NetworkCluster)
        {
            return cloneNetworkCluster((NetworkCluster) instance);
        }
        if (instance instanceof VmPool)
        {
            return cloneVmPool((VmPool) instance);
        }
        if (instance instanceof StorageDomainStatic)
        {
            return cloneStorageDomainStatic((StorageDomainStatic) instance);
        }
        if (instance instanceof VmTemplate)
        {
            return cloneVmTemplate((VmTemplate) instance);
        }
        if (instance instanceof VmNetworkInterface)
        {
            return cloneVmNetworkInterface((VmNetworkInterface) instance);
        }
        if (instance instanceof VdsNetworkInterface)
        {
            return cloneVdsNetworkInterface((VdsNetworkInterface) instance);
        }
        if (instance instanceof VmStatic)
        {
            return cloneVmStatic((VmStatic) instance);
        }
        if (instance instanceof Version)
        {
            return cloneVersion((Version) instance);
        }

        // Throw exception to determine development needs.
        throw new NotImplementedException();
    }

    private static Object cloneVM(VM instance)
    {
        if (instance == null)
        {
            return null;
        }

        VM vm = new VM();

        vm.setAcpiEnable(instance.getAcpiEnable());
        // TODO: this field is read only in serialization - not sure why it is cloned
        // vm.ActualDiskWithSnapshotsSize = instance.ActualDiskWithSnapshotsSize;
        vm.setAppList(instance.getAppList());
        vm.setAutoStartup(instance.isAutoStartup());
        vm.setBootSequence(instance.getBootSequence());
        vm.setClientIp(instance.getClientIp());
        vm.setCpuPerSocket(instance.getCpuPerSocket());
        vm.setCpuSys(instance.getCpuSys());
        vm.setCpuUser(instance.getCpuUser());
        vm.setDedicatedVmForVds(instance.getDedicatedVmForVds());
        vm.setDefaultBootSequence(instance.getDefaultBootSequence());
        vm.setDefaultDisplayType(instance.getDefaultDisplayType());
        // TODO: 1. DiskList is an array - CopyTo should be considered (if it can be converted to java, otherwise a
        // simple loop is needed)
        // TODO: 2. it is also read only in serialization, so not sure why it is cloned. it is manipulated via
        // addDriveToImageMap
        // vm.DiskList = instance.DiskList;
        vm.setDiskSize(instance.getDiskSize());
        // TODO: this is also an object, so needs to be cloned as well. while it is only accessed via VM.DiskMap, which
        // creates a dictionary
        // from it - actually the DiskImage's themselves are probably sharing the same reference...
        vm.setDisplay(instance.getDisplay());
        vm.setDisplayIp(instance.getDisplayIp());
        vm.setDisplaySecurePort(instance.getDisplaySecurePort());
        vm.setDisplayType(instance.getDisplayType());
        vm.setElapsedTime(instance.getElapsedTime());
        vm.setRoundedElapsedTime(instance.getRoundedElapsedTime());
        vm.setExitMessage(instance.getExitMessage());
        vm.setExitStatus(instance.getExitStatus());
        vm.setFailBack(instance.isFailBack());
        vm.setConsoleCurrentUserName(instance.getConsoleCurentUserName());
        vm.setGuestCurrentUserName(instance.getGuestCurentUserName());
        vm.setConsoleUserId(instance.getConsoleUserId());
        vm.setGuestLastLoginTime(instance.getGuestLastLoginTime());
        vm.setGuestLastLogoutTime(instance.getGuestLastLogoutTime());
        vm.setGuestOs(instance.getGuestOs());
        vm.setGuestRequestedMemory(instance.getGuestRequestedMemory());
        // TODO: Object, should be "cloned" (probably easiest via new Version(instance.GuestAgentVersion.ToString())
        // pay attention NOT to use lower case version in UICommon code.
        vm.setGuestAgentVersion(instance.getGuestAgentVersion());
        vm.setInitrdUrl(instance.getInitrdUrl());
        // TODO: array - need to consider cloning of array, and of actual interfaces
        vm.setInterfaces(instance.getInterfaces());
        vm.setAutoSuspend(instance.isAutoSuspend());
        vm.setInitialized(instance.isInitialized());
        vm.setStateless(instance.isStateless());
        vm.setRunAndPause(instance.isRunAndPause());
        vm.setIsoPath(instance.getIsoPath());
        vm.setKernelParams(instance.getKernelParams());
        vm.setKernelUrl(instance.getKernelUrl());
        vm.setKvmEnable(instance.getKvmEnable());
        // TODO: Guid/NGuid is an object, but code should treat it as immutable, and not change it's uuid directly.
        // (quick skim of code shows this should be safe with current code)
        vm.setLastVdsRunOn(instance.getLastVdsRunOn());
        vm.setMigratingToVds(instance.getMigratingToVds());
        vm.setMigrationSupport(instance.getMigrationSupport());
        vm.setNiceLevel(instance.getNiceLevel());
        vm.setUseHostCpuFlags(instance.isUseHostCpuFlags());
        // TODO: this is readonly in java, since it is computed.
        // options: use calculation here in cloner, or still wrap this in VM instead of serializing it
        // vm.num_of_cpus = instance.num_of_cpus;
        vm.setNumOfMonitors(instance.getNumOfMonitors());
        vm.setAllowConsoleReconnect(instance.getAllowConsoleReconnect());
        vm.setNumOfSockets(instance.getNumOfSockets());
        vm.setOrigin(instance.getOrigin());
        vm.setVmPauseStatus(instance.getVmPauseStatus());
        vm.setPriority(instance.getPriority());
        vm.setRunOnVds(instance.getRunOnVds());
        vm.setRunOnVdsName(instance.getRunOnVdsName());
        vm.setSession(instance.getSession());
        // TODO: see version comment above
        vm.setSpiceDriverVersion(instance.getSpiceDriverVersion());
        vm.setStatus(instance.getStatus());
        vm.setStoragePoolId(instance.getStoragePoolId());
        vm.setStoragePoolName(instance.getStoragePoolName());
        vm.setTimeZone(instance.getTimeZone());
        vm.setTransparentHugePages(instance.isTransparentHugePages());
        vm.setUsageCpuPercent(instance.getUsageCpuPercent());
        vm.setUsageMemPercent(instance.getUsageMemPercent());
        vm.setUsageNetworkPercent(instance.getUsageNetworkPercent());
        vm.setUsbPolicy(instance.getUsbPolicy());
        vm.setUtcDiff(instance.getUtcDiff());
        vm.setVdsGroupCompatibilityVersion(instance.getVdsGroupCompatibilityVersion());
        vm.setVdsGroupId(instance.getVdsGroupId());
        vm.setVdsGroupName(instance.getVdsGroupName());
        vm.setVmCreationDate(instance.getVmCreationDate());
        vm.setVmDescription(instance.getVmDescription());
        vm.setVmDomain(instance.getVmDomain());
        vm.setId(instance.getId());
        vm.setVmHost(instance.getVmHost());
        vm.setVmIp(instance.getVmIp());
        vm.setLastStartTime(instance.getLastStartTime());
        vm.setVmMemSizeMb(instance.getVmMemSizeMb());
        vm.setName(instance.getName());
        vm.setVmOs(instance.getVmOs());
        vm.setVmPid(instance.getVmPid());
        vm.setVmType(instance.getVmType());
        vm.setVmPoolId(instance.getVmPoolId());
        vm.setVmPoolName(instance.getVmPoolName());
        vm.setVmtGuid(instance.getVmtGuid());
        vm.setVmtName(instance.getVmtName());

        return vm;
    }

    private static Object cloneVersion(Version instance)
    {
        return new Version(instance.toString());
    }

    private static Object cloneVDS(VDS instance)
    {
        VDS obj = new VDS();

        obj.setHostName(instance.getHostName());
        obj.setSSHKeyFingerprint(instance.getSSHKeyFingerprint());

        obj.setManagementIp(instance.getManagementIp());
        obj.setPmUser(instance.getPmUser());
        obj.setPmPassword(instance.getPmPassword());
        obj.setPmType(instance.getPmType());
        obj.setPmOptionsMap(instance.getPmOptionsMap());

        obj.setPmSecondaryIp(instance.getManagementIp());
        obj.setPmSecondaryUser(instance.getPmUser());
        obj.setPmSecondaryPassword(instance.getPmPassword());
        obj.setPmSecondaryType(instance.getPmType());
        obj.setPmSecondaryOptionsMap(instance.getPmOptionsMap());

        obj.setpm_enabled(instance.getpm_enabled());
        obj.setPmSecondaryConcurrent(instance.isPmSecondaryConcurrent());

        obj.setPort(instance.getPort());
        obj.setServerSslEnabled(instance.isServerSslEnabled());
        obj.setVdsGroupId(instance.getVdsGroupId());
        obj.setId(instance.getId());
        obj.setVdsName(instance.getName());
        obj.setVdsStrength(instance.getVdsStrength());
        obj.setVdsType(instance.getVdsType());
        obj.setUniqueId(instance.getUniqueId());
        obj.setVdsSpmPriority(instance.getVdsSpmPriority());

        return obj;
    }

    private static StoragePool cloneStorage_pool(StoragePool instance)
    {
        StoragePool obj = new StoragePool();

        obj.setdescription(instance.getdescription());
        obj.setId(instance.getId());
        obj.setname(instance.getname());
        obj.setstorage_pool_type(instance.getstorage_pool_type());
        obj.setstatus(instance.getstatus());

        obj.setmaster_domain_version(instance.getmaster_domain_version());
        obj.setLVER(instance.getLVER());
        obj.setrecovery_mode(instance.getrecovery_mode());
        obj.setspm_vds_id(instance.getspm_vds_id());
        obj.setcompatibility_version(instance.getcompatibility_version());

        return obj;
    }

    private static VDSGroup cloneVDSGroup(VDSGroup instance)
    {
        VDSGroup obj = new VDSGroup();
        obj.setId(instance.getId());
        obj.setname(instance.getname());
        obj.setdescription(instance.getdescription());
        obj.setcpu_name(instance.getcpu_name());

        obj.setselection_algorithm(instance.getselection_algorithm());
        obj.sethigh_utilization(instance.gethigh_utilization());
        obj.setlow_utilization(instance.getlow_utilization());
        obj.setcpu_over_commit_duration_minutes(instance.getcpu_over_commit_duration_minutes());
        obj.setcompatibility_version(instance.getcompatibility_version());
        obj.setMigrateOnError(instance.getMigrateOnError());
        obj.setTransparentHugepages(instance.getTransparentHugepages());

        obj.setStoragePoolId(instance.getStoragePoolId());
        obj.setmax_vds_memory_over_commit(instance.getmax_vds_memory_over_commit());
        obj.setCountThreadsAsCores(instance.getCountThreadsAsCores());

        return obj;
    }

    private static Network cloneNetwork(Network instance)
    {
        Network obj = new Network();

        obj.setAddr(instance.getAddr());
        obj.setDescription(instance.getDescription());
        obj.setId(instance.getId());
        obj.setName(instance.getName());
        obj.setSubnet(instance.getSubnet());
        obj.setGateway(instance.getGateway());
        obj.setType(instance.getType());
        obj.setVlanId(instance.getVlanId());
        obj.setStp(instance.getStp());
        obj.setDataCenterId(instance.getDataCenterId());
        obj.setMtu(instance.getMtu());
        obj.setVmNetwork(instance.isVmNetwork());
        if (instance.getCluster() !=null){
            obj.setCluster(cloneNetworkCluster(instance.getCluster()));
        }
        return obj;
    }

    private static NetworkCluster cloneNetworkCluster(NetworkCluster instance)
    {
        NetworkCluster obj = new NetworkCluster();

        obj.setStatus(instance.getStatus());
        obj.setDisplay(instance.isDisplay());
        obj.setRequired(instance.isRequired());
        obj.setMigration(instance.isMigration());
        return obj;
    }

    private static VmPool cloneVmPool(VmPool instance)
    {
        VmPool obj = new VmPool();

        obj.setVmPoolDescription(instance.getVmPoolDescription());
        obj.setVmPoolId(instance.getVmPoolId());
        obj.setName(instance.getName());
        obj.setVmPoolType(instance.getVmPoolType());
        obj.setVdsGroupId(instance.getVdsGroupId());

        obj.setVmPoolType(instance.getVmPoolType());
        obj.setParameters(instance.getParameters());
        obj.setDefaultEndTime(instance.getDefaultEndTime());
        obj.setDefaultStartTime(instance.getDefaultStartTime());
        obj.setDefaultTimeInDays(instance.getDefaultTimeInDays());
        obj.setVdsGroupName(instance.getVdsGroupName());
        obj.setAssignedVmsCount(instance.getAssignedVmsCount());
        obj.setVmPoolDescription(instance.getVmPoolDescription());
        obj.setRunningVmsCount(instance.getRunningVmsCount());
        obj.setPrestartedVms(instance.getPrestartedVms());

        return obj;
    }

    private static StorageDomainStatic cloneStorageDomainStatic(StorageDomainStatic instance)
    {
        StorageDomainStatic obj = new StorageDomainStatic();
        obj.setConnection(instance.getConnection());
        obj.setId(instance.getId());
        obj.setStorage(instance.getStorage());
        obj.setStorageDomainType(instance.getStorageDomainType());
        obj.setStorageType(instance.getStorageType());
        obj.setStorageName(instance.getStorageName());
        obj.setStorageFormat(instance.getStorageFormat());

        return obj;
    }

    private static VmTemplate cloneVmTemplate(VmTemplate instance)
    {
        VmTemplate obj = new VmTemplate();
        obj.setStoragePoolId(instance.getStoragePoolId());
        obj.setStoragePoolName(instance.getStoragePoolName());
        obj.setDefaultDisplayType(instance.getDefaultDisplayType());
        obj.setPriority(instance.getPriority());
        obj.setIsoPath(instance.getIsoPath());
        obj.setOrigin(instance.getOrigin());
        obj.setSizeGB(instance.getSizeGB());
        // TODO: see comments above on DiskImageMap
        obj.setDiskImageMap(instance.getDiskImageMap());
        obj.setInterfaces(instance.getInterfaces());
        obj.setAutoStartup(instance.isAutoStartup());
        obj.setChildCount(instance.getChildCount());
        obj.setCpuPerSocket(instance.getCpuPerSocket());
        obj.setCreationDate(instance.getCreationDate());
        obj.setDefaultBootSequence(instance.getDefaultBootSequence());
        obj.setDescription(instance.getDescription());
        obj.setDomain(instance.getDomain());
        obj.setFailBack(instance.isFailBack());
        obj.setAutoSuspend(instance.isAutoSuspend());
        obj.setStateless(instance.isStateless());
        obj.setMemSizeMb(instance.getMemSizeMb());
        obj.setName(instance.getName());
        obj.setNiceLevel(instance.getNiceLevel());
        obj.setNumOfMonitors(instance.getNumOfMonitors());
        obj.setAllowConsoleReconnect(instance.isAllowConsoleReconnect());
        obj.setNumOfSockets(instance.getNumOfSockets());
        obj.setStatus(instance.getStatus());
        obj.setTimeZone(instance.getTimeZone());
        obj.setUsbPolicy(instance.getUsbPolicy());
        obj.setVdsGroupId(instance.getVdsGroupId());
        obj.setVdsGroupName(instance.getVdsGroupName());
        obj.setVmType(instance.getVmType());
        obj.setId(instance.getId());
        obj.setDiskList(instance.getDiskList());
        obj.setRunAndPause(instance.isRunAndPause());

        return obj;
    }

    private static VmStatic cloneVmStatic(VmStatic instance)
    {
        VmStatic obj = new VmStatic();

        obj.setFailBack(instance.isFailBack());
        obj.setDefaultBootSequence(instance.getDefaultBootSequence());
        obj.setVmType(instance.getVmType());
        obj.setDefaultDisplayType(instance.getDefaultDisplayType());
        obj.setPriority(instance.getPriority());
        obj.setIsoPath(instance.getIsoPath());
        obj.setOrigin(instance.getOrigin());
        obj.setAutoStartup(instance.isAutoStartup());
        obj.setCpuPerSocket(instance.getCpuPerSocket());
        obj.setCreationDate(instance.getCreationDate());
        obj.setDedicatedVmForVds(instance.getDedicatedVmForVds());
        obj.setDescription(instance.getDescription());
        obj.setDomain(instance.getDomain());
        obj.setAutoSuspend(instance.isAutoSuspend());
        obj.setInitialized(instance.isInitialized());
        obj.setStateless(instance.isStateless());
        obj.setRunAndPause(instance.isRunAndPause());
        obj.setMemSizeMb(instance.getMemSizeMb());
        obj.setDiskSize(instance.getDiskSize());
        obj.setNiceLevel(instance.getNiceLevel());
        obj.setNumOfMonitors(instance.getNumOfMonitors());
        obj.setAllowConsoleReconnect(instance.isAllowConsoleReconnect());
        obj.setNumOfSockets(instance.getNumOfSockets());
        obj.setTimeZone(instance.getTimeZone());
        obj.setUsbPolicy(instance.getUsbPolicy());
        obj.setVdsGroupId(instance.getVdsGroupId());
        obj.setId(instance.getId());
        obj.setName(instance.getName());
        obj.setVmtGuid(instance.getVmtGuid());

        return obj;
    }

    private static void cloneNetworkStatisticss(NetworkStatistics instance, NetworkStatistics obj)
    {
        obj.setId(instance.getId());
        obj.setReceiveDropRate(instance.getReceiveDropRate());
        obj.setReceiveRate(instance.getReceiveRate());
        obj.setTransmitDropRate(instance.getTransmitDropRate());
        obj.setTransmitRate(instance.getTransmitRate());
        obj.setStatus(instance.getStatus());
    }

    private static VdsNetworkStatistics cloneVdsNetworkStatistics(VdsNetworkStatistics instance)
    {
        VdsNetworkStatistics obj = new VdsNetworkStatistics();

        cloneNetworkStatisticss(instance, obj);
        obj.setVdsId(instance.getVdsId());

        return obj;
    }

    private static Object cloneVdsNetworkInterface(VdsNetworkInterface vdsNetworkInterface)
    {
        VdsNetworkInterface obj = new VdsNetworkInterface();

        obj.setAddress(vdsNetworkInterface.getAddress());
        obj.setBonded(vdsNetworkInterface.getBonded());
        obj.setBondName(vdsNetworkInterface.getBondName());
        obj.setBondOptions(vdsNetworkInterface.getBondOptions());
        obj.setBondType(vdsNetworkInterface.getBondType());
        obj.setBootProtocol(vdsNetworkInterface.getBootProtocol());
        obj.setGateway(vdsNetworkInterface.getGateway());
        obj.setId(vdsNetworkInterface.getId());
        obj.setMacAddress(vdsNetworkInterface.getMacAddress());
        obj.setName(vdsNetworkInterface.getName());
        obj.setNetworkName(vdsNetworkInterface.getNetworkName());
        obj.setSpeed(vdsNetworkInterface.getSpeed());
        obj.setStatistics(cloneVdsNetworkStatistics(vdsNetworkInterface.getStatistics()));

        return obj;
    }

    private static VmNetworkStatistics cloneVmNetworkStatistics(VmNetworkStatistics instance)
    {
        VmNetworkStatistics obj = new VmNetworkStatistics();

        cloneNetworkStatisticss(instance, obj);
        obj.setVmId(instance.getVmId());

        return obj;
    }

    private static Object cloneVmNetworkInterface(VmNetworkInterface vmNetworkInterface)
    {
        VmNetworkInterface obj = new VmNetworkInterface();
        obj.setId(vmNetworkInterface.getId());
        obj.setMacAddress(vmNetworkInterface.getMacAddress());
        obj.setName(vmNetworkInterface.getName());
        obj.setNetworkName(vmNetworkInterface.getNetworkName());
        obj.setLinked(vmNetworkInterface.isLinked());
        obj.setSpeed(vmNetworkInterface.getSpeed());
        obj.setType(vmNetworkInterface.getType());
        obj.setVmId(vmNetworkInterface.getVmId());
        obj.setVmName(vmNetworkInterface.getVmName());
        obj.setVmTemplateId(vmNetworkInterface.getVmTemplateId());
        obj.setStatistics(cloneVmNetworkStatistics(vmNetworkInterface.getStatistics()));

        return obj;
    }
}
