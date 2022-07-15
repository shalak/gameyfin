package de.grimsi.gameyfin.entities;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Theme {
    @Id
    private String slug;

    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Theme theme = (Theme) o;
        return slug != null && Objects.equals(slug, theme.slug);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}