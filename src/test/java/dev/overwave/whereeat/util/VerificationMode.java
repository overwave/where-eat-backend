package dev.overwave.whereeat.util;

import lombok.experimental.UtilityClass;

import static org.mockito.Mockito.times;

@UtilityClass
public class VerificationMode {
    public static org.mockito.verification.VerificationMode once() {
        return times(1);
    }
}
