package vn.huuchuong.lcstorebackendweb.entity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import vn.huuchuong.lcstorebackendweb.base.BaseEntity;

import java.util.ArrayList;
import java.util.List;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(
        name = "category", uniqueConstraints = {@UniqueConstraint(name = "uq_category_name", columnNames = "name") }// dam bao ko co 2 user nao trugn email}// ngan trung du lieu
)
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  int id;


    private String name;

    private String description;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "parent_id")
    private Category parent;

    // Category con
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Category> children = new ArrayList<>();

    public void addChild(Category child) {
        child.setParent(this);        // Gán parent cho category con
        this.children.add(child);     // Thêm vào danh sách con
    }

    public void setParent(Category parent) {
        this.parent = parent;
        if (parent != null && !parent.getChildren().contains(this)) {
            parent.getChildren().add(this);
        }
    }









}
