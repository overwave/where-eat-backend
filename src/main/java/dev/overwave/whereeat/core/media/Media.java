package dev.overwave.whereeat.core.media;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Media {
    @Id
    private String id;

    private int width;

    private int height;

    @Lob
    @Type(type = "org.hibernate.type.BinaryType")
    private byte[] data;
}
