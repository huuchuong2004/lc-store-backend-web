package vn.huuchuong.lcstorebackendweb.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.huuchuong.lcstorebackendweb.base.BaseResponse;
import vn.huuchuong.lcstorebackendweb.entity.Category;
import vn.huuchuong.lcstorebackendweb.exception.BusinessException;
import vn.huuchuong.lcstorebackendweb.payload.request.catogory.CreateCategoryRequest;
import vn.huuchuong.lcstorebackendweb.repository.ICategoryRepository;
import vn.huuchuong.lcstorebackendweb.service.ICategoryService;

import java.util.List;
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements ICategoryService {
    private final ICategoryRepository categoryRepository;


    @Override
    public Page<Category> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }


    @Override
    public Category create(CreateCategoryRequest request) {
        Category cate = new Category();
        cate.setName(request.getName());
        cate.setDescription(request.getDescription());

        if (request.getParent() != null) {
            Category parent = categoryRepository.findById(request.getParent())
                    .orElseThrow(() -> new RuntimeException("Parent not found"));

            parent.addChild(cate);

        }

        return categoryRepository.save(cate);
    }


    @Transactional
    @Override
    public Boolean delete(Integer id) {
        // 1. Tìm category, nếu không có thì báo lỗi rõ ràng
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Category not found with id = " + id));

        // 2. Không cho xoá nếu vẫn còn category con
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            throw new BusinessException("Không thể xoá category vì vẫn còn category con");
        }

        // 3. (Optional) Không cho xoá nếu category đang được sử dụng (ví dụ có product)
        // if (productRepository.existsByCategoryId(id)) {
        //     throw new BusinessException("Không thể xoá category vì đang được sử dụng bởi sản phẩm");
        // }


        categoryRepository.delete(category);
        return true;
    }

    @Override // lay Category chinh
    public List<Category> findRoots() {
        return categoryRepository.findAllByParentIsNull();
    }

    @Override // lay categpry con
    public List<Category> findByParent(Integer parentId) {
        return categoryRepository.findAllByParentId(parentId);
    }

}


