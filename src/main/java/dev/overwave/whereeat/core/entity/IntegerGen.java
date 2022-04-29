package dev.overwave.whereeat.core.entity;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass
public class IntegerGen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id = -1;
}
