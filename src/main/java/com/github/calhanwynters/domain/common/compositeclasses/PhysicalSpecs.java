package com.github.calhanwynters.domain.common.compositeclasses;

import com.github.calhanwynters.domain.common.valueobjects.CareInstruction;
import com.github.calhanwynters.domain.common.valueobjects.Dimensions;
import com.github.calhanwynters.domain.common.valueobjects.Weight;
import com.github.calhanwynters.domain.common.validationchecks.DomainGuard;

public record PhysicalSpecs(
        Weight weight,
        Dimensions dimensions,
        CareInstruction careInstructions
) {
    public static final PhysicalSpecs NONE = new PhysicalSpecs(
            Weight.NONE, Dimensions.NONE, CareInstruction.NONE
    );

    public PhysicalSpecs {
        DomainGuard.notNull(weight, "Weight");
        DomainGuard.notNull(dimensions, "Dimensions");
        DomainGuard.notNull(careInstructions, "Care Instructions");
    }

    public boolean isNone() {
        return weight.isNone() && dimensions.isNone() && (careInstructions == null || careInstructions.equals(CareInstruction.NONE));
    }
}
