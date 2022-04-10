package dev.overwave.whereeat.post;

import dev.overwave.whereeat.core.entity.IntegerGen;
import dev.overwave.whereeat.file.File;
import dev.overwave.whereeat.util.StringUtils;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Post extends IntegerGen {

    private long messageId;

    private String text;

    @OneToOne
    @JoinColumn(name = "file_id")
    private File file;

    @Enumerated(EnumType.STRING)
    private PostType type;

    private boolean hidden;

    public Post(long messageId, String text, PostType type, File file) {
        this.messageId = messageId;
        this.text = text;
        this.type = type;
        this.file = file;
    }

    @Override
    public String toString() {
        String fileId = file != null ? ", file id: " + file.getId() : "";
        return StringUtils.trimWithEllipsis(text, 10) + fileId;
    }
}
