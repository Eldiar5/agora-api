package agora.entity.tags;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "tags")
public class Tags {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long tagId;

    @Column(name = "name")
    private String tagName;

}
