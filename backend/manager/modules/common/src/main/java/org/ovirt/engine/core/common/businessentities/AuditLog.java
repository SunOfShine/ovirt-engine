package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Date;

import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.NGuid;

public class AuditLog extends IVdcQueryable implements Serializable {
    private static final long serialVersionUID = -2808392095455280186L;

    private long auditLogId;
    private Date logTime;
    private String message;
    private NGuid userId;
    private String userName;
    private NGuid quotaId;
    private String quotaName;
    private NGuid vdsId;
    private String vdsName;
    private NGuid vmTemplateId;
    private String vmTemplateName;
    private NGuid vmId;
    private String vmName;
    private NGuid storagePoolId;
    private String storagePoolName;
    private NGuid storageDomainId;
    private String storageDomainName;
    private NGuid vdsGroupId;
    private String vdsGroupName;
    private int logType = AuditLogType.UNASSIGNED.getValue();
    private int severity = AuditLogSeverity.NORMAL.getValue();
    private boolean processed = false;
    private String correlationId;
    private NGuid jobId;
    private NGuid glusterVolumeId;
    private String glusterVolumeName;
    private String origin = "oVirt";
    private int customEventId = -1;
    private int eventFloodInSec = 30;
    private String customData = "";
    private boolean external = false;
    private boolean deleted = false;
    private String storagePoolType;
    private String compatibilityVersion;
    private String quotaEnforcementType;

    public AuditLog() {
    }

    public AuditLog(AuditLogType al_type, AuditLogSeverity al_severity, String al_msg, NGuid al_user_id,
            String al_user_name, NGuid al_vm_id, String al_vm_name, NGuid al_vds_id, String al_vds_name,
            NGuid al_vmt_id, String al_vmt_name) {
        logTime = new Date();
        logType = al_type.getValue();
        severity = al_severity.getValue();
        message = al_msg;
        userId = al_user_id;
        userName = al_user_name;
        vmId = al_vm_id;
        vmName = al_vm_name;
        vdsId = al_vds_id;
        vdsName = al_vds_name;
        vmTemplateId = al_vmt_id;
        vmTemplateName = al_vmt_name;
    }

    public AuditLog(AuditLogType al_type,
            AuditLogSeverity al_severity,
            String al_msg,
            NGuid al_user_id,
            String al_user_name,
            NGuid al_vm_id,
            String al_vm_name,
            NGuid al_vds_id,
            String al_vds_name,
            NGuid al_vmt_id,
            String al_vmt_name,
            String origin,
            int customEventId,
            int eventFloogInSec,
            String customData) {
        logTime = new Date();
        logType = al_type.getValue();
        severity = al_severity.getValue();
        message = al_msg;
        userId = al_user_id;
        userName = al_user_name;
        vmId = al_vm_id;
        vmName = al_vm_name;
        vdsId = al_vds_id;
        vdsName = al_vds_name;
        vmTemplateId = al_vmt_id;
        vmTemplateName = al_vmt_name;
        this.origin = origin;
        this.customEventId = customEventId;
        this.eventFloodInSec = eventFloogInSec;
        this.customData = customData;
    }
    public long getaudit_log_id() {
        return this.auditLogId;
    }

    public void setaudit_log_id(long value) {
        this.auditLogId = value;
    }

    public java.util.Date getlog_time() {
        return this.logTime;
    }

    public void setlog_time(java.util.Date value) {
        this.logTime = value;
    }

    public String getmessage() {
        return this.message;
    }

    public void setmessage(String value) {
        this.message = value;
    }

    public NGuid getuser_id() {
        return this.userId;
    }

    public void setuser_id(NGuid value) {
        this.userId = value;
    }

    public String getuser_name() {
        return this.userName;
    }

    public void setuser_name(String value) {
        this.userName = value;
    }

    public NGuid getQuotaId() {
        return this.quotaId;
    }

    public void setQuotaId(NGuid value) {
        this.quotaId = value;
    }

    public String getQuotaName() {
        return this.quotaName;
    }

    public void setQuotaName(String value) {
        this.quotaName = value;
    }

    public NGuid getvds_id() {
        return this.vdsId;
    }

    public void setvds_id(NGuid value) {
        this.vdsId = value;
    }

    public String getvds_name() {
        return this.vdsName;
    }

    public void setvds_name(String value) {
        this.vdsName = value;
    }

    public NGuid getvm_template_id() {
        return this.vmTemplateId;
    }

    public void setvm_template_id(NGuid value) {
        this.vmTemplateId = value;
    }

    public String getvm_template_name() {
        return this.vmTemplateName;
    }

    public void setvm_template_name(String value) {
        this.vmTemplateName = value;
    }

    public NGuid getvm_id() {
        return this.vmId;
    }

    public void setvm_id(NGuid value) {
        this.vmId = value;
    }

    public String getvm_name() {
        return this.vmName;
    }

    public void setvm_name(String value) {
        this.vmName = value;
    }

    public NGuid getstorage_pool_id() {
        return storagePoolId;
    }

    public void setstorage_pool_id(NGuid value) {
        storagePoolId = value;
    }

    public String getstorage_pool_name() {
        return storagePoolName;
    }

    public void setstorage_pool_name(String value) {
        storagePoolName = value;
    }

    public NGuid getstorage_domain_id() {
        return storageDomainId;
    }

    public void setstorage_domain_id(NGuid value) {
        storageDomainId = value;
    }



    public String getstorage_domain_name() {
        return storageDomainName;
    }

