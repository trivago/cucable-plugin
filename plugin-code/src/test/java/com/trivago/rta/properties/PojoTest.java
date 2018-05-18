package com.trivago.rta.properties;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.filters.FilterPackageInfo;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.affirm.Affirm;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import org.junit.Test;

import java.util.List;

public class PojoTest {
    private static final int EXPECTED_CLASS_COUNT = 3;
    private static final String POJO_PACKAGE = "com.trivago.rta.properties";

    @Test
    public void expectedPojoCountTest() {
        List<PojoClass> pojoClasses = PojoClassFactory.getPojoClasses(POJO_PACKAGE,
                new FilterPackageInfo());
        Affirm.affirmEquals("Classes added / removed?", EXPECTED_CLASS_COUNT, pojoClasses.size());
    }

    @Test
    public void pojoStructureAndBehaviorTest() {
        Validator validator = ValidatorBuilder.create()
                .with(new SetterTester())
                .with(new GetterTester())
                .build();

        validator.validate(POJO_PACKAGE, new FilterPackageInfo());
    }
}

