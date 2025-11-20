package vn.huuchuong.lcstorebackendweb.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.huuchuong.lcstorebackendweb.entity.Category;
import vn.huuchuong.lcstorebackendweb.payload.request.catogory.CreateCategoryRequest;

import java.util.List;

public interface ICategoryService {

    Page<Category> findAll(Pageable pageable);

    Category create(CreateCategoryRequest category);


    Boolean delete(Integer id);
    List<Category> findRoots();

    List<Category> findByParent(Integer parentId);
}
