package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.common.businessentities.LdapRefStatus;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.StringHelper;

public class VdcUserConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {
    public enum UserOrGroup {
        User,
        Group
    }

    public VdcUserConditionFieldAutoCompleter() {
        super();
        // Building the basic vervs Dict
        mVerbs.add("NAME");
        mVerbs.add("LASTNAME");
        mVerbs.add("USRNAME");
        mVerbs.add("DEPARTMENT");
        mVerbs.add("GROUP");
        mVerbs.add("TITLE");
        mVerbs.add("STATUS");
        mVerbs.add("ROLE");
        mVerbs.add("TAG");
        mVerbs.add("POOL");
        mVerbs.add("TYPE");

        // Building the autoCompletion Dict
        buildCompletions();
        // Building the types dict
        getTypeDictionary().put("NAME", String.class);
        getTypeDictionary().put("LASTNAME", String.class);
        getTypeDictionary().put("USRNAME", String.class);
        getTypeDictionary().put("DEPARTMENT", String.class);
        getTypeDictionary().put("TITLE", String.class);
        getTypeDictionary().put("GROUP", String.class);
        getTypeDictionary().put("STATUS", LdapRefStatus.class);
        getTypeDictionary().put("ROLE", String.class);
        getTypeDictionary().put("TAG", String.class);
        getTypeDictionary().put("POOL", String.class);
        getTypeDictionary().put("TYPE", UserOrGroup.class);

        // building the ColumnName Dict
        columnNameDict.put("NAME", "name");
        columnNameDict.put("LASTNAME", "surname");
        columnNameDict.put("USRNAME", "username");
        columnNameDict.put("DEPARTMENT", "department");
        columnNameDict.put("TITLE", "role");
        columnNameDict.put("GROUP", "groups");
        columnNameDict.put("STATUS", "status");
        columnNameDict.put("ROLE", "mla_role");
        columnNameDict.put("TAG", "tag_name");
        columnNameDict.put("POOL", "vm_pool_name");
        columnNameDict.put("TYPE", "user_group");
        // Building the validation dict
        buildBasicValidationTable();
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName) {
        if ("TAG".equals(fieldName)) {
            return StringOnlyEqualConditionRelationAutoCompleter.INSTANCE;
        } else {
            return StringConditionRelationAutoCompleter.INSTANCE;
        }
    }

    @Override
    public IConditionValueAutoCompleter getFieldValueAutoCompleter(String fieldName) {
        IConditionValueAutoCompleter retval = null;
        if ("STATUS".equals(fieldName)) {
            retval = new EnumValueAutoCompleter(LdapRefStatus.class);
        }
        return retval;
    }

    @Override
    public void formatValue(String fieldName, RefObject<String> relations, RefObject<String> value, boolean caseSensitive) {
        if ("STATUS".equals(fieldName)) {
            String tmp = StringHelper.trim(value.argvalue, '\'');
            if ("=".equals(relations.argvalue) && "1".equals(tmp)) {
                relations.argvalue = ">=";
            }
            if ("!=".equals(relations.argvalue) && "1".equals(tmp)) {
                relations.argvalue = "<";
            }
        } else {
            super.formatValue(fieldName, relations, value, caseSensitive);
        }
    }
}
