package vn.huuchuong.lcstorebackendweb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.huuchuong.lcstorebackendweb.base.BaseResponse;

import vn.huuchuong.lcstorebackendweb.payload.request.product.*;
import vn.huuchuong.lcstorebackendweb.payload.response.ProductListResponse;
import vn.huuchuong.lcstorebackendweb.payload.response.ProductResponse;
import vn.huuchuong.lcstorebackendweb.service.IProductService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final IProductService productService;

    // 1. Create product
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<ProductResponse>> create(
            @RequestBody CreateProductRequest req) {
        return ResponseEntity.ok(
                BaseResponse.success(productService.createProduct(req), "Tạo sản phẩm thành công")
        );
    }

    // 2. Get all product (list)
    @GetMapping
    public ResponseEntity<BaseResponse<Page<ProductListResponse>>> getAllProduct(
            @PageableDefault(page = 0, size = 10, sort = "productId", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<ProductListResponse> result = productService.findAll(pageable);
        return ResponseEntity.ok(BaseResponse.success(result, "Lấy danh sách thành công"));
    }


    // 3. Get detail product
    @GetMapping("/{productId}")
    public ResponseEntity<BaseResponse<ProductResponse>> getDetail(@PathVariable Integer productId) {
        return ResponseEntity.ok(
                BaseResponse.success(productService.getProductDetail(productId), "Lấy chi tiết thành công")
        );
    }

    // 4. Update product
    @PutMapping("/{productId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<ProductResponse>> update(
            @PathVariable Integer productId,
            @RequestBody UpdateProductRequest req) {
        return ResponseEntity.ok(
                BaseResponse.success(productService.updateProduct(productId, req), "Cập nhật thành công")
        );
    }

    // 5. Delete product
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<String>> delete(@PathVariable Integer productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.ok(BaseResponse.success("Xóa sản phẩm thành công"));
    }


    // ====================== VARIANTS ==========================

    // 6. Create variant
    @PostMapping("/{productId}/variants")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<ProductResponse>> addVariant(
            @PathVariable Integer productId,
            @RequestBody CreateProductVariantRequest req) {
        return ResponseEntity.ok(
                BaseResponse.success(productService.createpv(productId, req), "Thêm biến thể thành công")
        );
    }

    // 7. Update variant
    @PutMapping("/{productId}/variants/{variantId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<ProductResponse>> updateVariant(
            @PathVariable Integer productId,
            @PathVariable Integer variantId,
            @RequestBody UpdateProductVariantRequest req) {
        return ResponseEntity.ok(
                BaseResponse.success(productService.updateVariant(productId, variantId, req),
                        "Cập nhật biến thể thành công")
        );
    }

    // 8. Delete variant
    @DeleteMapping("/{productId}/variants/{variantId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<String>> deleteVariant(
            @PathVariable Integer productId,
            @PathVariable Integer variantId) {
        productService.deleteVariant(productId, variantId);
        return ResponseEntity.ok(BaseResponse.success("Xóa biến thể thành công"));
    }

    // ====================== IMAGES ==========================

    // 9. Add images
    @PostMapping("/{productId}/images")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<ProductResponse>> addImages(
            @PathVariable Integer productId,
            @RequestBody AddProductImagesRequest req) {
        return ResponseEntity.ok(
                BaseResponse.success(productService.addImages(productId, req), "Thêm ảnh thành công")
        );
    }

    // 10. Delete image
    @DeleteMapping("/{productId}/images/{imageId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<String>> deleteImage(
            @PathVariable Integer productId,
            @PathVariable Integer imageId) {
        productService.deleteImage(productId, imageId);
        return ResponseEntity.ok(BaseResponse.success("Xóa ảnh thành công"));
    }

    // 11. Hàm tìm kiếm theo tên...
    @GetMapping("search")
    public ResponseEntity<BaseResponse<Page<ProductListResponse>>> search(@ModelAttribute ProductFilter productFilter,Pageable pageable) {

        Page page = productService.search(productFilter,pageable);
        return ResponseEntity.ok(new BaseResponse<>(page,"Tim kiem thanh cong"));

    }

    // 12 lay danh sach theo tree
    @GetMapping("/categories/{id}/products")
    public ResponseEntity<BaseResponse<Page<ProductListResponse>>> getProductsByCategorys( @PathVariable Integer id, Pageable pageable) {
            return ResponseEntity.ok(new BaseResponse<>(productService.getProductByCategpgys(id,pageable),"Lay danh sach thanh cong"));
    }
}