    public void setstorage_domain_name(String value) {
        storageDomainName = value;
    }



    public NGuid getvds_group_id() {
        return vdsGroupId;
    }

    public void setvds_group_id(NGuid value) {
        vdsGroupId = value;
    }

    public String getvds_group_name() {
        return vdsGroupName;
    }

    public void setvds_group_name(String value) {
        vdsGroupName = value;
    }

    public AuditLogType getlog_type() {
        return AuditLogType.forValue(logType);
    }

    public void setlog_type(AuditLogType value) {
        logType = value.getValue();
    }

    // We need log_typeValue for the UI,
    // We dont have the AuditLogType enumeration synchronized,
    // WSDL formatter set the enumeration value according to its string value
    // (enums are strings in WSDL)
    public int getlog_typeValue() {
        return getlog_type().getValue();
    }

    public void setlog_typeValue(int value) {
        // Do nothing, this is mockup (WSDL need setter)
    }

    public String getlog_type_name() {
        return getlog_type().name();
    }

    public AuditLogSeverity getseverity() {
        return AuditLogSeverity.forValue(severity);
    }

    public void setseverity(AuditLogSeverity value) {
        severity = value.getValue();
    }

    @Override
    public Object getQueryableId() {
        return getaudit_log_id();
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setJobId(NGuid jobId) {
        this.jobId = jobId;
    }

    public NGuid getJobId() {
        return jobId;
    }

    public NGuid getGlusterVolumeId() {
        return glusterVolumeId;
    }

    public void setGlusterVolumeId(NGuid value) {
        glusterVolumeId = value;
    }

    public String getGlusterVolumeName() {
        return glusterVolumeName;
    }

    public void setGlusterVolumeName(String value) {
        glusterVolumeName = value;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public int getCustomEventId() {
        return customEventId;
    }

    public void setCustomEventId(int customEventId) {
        this.customEventId = customEventId;
    }

    public int getEventFloodInSec() {
        return eventFloodInSec;
    }

    public void setEventFloodInSec(int eventFloodInSec) {
        this.eventFloodInSec = eventFloodInSec;
    }

    public String getCustomData() {
        return customData;
    }

    public void setCustomData(String customData) {
        this.customData = customData;
    }

    public boolean isExternal() {
        return external;
    }

    public void setExternal(boolean external) {
        this.external = external;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getStoragePoolType() {
        return storagePoolType;
    }

    public void setStoragePoolType(String storagePoolType) {
        this.storagePoolType = storagePoolType;
    }

    public String getCompatibilityVersion() {
        return compatibilityVersion;
    }

    public void setCompatibilityVersion(String compatibilityVersion) {
        this.compatibilityVersion = compatibilityVersion;
    }

    public String getQuotaEnforcementType() {
        return quotaEnforcementType;
    }

    public void setQuotaEnforcementType(String quotaEnforcementType) {
        this.quotaEnforcementType = quotaEnforcementType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (auditLogId ^ (auditLogId >>> 32));
        result = prime * result + ((logTime == null) ? 0 : logTime.hashCode());
        result = prime * result + logType;
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((storageDomainId == null) ? 0 : storageDomainId.hashCode());
        result = prime * result + ((storagePoolId == null) ? 0 : storagePoolId.hashCode());
        result = prime * result + severity;
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        result = prime * result + ((vdsId == null) ? 0 : vdsId.hashCode());
        result = prime * result + ((quotaId == null) ? 0 : quotaId.hashCode());
        result = prime * result + ((vmId == null) ? 0 : vmId.hashCode());
        result = prime * result + ((vmTemplateId == null) ? 0 : vmTemplateId.hashCode());
        result = prime * result + (processed ? 1231 : 1237);
        result = prime * result + ((correlationId == null) ? 0 : correlationId.hashCode());
        result = prime * result + ((jobId == null) ? 0 : jobId.hashCode());
        result = prime * result + ((origin == null) ? 0 : origin.hashCode());
        result = prime * result + customEventId;
        result = prime * result + eventFloodInSec;
        result = prime * result + ((customData == null) ? 0 : customData.hashCode());
        result = prime * result + (external ? 1231 : 1237);
        result = prime * result + (deleted ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AuditLog other = (AuditLog) obj;
        return (auditLogId == other.auditLogId
                && ObjectUtils.objectsEqual(logTime, other.logTime)
                && logType == other.logType
                && ObjectUtils.objectsEqual(message, other.message)
                && ObjectUtils.objectsEqual(storageDomainId, other.storageDomainId)
                && ObjectUtils.objectsEqual(storagePoolId, other.storagePoolId)
                && severity == other.severity
                && ObjectUtils.objectsEqual(userId, other.userId)
                && ObjectUtils.objectsEqual(vdsId, other.vdsId)
                && ObjectUtils.objectsEqual(quotaId, other.quotaId)
                && ObjectUtils.objectsEqual(vmId, other.vmId)
                && ObjectUtils.objectsEqual(vmTemplateId, other.vmTemplateId)
                && processed == other.processed
                && ObjectUtils.objectsEqual(correlationId, other.correlationId)
                && ObjectUtils.objectsEqual(jobId, other.jobId)
                && ObjectUtils.objectsEqual(origin, other.origin)
                && customEventId == other.customEventId
                && eventFloodInSec == other.eventFloodInSec
                && ObjectUtils.objectsEqual(customData, other.customData)
                && external == other.external
                && deleted == other.deleted);
    }
}
