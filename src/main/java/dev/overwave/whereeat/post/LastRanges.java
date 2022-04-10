package dev.overwave.whereeat.post;

import lombok.Value;

@Value
public class LastRanges {
    ScannedRange first;

    ScannedRange second;

    public boolean isEmpty() {
        return first == null && second == null;
    }
}
