package org.ovirt.engine.core.searchbackend;

import java.util.Date;
import java.util.UUID;

import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;

public class DiskConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {
    public DiskConditionFieldAutoCompleter() {
        // Building the basic verbs set.
        mVerbs.add("ALIAS");
        mVerbs.add("DESCRIPTION");
        mVerbs.add("PROVISIONED_SIZE");
        mVerbs.add("SIZE");
        mVerbs.add("ACTUAL_SIZE");
        mVerbs.add("CREATION_DATE");
        mVerbs.add("BOOTABLE");
        mVerbs.add("SHAREABLE");
        mVerbs.add("STATUS");
        mVerbs.add("DISK_TYPE");
        mVerbs.add("NUMBER_OF_VMS");
        mVerbs.add("VM_NAMES");
        mVerbs.add("QUOTA");
        mVerbs.add("ID");

        // Building the autoCompletion dict.
        buildCompletions();

        // Building the types dict.
        getTypeDictionary().put("ALIAS", String.class);
        getTypeDictionary().put("DESCRIPTION", String.class);
        getTypeDictionary().put("PROVISIONED_SIZE", Long.class);
        getTypeDictionary().put("SIZE", Long.class);
        getTypeDictionary().put("ACTUAL_SIZE", Long.class);
        getTypeDictionary().put("CREATION_DATE", Date.class);
        getTypeDictionary().put("BOOTABLE", Boolean.class);
        getTypeDictionary().put("SHAREABLE", Boolean.class);
        getTypeDictionary().put("STATUS", ImageStatus.class);
        getTypeDictionary().put("DISK_TYPE", DiskStorageType.class);
        getTypeDictionary().put("NUMBER_OF_VMS", Integer.class);
        getTypeDictionary().put("VM_NAMES", String.class);
        getTypeDictionary().put("QUOTA", String.class);
        getTypeDictionary().put("ID", UUID.class);

        // building the ColumnName dict. - the name of the column in db
        columnNameDict.put("ALIAS", "disk_alias");
        columnNameDict.put("DESCRIPTION", "disk_description");
        columnNameDict.put("PROVISIONED_SIZE", "size");
        columnNameDict.put("SIZE", "size");
        columnNameDict.put("ACTUAL_SIZE", "actual_size");
        columnNameDict.put("CREATION_DATE", "creation_date");
        columnNameDict.put("BOOTABLE", "boot");
        columnNameDict.put("SHAREABLE", "shareable");
        columnNameDict.put("STATUS", "imageStatus");
        columnNameDict.put("DISK_TYPE", "disk_storage_type");
        columnNameDict.put("NUMBER_OF_VMS", "number_of_vms");
        columnNameDict.put("VM_NAMES", "vm_names");
        columnNameDict.put("QUOTA", "quota_name");
        columnNameDict.put("ID", "disk_id");

        // Building the validation dict.
        buildBasicValidationTable();
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName) {
        if ("CREATIONDATE".equals(fieldName) || "SIZE".equals(fieldName)
                || "ACTUAL_SIZE".equals(fieldName)
                || "PROVISIONED_SIZE".equals(fieldName)) {
            return BiggerOrSmallerRelationAutoCompleter.INSTANCE;
        }
        else if ("NUMBER_OF_VMS".equals(fieldName)) {
            return NumericConditionRelationAutoCompleter.INSTANCE;
        } else {
            return StringConditionRelationAutoCompleter.INSTANCE;
        }
    }

    @Override
    public IConditionValueAutoCompleter getFieldValueAutoCompleter(String fieldName) {
        if ("STATUS".equals(fieldName)) {
            return new EnumValueAutoCompleter(ImageStatus.class);
        } else if ("DISK_TYPE".equals(fieldName)) {
            return new EnumValueAutoCompleter(DiskStorageType.class);
        } else if ("BOOTABLE".equals(fieldName) ||
                   "SHAREABLE".equals(fieldName) ) {
            return new BitValueAutoCompleter();
        }
        return null;
    }

    @Override
    public void formatValue(String fieldName,
            RefObject<String> relations,
            RefObject<String> value,
            boolean caseSensitive) {
        if ("CREATIONDATE".equals(fieldName)) {
            Date tmp = new Date(Date.parse(StringHelper.trim(value.argvalue, '\'')));
            value.argvalue = StringFormat.format("'%1$s'", tmp);
        } else {
            super.formatValue(fieldName, relations, value, caseSensitive);
        }
    }
}
