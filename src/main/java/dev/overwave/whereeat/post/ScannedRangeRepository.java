package dev.overwave.whereeat.post;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScannedRangeRepository extends JpaRepository<ScannedRange, Long> {
    List<ScannedRange> findTop2ByOrderByOrdinalDesc();

    default LastRanges findTwoLastRanges() {
        List<ScannedRange> ranges = findTop2ByOrderByOrdinalDesc();

        ScannedRange first = ranges.size() > 0 ? ranges.get(0) : null;
        ScannedRange second = ranges.size() > 1 ? ranges.get(1) : null;

        return new LastRanges(first, second);
    }
}
