package dev.overwave.whereeat.core.auth;

import dev.overwave.whereeat.core.entity.IntegerGen;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Session extends IntegerGen {

    private String token;

    @ManyToOne
    @JoinColumn(referencedColumnName = "id")
    private User user;
}
