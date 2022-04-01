package dev.overwave.whereeat.post;

import dev.overwave.whereeat.file.File;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private boolean hidden;

    private String text;

    @OneToOne
    @JoinColumn(name = "file_id")
    private File file;

    public Post(String text, File file) {
        this.text = text;
        this.file = file;
    }
}
