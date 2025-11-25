package vn.huuchuong.lcstorebackendweb.utils;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import vn.huuchuong.lcstorebackendweb.entity.Product;
import vn.huuchuong.lcstorebackendweb.entity.ProductImage;
import vn.huuchuong.lcstorebackendweb.entity.ProductVariant;
import vn.huuchuong.lcstorebackendweb.payload.response.ProductListResponse;
import vn.huuchuong.lcstorebackendweb.payload.response.ProductResponse;
import vn.huuchuong.lcstorebackendweb.payload.response.ProductVariantResponse;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
 public class ProductMapper {

    private final ModelMapper modelMapper;

    public ProductResponse toProductResponse(Product product) {
        ProductResponse.ProductResponseBuilder builder = ProductResponse.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .baseprice(product.getBaseprice());

        if (product.getCategory() != null) {
            builder.categoryId(product.getCategory().getId())
                    .categoryName(product.getCategory().getName());
        }

        List<String> imageUrls = new ArrayList<>();
        if (product.getImages() != null) {
            for (ProductImage img : product.getImages()) {
                if (img.getImageURL() != null) {
                    imageUrls.add(img.getImageURL());
                }
            }
        }

        List<ProductVariantResponse> variants = new ArrayList<>();
        if (product.getVariants() != null) {
            for (ProductVariant v : product.getVariants()) {
                ProductVariantResponse vDto =
                        modelMapper.map(v, ProductVariantResponse.class);
                variants.add(vDto);
            }
        }

        return builder
                .imageUrls(imageUrls)
                .variants(variants)
                .build();
    }

    public ProductListResponse toProductListResponse(Product product) {

        ProductListResponse.ProductListResponseBuilder builder = ProductListResponse.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .baseprice(product.getBaseprice());

        if (product.getCategory() != null) {
            builder.categoryId(product.getCategory().getId())
                    .categoryName(product.getCategory().getName());
        }

        List<String> imageUrls = new ArrayList<>();
        if (product.getImages() != null) {
            for (ProductImage img : product.getImages()) {
                if (img.getImageURL() != null) {
                    imageUrls.add(img.getImageURL());
                }
            }
        }

        return builder
                .imageUrls(imageUrls)
                .build();
    }
}
