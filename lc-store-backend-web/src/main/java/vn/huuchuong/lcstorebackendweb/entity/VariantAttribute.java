package vn.huuchuong.lcstorebackendweb.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@RequiredArgsConstructor
public class VariantAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;   // Size, Color
    private String value;  // L, Đen

    @ManyToOne
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;
}
