package org.ovirt.engine.core.common.businessentities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EditableOnVmStatusField {

    VMStatus[] statuses() default { VMStatus.Down };

}
