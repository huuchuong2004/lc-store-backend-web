package vn.huuchuong.lcstorebackendweb.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.huuchuong.lcstorebackendweb.payload.request.product.*;
import vn.huuchuong.lcstorebackendweb.payload.response.ProductListResponse;
import vn.huuchuong.lcstorebackendweb.payload.response.ProductResponse;


import java.util.List;

public interface IProductService {

    ProductResponse createProduct(CreateProductRequest req);

    Page<ProductListResponse> findAll(Pageable pageable);

    ProductResponse getProductDetail(Integer productId);

    ProductResponse updateProduct(Integer id, UpdateProductRequest req);

    void deleteProduct(Integer id);

    ProductResponse createpv(Integer productId, CreateProductVariantRequest req);

    ProductResponse updateVariant(Integer productId, Integer variantId, UpdateProductVariantRequest req);

    void deleteVariant(Integer productId, Integer variantId);

    ProductResponse addImages(Integer productId, AddProductImagesRequest req);

    void deleteImage(Integer productId, Integer imageId);

    Page search(ProductFilter productFilter, Pageable pageable);

    Page<ProductListResponse> getProductByCategpgys(Integer categoryId, Pageable pageable);

    void deleteImageByUrl(Integer productId, String imageUrl);
}
