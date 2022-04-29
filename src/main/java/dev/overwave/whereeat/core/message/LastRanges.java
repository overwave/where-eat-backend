package dev.overwave.whereeat.core.message;

import lombok.Value;

@Value
public class LastRanges {
    ScannedRange first;

    ScannedRange second;

    public boolean isEmpty() {
        return first == null && second == null;
    }
}
