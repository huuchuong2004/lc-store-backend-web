package vn.huuchuong.lcstorebackendweb.payload.request.catogory;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.huuchuong.lcstorebackendweb.entity.Category;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateCategoryRequest {

    private String name;

    private String description;


    private Integer parent;

}
