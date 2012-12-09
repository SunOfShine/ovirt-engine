package org.ovirt.engine.core.bll.validator;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dal.VdcBllMessages;

/**
 * A class that can validate a {@link VmNetworkInterface} is valid from certain aspects.
 */
public class VmNicValidator {

    private VmNetworkInterface nic;

    private String version;

    public VmNicValidator(VmNetworkInterface nic, String version) {
        this.nic = nic;
        this.version = version;
    }

    /**
     * @return An error if unlinking is not supported and the interface is unlinked, otherwise it's OK.
     */
    public ValidationResult linkedCorrectly() {
        return !networkLinkingSupported(version) && !nic.isLinked()
                ? new ValidationResult(VdcBllMessages.UNLINKING_IS_NOT_SUPPORTED, clusterVersion())
                : ValidationResult.VALID;
    }

    /**
     * @return An error if unlinking is not supported and the network is not set, otherwise it's OK.
     */
    public ValidationResult networkNameValid() {
        return !networkLinkingSupported(version) && nic.getNetworkName() == null
                ? new ValidationResult(VdcBllMessages.NULL_NETWORK_IS_NOT_SUPPORTED, clusterVersion())
                : ValidationResult.VALID;
    }

    public static boolean networkLinkingSupported(String version) {
        return Config.<Boolean> GetValue(ConfigValues.NetworkLinkingSupported, version);
    }

    private String clusterVersion() {
        return String.format("$clusterVersion %s", version);
    }
}