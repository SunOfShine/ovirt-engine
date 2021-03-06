package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class HostHardwareGeneralModel extends EntityModel
{
    private boolean updateUpgradeAlert;

    @Override
    public VDS getEntity()
    {
        return (VDS) super.getEntity();
    }

    @Override
    public void setEntity(Object value)
    {
        VDS vds = (VDS) value;
        updateUpgradeAlert = vds == null || getEntity() == null
            || !vds.getId().equals(getEntity().getId())
            || !vds.getStatus().equals(getEntity().getStatus());

        super.setEntity(value);
    }

    private String hardwareManufacturer;

    public String getHardwareManufacturer()
    {
        return hardwareManufacturer;
    }

    public void setHardwareManufacturer(String value)
    {
        if (!StringHelper.stringsEqual(hardwareManufacturer, value))
        {
            hardwareManufacturer = value;
            onPropertyChanged(new PropertyChangedEventArgs("manufacturer")); //$NON-NLS-1$
        }
    }

    private String hardwareProductName;

    public String getHardwareProductName()
    {
        return hardwareProductName;
    }

    public void setHardwareProductName(String value)
    {
        if (!StringHelper.stringsEqual(hardwareProductName, value))
        {
            hardwareProductName = value;
            onPropertyChanged(new PropertyChangedEventArgs("productName")); //$NON-NLS-1$
        }
    }

    private String hardwareSerialNumber;

    public String getHardwareSerialNumber()
    {
        return hardwareSerialNumber;
    }

    public void setHardwareSerialNumber(String value)
    {
        if (!StringHelper.stringsEqual(hardwareSerialNumber, value))
        {
            hardwareSerialNumber = value;
            onPropertyChanged(new PropertyChangedEventArgs("serialNumber")); //$NON-NLS-1$
        }
    }

    private String hardwareVersion;

    public String getHardwareVersion()
    {
        return hardwareVersion;
    }

    public void setHardwareVersion(String value)
    {
        if (!StringHelper.stringsEqual(hardwareVersion, value))
        {
            hardwareVersion = value;
            onPropertyChanged(new PropertyChangedEventArgs("hardwareVersion")); //$NON-NLS-1$
        }
    }

    private String hardwareUUID;

    public String getHardwareUUID()
    {
        return hardwareUUID;
    }

    public void setHardwareUUID(String value)
    {
        if (!StringHelper.stringsEqual(hardwareUUID, value))
        {
            hardwareUUID = value;
            onPropertyChanged(new PropertyChangedEventArgs("uuid")); //$NON-NLS-1$
        }
    }

    private String hardwareFamily;

    public String getHardwareFamily()
    {
        return hardwareFamily;
    }

    public void setHardwareFamily(String value)
    {
        if (!StringHelper.stringsEqual(hardwareFamily, value))
        {
            hardwareFamily = value;
            onPropertyChanged(new PropertyChangedEventArgs("family")); //$NON-NLS-1$
        }
    }

    public HostHardwareGeneralModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().generalTitle());
        setHashName("hardware"); //$NON-NLS-1$
        setAvailableInModes(ApplicationMode.VirtOnly);
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.entityPropertyChanged(sender, e);
    }

    private void updateProperties()
    {
        VDS vds = getEntity();
        setHardwareManufacturer(vds.getHardwareManufacturer());
        setHardwareVersion(vds.getHardwareVersion());
        setHardwareProductName(vds.getHardwareProductName());
        setHardwareUUID(vds.getHardwareUUID());
        setHardwareSerialNumber(vds.getHardwareSerialNumber());
        setHardwareFamily(vds.getHardwareFamily());
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();

        if (getEntity() != null)
        {
            updateProperties();
        }
    }
}
