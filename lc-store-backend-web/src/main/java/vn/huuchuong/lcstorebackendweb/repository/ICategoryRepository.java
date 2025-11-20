package vn.huuchuong.lcstorebackendweb.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import vn.huuchuong.lcstorebackendweb.entity.Category;

import java.util.List;

public interface ICategoryRepository extends JpaRepository<Category, Integer> , JpaSpecificationExecutor<Category> {
    // Lấy tất cả category không có parent
    List<Category> findAllByParentIsNull();

    // Lấy tất cả category con theo parent id
    List<Category> findAllByParentId(Integer parentId);

}
