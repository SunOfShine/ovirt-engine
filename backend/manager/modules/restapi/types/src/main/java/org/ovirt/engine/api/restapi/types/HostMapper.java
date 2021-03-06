package org.ovirt.engine.api.restapi.types;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.common.util.StatusUtils;
import org.ovirt.engine.api.model.Agent;
import org.ovirt.engine.api.model.Agents;
import org.ovirt.engine.api.model.CPU;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.CpuTopology;
import org.ovirt.engine.api.model.Display;
import org.ovirt.engine.api.model.Hook;
import org.ovirt.engine.api.model.Hooks;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostStatus;
import org.ovirt.engine.api.model.HostType;
import org.ovirt.engine.api.model.IscsiDetails;
import org.ovirt.engine.api.model.KSM;
import org.ovirt.engine.api.model.OperatingSystem;
import org.ovirt.engine.api.model.Option;
import org.ovirt.engine.api.model.Options;
import org.ovirt.engine.api.model.PmProxies;
import org.ovirt.engine.api.model.PmProxy;
import org.ovirt.engine.api.model.HardwareInformation;
import org.ovirt.engine.api.model.PowerManagement;
import org.ovirt.engine.api.model.StorageManager;
import org.ovirt.engine.api.model.TransparentHugePages;
import org.ovirt.engine.api.model.Version;
import org.ovirt.engine.api.model.VmSummary;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VdsTransparentHugePagesState;
import org.ovirt.engine.core.common.queries.ValueObjectMap;
import org.ovirt.engine.core.common.queries.ValueObjectPair;
import org.ovirt.engine.core.compat.NGuid;

public class HostMapper {

    public static Long BYTES_IN_MEGABYTE = 1024L * 1024L;
    // REVISIT retrieve from configuration
    private static final int DEFAULT_VDSM_PORT = 54321;
    private static final String MD5_FILE_SIGNATURE = "md5";
    private static final String MD5_SECURITY_ALGORITHM = "MD5";

    private static final String HOST_OS_DELEIMITER = " - ";

    @Mapping(from = Host.class, to = VdsStatic.class)
    public static VdsStatic map(Host model, VdsStatic template) {
        VdsStatic entity = template != null ? template : new VdsStatic();
        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetName()) {
            entity.setVdsName(model.getName());
        }
        if (model.isSetCluster() && model.getCluster().isSetId()) {
            entity.setVdsGroupId(GuidUtils.asGuid(model.getCluster().getId()));
        }
        if (model.isSetAddress()) {
            entity.setHostName(model.getAddress());
        }
        if (model.isSetPort() && model.getPort() > 0) {
            entity.setPort(model.getPort());
        } else {
            entity.setPort(DEFAULT_VDSM_PORT);
        }
        if (model.isSetPowerManagement()) {
            entity = map(model.getPowerManagement(), entity);
        }
        if (model.isSetStorageManager()) {
            if (model.getStorageManager().getPriority() != null) {
                entity.setVdsSpmPriority(model.getStorageManager().getPriority());
            }
        }
        if (model.isSetDisplay() && model.getDisplay().isSetAddress()) {
            entity.setConsoleAddress("".equals(model.getDisplay().getAddress()) ? null : model.getDisplay().getAddress());
        }

