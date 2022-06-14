package dev.overwave.whereeat.core.message;

import dev.overwave.whereeat.core.entity.IntegerGen;
import dev.overwave.whereeat.core.media.Media;
import dev.overwave.whereeat.core.util.StringUtils;
import javax.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Message extends IntegerGen {

    private long messageId;

    @Column(name = "_text")
    private String text;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL)
    private List<TextAttachment> textAttachments;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "media_id")
    private Media media;

    @Enumerated(EnumType.STRING)
    @Column(name = "_type")
    private MessageType type;

    @Column(name = "_timestamp")
    private Instant timestamp;

    private boolean hidden;

    private String groupId;

    public Message(long messageId, String text, List<TextAttachment> attachments, MessageType type, Instant timestamp,
                   Media media, String groupId) {
        this.messageId = messageId;
        this.text = text;
        this.textAttachments = attachments;
        this.type = type;
        this.timestamp = timestamp;
        this.media = media;
        this.groupId = groupId;
    }

    @Override
    public String toString() {
        String fileId = media != null ? ", file id: " + media.getId() : "";
        return StringUtils.trimWithEllipsis(text, 10) + fileId;
    }
}
