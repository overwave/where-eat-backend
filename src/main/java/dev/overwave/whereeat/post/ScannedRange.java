package dev.overwave.whereeat.post;

import dev.overwave.whereeat.core.entity.LongGen;
import javax.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScannedRange extends LongGen {
    private int ordinal;

    private long messageIdFrom;

    private long messageIdTo;

    @Override
    public String toString() {
        return "[%d->%d]".formatted(messageIdFrom, messageIdTo);
    }
}