        return entity;
    }

    @Mapping(from = PowerManagement.class, to = VdsStatic.class)
    public static VdsStatic map(PowerManagement model, VdsStatic template) {
        VdsStatic entity = template != null ? template : new VdsStatic();
        if (model.isSetType()) {
            entity.setPmType(model.getType());
        }
        if (model.isSetEnabled()) {
            entity.setPmEnabled(model.isEnabled());
        }
        if (model.isSetAddress()) {
            entity.setManagementIp(model.getAddress());
        }
        if (model.isSetUsername()) {
            entity.setPmUser(model.getUsername());
        }
        if (model.isSetPassword()) {
            entity.setPmPassword(model.getPassword());
        }
        if (model.isSetOptions()) {
            entity.setPmOptions(map(model.getOptions(), null));
        }
        if (model.isSetPmProxies()) {
            String delim = "";
            StringBuilder builder = new StringBuilder();
            for (PmProxy pmProxy : model.getPmProxies().getPmProxy()) {
                builder.append(delim);
                builder.append(pmProxy.getType());
                delim = ",";
            }
            entity.setPmProxyPreferences(builder.toString());
        }
        if (model.isSetAgents()) {
            // Currently only Primary/Secondary agents are supported
            int order = 1;
            for (Agent agent : model.getAgents().getAgents()) {

                if (agent.isSetOrder()) {
                    order = agent.getOrder();
                }
                if (order == 1) { // Primary
                    order++; // in case that order is not defined, secondary will still be defined correctly.
                    if (agent.isSetType()) {
                        entity.setPmType(agent.getType());
                    }
                    if (agent.isSetAddress()) {
                        entity.setManagementIp(agent.getAddress());
                    }
                    if (agent.isSetUsername()) {
                        entity.setPmUser(agent.getUsername());
                    }
                    if (agent.isSetPassword()) {
                        entity.setPmPassword(agent.getPassword());
                    }
                    if (agent.isSetOptions()) {
                        entity.setPmOptions(map(agent.getOptions(), null));
                    }
                }
                else if (order == 2) { // Secondary
                    if (agent.isSetType()) {
                        entity.setPmSecondaryType(agent.getType());
                    }
                    if (agent.isSetAddress()) {
                        entity.setPmSecondaryIp(agent.getAddress());
                    }
                    if (agent.isSetUsername()) {
                        entity.setPmSecondaryUser(agent.getUsername());
                    }
                    if (agent.isSetPassword()) {
                        entity.setPmSecondaryPassword(agent.getPassword());
                    }
                    if (agent.isSetOptions()) {
                        entity.setPmSecondaryOptions(map(agent.getOptions(), null));
                    }
                    if (agent.isSetConcurrent()) {
                        entity.setPmSecondaryConcurrent(agent.isConcurrent());
                    }
                }
            }
        }
        return entity;
    }

    @Mapping(from = Options.class, to = String.class)
    public static String map(Options model, String template) {
        StringBuilder buf = template != null ? new StringBuilder(template) : new StringBuilder();
        for (Option option : model.getOptions()) {
            String opt = map(option, null);
            if (opt != null) {
                if (buf.length() > 0) {
                    buf.append(",");
                }
                buf.append(opt);
            }
        }
        return buf.toString();
    }

    @Mapping(from = Option.class, to = String.class)
    public static String map(Option model, String template) {
        if (model.isSetName() && (!model.getName().isEmpty()) && model.isSetValue() && (!model.getValue().isEmpty())) {
            return model.getName() + "=" + model.getValue();
        } else {
            return template;
        }
    }

    @Mapping(from = VDS.class, to = Host.class)
    public static Host map(VDS entity, Host template) {
        Host model = template != null ? template : new Host();
        model.setId(entity.getId().toString());
        model.setName(entity.getName());
        if (entity.getVdsGroupId() != null) {
            Cluster cluster = new Cluster();
            cluster.setId(entity.getVdsGroupId().toString());
            model.setCluster(cluster);
        }
        model.setAddress(entity.getHostName());
        if (entity.getPort() > 0) {
            model.setPort(entity.getPort());
        }
        HostStatus status = map(entity.getStatus(), null);
        model.setStatus(StatusUtils.create(status));
        if (status==HostStatus.NON_OPERATIONAL) {
            model.getStatus().setDetail(entity.getNonOperationalReason().name().toLowerCase());
        }
        StorageManager sm = new StorageManager();
        sm.setPriority(entity.getVdsSpmPriority());
        sm.setValue(entity.getSpmStatus() == VdsSpmStatus.SPM);
        model.setStorageManager(sm);
        if (entity.getVersion() != null &&
                entity.getVersion().getMajor() != -1 &&
                entity.getVersion().getMinor() != -1 &&
                entity.getVersion().getRevision() != -1 &&
                entity.getVersion().getBuild() != -1) {
            Version version = new Version();
            version.setMajor(entity.getVersion().getMajor());
            version.setMinor(entity.getVersion().getMinor());
            version.setRevision(entity.getVersion().getRevision());
            version.setBuild(entity.getVersion().getBuild());
            version.setFullVersion(entity.getVersion().getRpmName());
            model.setVersion(version);
        }
        model.setOs(getHostOs(entity.getHostOs()));
        model.setKsm(new KSM());
        model.getKsm().setEnabled(Boolean.TRUE.equals(entity.getKsmState()));
        model.setTransparentHugepages(new TransparentHugePages());
        model.getTransparentHugepages().setEnabled(!(entity.getTransparentHugePagesState() == null ||
                entity.getTransparentHugePagesState() == VdsTransparentHugePagesState.Never));
        if (entity.getIScsiInitiatorName() != null) {
            model.setIscsi(new IscsiDetails());
            model.getIscsi().setInitiator(entity.getIScsiInitiatorName());
        }
        model.setPowerManagement(map(entity, (PowerManagement)null));
        model.setHardwareInformation(map(entity, (HardwareInformation)null));
        CPU cpu = new CPU();
        CpuTopology cpuTopology = new CpuTopology();
        if (entity.getCpuSockets() != null) {
            cpuTopology.setSockets(entity.getCpuSockets());
            if (entity.getCpuCores()!=null) {
                cpuTopology.setCores(entity.getCpuCores()/entity.getCpuSockets());
                if (entity.getCpuThreads() != null) {
                    cpuTopology.setThreads(entity.getCpuThreads()/entity.getCpuCores());
                }
            }
        }
        cpu.setTopology(cpuTopology);
        cpu.setName(entity.getCpuModel());
        if (entity.getCpuSpeedMh()!=null) {
            cpu.setSpeed(new BigDecimal(entity.getCpuSpeedMh()));
        }
        model.setCpu(cpu);
        VmSummary vmSummary = new VmSummary();
        vmSummary.setActive(entity.getVmActive());
        vmSummary.setMigrating(entity.getVmMigrating());
        vmSummary.setTotal(entity.getVmCount());
        model.setSummary(vmSummary);
        if (entity.getVdsType() != null) {
            HostType type = map(entity.getVdsType(), null);
            model.setType(type != null ? type.value() : null);
        }
        model.setMemory(Long.valueOf(entity.getPhysicalMemMb() == null ? 0 : entity.getPhysicalMemMb()
                * BYTES_IN_MEGABYTE));
        model.setMaxSchedulingMemory((int) entity.getMaxSchedulingMemory() * BYTES_IN_MEGABYTE);

        if (entity.getLibvirtVersion() != null &&
                entity.getLibvirtVersion().getMajor() != -1 &&
                entity.getLibvirtVersion().getMinor() != -1 &&
                entity.getLibvirtVersion().getRevision() != -1 &&
                entity.getLibvirtVersion().getBuild() != -1) {
            Version version = new Version();
            version.setMajor(entity.getLibvirtVersion().getMajor());
            version.setMinor(entity.getLibvirtVersion().getMinor());
            version.setRevision(entity.getLibvirtVersion().getRevision());
            version.setBuild(entity.getLibvirtVersion().getBuild());
            version.setFullVersion(entity.getLibvirtVersion().getRpmName());
            model.setLibvirtVersion(version);
        }

        if (entity.getConsoleAddress() != null && !"".equals(entity.getConsoleAddress())) {
            model.setDisplay(new Display());
            model.getDisplay().setAddress(entity.getConsoleAddress());
        }

        return model;
    }

    private static OperatingSystem getHostOs(String hostOs) {
        if (hostOs == null || hostOs.trim().length() == 0) {
            return null;
        }
        String[] hostOsInfo = hostOs.split(HOST_OS_DELEIMITER);
        Version version = new Version();
        version.setMajor(getIntegerValue(hostOsInfo, 1));
        version.setMinor(getIntegerValue(hostOsInfo, 2));
        version.setFullVersion(getFullHostOsVersion(hostOsInfo));
        OperatingSystem os = new OperatingSystem();
        os.setType(hostOsInfo[0]);
        os.setVersion(version);
        return os;
    }

    private static Integer getIntegerValue(String[] hostOsInfo, int indx) {
        if (hostOsInfo.length <= indx) {
            return null;
        }
        try {
            return Integer.valueOf(hostOsInfo[indx]);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String getFullHostOsVersion(String[] hostOsInfo) {
        StringBuilder buf = new StringBuilder("");
        for(int i = 1; i < hostOsInfo.length; i++) {
            if(i > 1) {
                buf.append(HOST_OS_DELEIMITER);
            }
            buf.append(hostOsInfo[i]);
        }
        return buf.toString();
    }

    @Mapping(from = VDS.class, to = HardwareInformation.class)
    public static HardwareInformation map(VDS entity, HardwareInformation template) {
        HardwareInformation model = template != null ? template : new HardwareInformation();
        model.setManufacturer(entity.getHardwareManufacturer());
        model.setFamily(entity.getHardwareFamily());
        model.setProductName(entity.getHardwareProductName());
        model.setSerialNumber(entity.getHardwareSerialNumber());
        model.setUuid(entity.getHardwareUUID());
        model.setVersion(entity.getHardwareVersion());
        return model;
    }

    @Mapping(from = VDS.class, to = PowerManagement.class)
    public static PowerManagement map(VDS entity, PowerManagement template) {
        PowerManagement model = template != null ? template : new PowerManagement();
        model.setType(entity.getPmType());
        model.setEnabled(entity.getpm_enabled());
        model.setAddress(entity.getManagementIp());
        model.setUsername(entity.getPmUser());
        if (entity.getPmOptionsMap() != null) {
            model.setOptions(map(entity.getPmOptionsMap(), null));
        }
        if (entity.getPmProxyPreferences() != null) {
            PmProxies pmProxies = new PmProxies();
                String[] proxies = StringUtils.split(entity.getPmProxyPreferences(), ",");
                for (String proxy : proxies) {
                        PmProxy pmProxy = new PmProxy();
                pmProxy.setType(proxy);
                        pmProxies.getPmProxy().add(pmProxy);
                }
            model.setPmProxies(pmProxies);
        }
        if (entity.getpm_enabled()) {
            // Set Primary Agent
            Agent agent = new Agent();
            if (!StringUtils.isEmpty(entity.getManagementIp())) {
                agent.setType(entity.getPmType());
                agent.setAddress(entity.getManagementIp());
                agent.setUsername(entity.getPmUser());
                if (entity.getPmOptionsMap() != null) {
                    agent.setOptions(map(entity.getPmOptionsMap(), null));
                }
                agent.setOrder(1);
                model.setAgents(new Agents());
                model.getAgents().getAgents().add(agent);

            }
            // Set Secondary Agent
            if (!StringUtils.isEmpty(entity.getPmSecondaryIp())) {
                agent = new Agent();
                agent.setType(entity.getPmSecondaryType());
                agent.setAddress(entity.getPmSecondaryIp());
                agent.setUsername(entity.getPmSecondaryUser());
                if (entity.getPmOptionsMap() != null) {
                    agent.setOptions(map(entity.getPmSecondaryOptionsMap(), null));
                }
                agent.setOrder(2);
                agent.setConcurrent(entity.isPmSecondaryConcurrent());
                model.getAgents().getAgents().add(agent);
            }
        }
        return model;
    }

    @Mapping(from = ValueObjectMap.class, to = Options.class)
    public static Options map(ValueObjectMap entity, Options template) {
        Options model = template != null ? template : new Options();
        for (ValueObjectPair option : entity.getValuePairs()) {
            model.getOptions().add(map(option, null));
        }
        return model;
    }

    @Mapping(from = ValueObjectPair.class, to = Option.class)
    public static Option map(ValueObjectPair entity, Option template) {
        Option model = template != null ? template : new Option();
        model.setName((String)entity.getKey());
        model.setValue((String)entity.getValue());
        return model;
    }

    @Mapping(from = VDSStatus.class, to = HostStatus.class)
    public static HostStatus map(VDSStatus entityStatus, HostStatus template) {
        switch (entityStatus) {
        case Unassigned:
            return HostStatus.UNASSIGNED;
        case Down:
            return HostStatus.DOWN;
        case Maintenance:
            return HostStatus.MAINTENANCE;
        case Up:
            return HostStatus.UP;
        case NonResponsive:
            return HostStatus.NON_RESPONSIVE;
        case Error:
            return HostStatus.ERROR;
        case Installing:
            return HostStatus.INSTALLING;
        case InstallFailed:
            return HostStatus.INSTALL_FAILED;
        case Reboot:
            return HostStatus.REBOOT;
        case PreparingForMaintenance:
            return HostStatus.PREPARING_FOR_MAINTENANCE;
        case NonOperational:
            return HostStatus.NON_OPERATIONAL;
        case PendingApproval:
            return HostStatus.PENDING_APPROVAL;
        case Initializing:
            return HostStatus.INITIALIZING;
        case Connecting:
            return HostStatus.CONNECTING;
        default:
            return null;
        }
    }

    @Mapping(from = VDSType.class, to = HostType.class)
    public static HostType map(VDSType type, HostType template) {
        switch (type) {
        case VDS:
            return HostType.RHEL;
        case oVirtNode:
            return HostType.RHEV_H;
        default:
            return null;
        }
    }

    @Mapping(from = HashMap.class, to = Hooks.class)
    public static Hooks map(HashMap<String, HashMap<String, HashMap<String, String>>> dictionary, Hooks hooks) {
        if (hooks == null) {
            hooks = new Hooks();
        }
        for (Map.Entry<String, HashMap<String, HashMap<String, String>>> keyValuePair : dictionary.entrySet()) { // events
            for (Map.Entry<String, HashMap<String, String>> keyValuePair1 : keyValuePair.getValue() // hooks
                    .entrySet()) {
                Hook hook = createHook(keyValuePair, keyValuePair1);
                hooks.getHooks().add(hook);
            }
        }
        return hooks;
    }

    private static Hook createHook(Map.Entry<String, HashMap<String, HashMap<String, String>>> keyValuePair,
            Map.Entry<String, HashMap<String, String>> keyValuePair1) {
        String hookName = keyValuePair1.getKey();
        String eventName = keyValuePair.getKey();
        String md5 = keyValuePair1.getValue().get(MD5_FILE_SIGNATURE);
        Hook hook = new Hook();
        hook.setName(hookName);
        hook.setEventName(eventName);
        hook.setMd5(md5);
        setHookId(hook, hookName, eventName, md5);
        return hook;
    }

    private static void setHookId(Hook hook, String hookName, String eventName, String md5) {
        NGuid guid = generateHookId(eventName, hookName, md5);
        hook.setId(guid.toString());
    }

    public static NGuid generateHookId(String eventName, String hookName, String md5) {
        String idString = eventName + hookName + md5;
        try {
            byte[] hash = MessageDigest.getInstance(MD5_SECURITY_ALGORITHM).digest(idString.getBytes());
            NGuid guid = new NGuid(hash, true);
            return guid;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e); // never happens, MD5 algorithm exists
        }
    }
}
