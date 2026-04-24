package com.github.calhanwynters.domain;

import com.github.calhanwynters.MyLibJavaWebCommonApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

public class ModularityTests {
    static ApplicationModules modules = ApplicationModules.of(MyLibJavaWebCommonApplication.class);

    @Test
    void verifiesModularStructure() { modules.verify(); }
}
