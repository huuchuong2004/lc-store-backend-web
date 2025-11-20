package vn.huuchuong.lcstorebackendweb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name="products",uniqueConstraints = {@UniqueConstraint(name = "uq_products_name", columnNames = "name")})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer productId;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private String name;

    private String description;

    private BigDecimal baseprice;

    @OneToMany(mappedBy = "product" , cascade = CascadeType.ALL,orphanRemoval = true)
    @JsonIgnore
    private List<ProductVariant> variants; // size,color

    @OneToMany(mappedBy = "product",cascade = CascadeType.ALL,orphanRemoval = true) // cu co list thì them cade vs orphan
    @JsonIgnore
    private List<ProductImage> images;


}
