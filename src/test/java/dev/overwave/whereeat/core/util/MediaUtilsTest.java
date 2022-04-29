package dev.overwave.whereeat.core.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.util.Pair;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MediaUtilsTest {

    public static final int GAP = 6;
    public static final int TARGET_WIDTH = 600;

    @Test
    void justifyForSingleBox() {
        List<Pair<Integer, Integer>> box = List.of(Pair.of(600, 400));

        List<Pair<Integer, Integer>> actual = MediaUtils.justify(box, TARGET_WIDTH, 300, GAP);
        List<Pair<Integer, Integer>> expected = List.of(Pair.of(TARGET_WIDTH - GAP - GAP, 392));

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void justifyForTwoEqualBoxes() {
        List<Pair<Integer, Integer>> box = List.of(Pair.of(200, 400), Pair.of(200, 400));

        List<Pair<Integer, Integer>> actual = MediaUtils.justify(box, TARGET_WIDTH, 300, GAP);
        List<Pair<Integer, Integer>> expected = List.of(
                Pair.of((TARGET_WIDTH - 3 * GAP) / 2, 582),
                Pair.of((TARGET_WIDTH - 3 * GAP) / 2, 582));

        assertThat(actual).isEqualTo(expected);
    }
}
