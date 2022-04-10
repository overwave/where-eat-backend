package dev.overwave.whereeat.core.entity;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class LongGen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id = -1;
}
