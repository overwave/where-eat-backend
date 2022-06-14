package dev.overwave.whereeat.core.auth;

import dev.overwave.whereeat.core.entity.IntegerGen;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import java.util.List;

import static com.vladmihalcea.hibernate.type.array.internal.AbstractArrayType.SQL_ARRAY_TYPE;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_user")
public class User extends IntegerGen {
    private String email;

    @Type(type = "com.vladmihalcea.hibernate.type.array.ListArrayType",
            parameters = @Parameter(name = SQL_ARRAY_TYPE, value = "text"))
    private List<Role> roles;

    private String idProvider;

    private String externalId;

    @Column(name = "_name")
    private String name;

    private String familyName;

    private String givenName;

    @Nullable
    private String pictureUrl;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Session> sessions;
}
