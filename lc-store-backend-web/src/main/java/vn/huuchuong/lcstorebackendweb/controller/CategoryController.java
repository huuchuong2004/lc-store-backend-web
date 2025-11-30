package vn.huuchuong.lcstorebackendweb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import vn.huuchuong.lcstorebackendweb.base.BaseResponse;
import vn.huuchuong.lcstorebackendweb.entity.Category;
import vn.huuchuong.lcstorebackendweb.payload.request.catogory.CreateCategoryRequest;
import vn.huuchuong.lcstorebackendweb.service.ICategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categorys")
@RequiredArgsConstructor
public class CategoryController {

    private final ICategoryService categoryService;

    @GetMapping
    public ResponseEntity<BaseResponse<Page<Category>>> getAllCategory(Pageable pageable) {
        return ResponseEntity.ok(new BaseResponse<>(categoryService.findAll(pageable),"Lay danh sach Thanh Cong"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<List<Category>>> getCategoryById(@PathVariable int id) { // lay ctegory co cha me
        return ResponseEntity.ok(new BaseResponse<>(categoryService.findByParent(id),null));
    }

    @GetMapping("/root")
    public ResponseEntity<BaseResponse<List<Category>>> getCategoryRoot() { // lay category goc
        return ResponseEntity.ok(new BaseResponse<>(categoryService.findRoots(),null));
    }

    @Transactional
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<Category>> addCategory(@RequestBody CreateCategoryRequest category) {
        return ResponseEntity.ok(new BaseResponse<>(categoryService.create(category),"Tao thanh cong"));
    }

    @Transactional
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<Boolean>> deleteCategory(@PathVariable Integer id) {
        return ResponseEntity.ok(new BaseResponse<>(categoryService.delete(id),"Xoa thanh cong"));
    }


}
