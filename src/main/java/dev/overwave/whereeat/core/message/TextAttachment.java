package dev.overwave.whereeat.core.message;

import dev.overwave.whereeat.core.entity.LongGen;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class TextAttachment extends LongGen {

    @Column(name = "_offset")
    private int offset;

    @Column(name = "_length")
    private int length;

    @Column(name = "_type")
    @Enumerated(EnumType.STRING)
    private AttachmentType type;

    @Column(name = "_value")
    private String value;

    @ManyToOne
    @JoinColumn(referencedColumnName = "id")
    private Message message;

    public TextAttachment(int offset, int length, AttachmentType attachmentType) {
        this.offset = offset;
        this.length = length;
        this.type = attachmentType;
    }

    public TextAttachment(int offset, int length, AttachmentType attachmentType, String value) {
        this.offset = offset;
        this.length = length;
        this.type = attachmentType;
        this.value = value;
    }
}
