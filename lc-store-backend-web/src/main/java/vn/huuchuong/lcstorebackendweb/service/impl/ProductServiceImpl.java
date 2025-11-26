package vn.huuchuong.lcstorebackendweb.service.impl;



import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import vn.huuchuong.lcstorebackendweb.entity.*;
import vn.huuchuong.lcstorebackendweb.exception.BusinessException;

import vn.huuchuong.lcstorebackendweb.payload.request.product.*;
import vn.huuchuong.lcstorebackendweb.payload.response.ProductListResponse;
import vn.huuchuong.lcstorebackendweb.payload.response.ProductResponse;
import vn.huuchuong.lcstorebackendweb.payload.response.ProductVariantResponse;
import vn.huuchuong.lcstorebackendweb.repository.*;
import vn.huuchuong.lcstorebackendweb.service.IProductService;
import vn.huuchuong.lcstorebackendweb.spectification.ProductSpectification;
import vn.huuchuong.lcstorebackendweb.utils.ProductMapper;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {

    private final IProductRepository productRepository;
    private final ICategoryRepository categoryRepository;
    private final IProductVariantRepository productVariantRepository;
    private final IProductImageRepository productImageRepository;
    private final ProductMapper productMapper;
    private final InventoryRepository inventoryRepository;

    // ======================== CREATE PRODUCT ============================

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest req) {

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new BusinessException("Danh mục không tồn tại"));

        Product product = new Product();
        product.setCategory(category);
        product.setName(req.getName());
        product.setDescription(req.getDescription());
        product.setBaseprice(req.getBaseprice());

        if (product.getVariants() == null) {
            product.setVariants(new ArrayList<>());
        }
        if (product.getImages() == null) {
            product.setImages(new ArrayList<>());
        }

        // variants
        if (req.getVariants() != null) {
            for (CreateProductVariantRequest vReq : req.getVariants()) {
                ProductVariant variant = new ProductVariant();
                variant.setProduct(product);
                variant.setSize(vReq.getSize());
                variant.setColor(vReq.getColor());
                variant.setPrice(vReq.getPrice());
                variant.setQuantityInStock(vReq.getQuantityInStock());

                String sku = vReq.getSku();
                if (sku == null || sku.isBlank()) {
                    sku = generateUniqueSku(product, vReq);
                }
                variant.setSku(sku);

                product.getVariants().add(variant);
            }
        }

        // images
        if (req.getImageUrls() != null) {
            for (String url : req.getImageUrls()) {
                if (url == null || url.isBlank()) continue;
                ProductImage img = new ProductImage();
                img.setProduct(product);
                img.setImageURL(url.trim());
                product.getImages().add(img);
            }
        }

        Product saved = productRepository.save(product);
        return productMapper.toProductResponse(saved);
    }

    // ======================== GET ALL ============================

    @Override
    @Transactional
    public Page<ProductListResponse> findAll(Pageable pageable) {
        Page<Product> page = productRepository.findAll(pageable);
        return page.map(productMapper::toProductListResponse);
    }

    // ======================== GET DETAIL ============================

    @Override
    @Transactional
    public ProductResponse getProductDetail(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("Sản phẩm không tồn tại"));
        return productMapper.toProductResponse(product);
    }

    // ======================== UPDATE PRODUCT ============================

    @Override
    @Transactional
    public ProductResponse updateProduct(Integer id, UpdateProductRequest req) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Sản phẩm không tồn tại"));

        // category
        if (req.getCategoryId() != null && req.getCategoryId() > 0) {
            Category category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new BusinessException("Danh mục không tồn tại"));
            product.setCategory(category);
        }

        // name
        if (req.getName() != null && !req.getName().isBlank()) {
            String cleaned = req.getName().trim();
            if (cleaned.length() < 3) {
                throw new BusinessException("Tên sản phẩm phải có ít nhất 3 ký tự");
            }
            product.setName(cleaned);
        }

        // description
        if (req.getDescription() != null && !req.getDescription().isBlank()) {
            product.setDescription(req.getDescription().trim());
        }

        // baseprice
        if (req.getBaseprice() != null) {
            if (req.getBaseprice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Giá sản phẩm phải lớn hơn 0");
            }
            product.setBaseprice(req.getBaseprice());
        }

        Product saved = productRepository.save(product);
        return productMapper.toProductResponse(saved);
    }

    // ======================== DELETE PRODUCT ============================

    @Override
    @Transactional
    public void deleteProduct(Integer productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("Sản phẩm không tồn tại"));

        // Lấy danh sách variants
        List<ProductVariant> variants = product.getVariants();

        if (variants != null) {

            // 1. Kiểm tra tồn kho của tất cả variant
            for (ProductVariant variant : variants) {

                Inventory inv = inventoryRepository.findByProductVariant(variant).orElse(null);

                if (inv != null && inv.getCurrentStockLevel() != null
                        && inv.getCurrentStockLevel() > 0) {
                    throw new BusinessException(
                            "Không thể xóa sản phẩm vì biến thể " + variant.getSku()
                                    + " còn tồn kho: " + inv.getCurrentStockLevel()
                    );
                }
            }

            // 2. Xóa inventory nếu tất cả tồn kho = 0
            for (ProductVariant variant : variants) {
                Inventory inv = inventoryRepository.findByProductVariant(variant).orElse(null);

                if (inv != null) {
                    inventoryRepository.delete(inv);
                }
            }
        }

        // 3. Xóa product
        productRepository.delete(product);
    }


    // ======================== VARIANT: CREATE ============================

    @Override
    @Transactional
    public ProductResponse createpv(Integer productId, CreateProductVariantRequest req) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("Sản phẩm không tồn tại"));

        if (product.getVariants() == null) {
            product.setVariants(new ArrayList<>());
        }

        // Kiểm tra trùng size + color
        boolean exists = product.getVariants().stream()
                .anyMatch(v -> v.getSize().equalsIgnoreCase(req.getSize())
                        && v.getColor().equalsIgnoreCase(req.getColor()));
        if (exists) {
            throw new BusinessException("Biến thể này đã tồn tại");
        }

        // Tạo variant mới
        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSize(req.getSize());
        variant.setColor(req.getColor());
        variant.setPrice(req.getPrice());
        variant.setQuantityInStock(req.getQuantityInStock());

        // Gen sku nếu rỗng
        String sku = req.getSku();
        if (sku == null || sku.isBlank()) {
            sku = generateUniqueSku(product, req);
        }
        variant.setSku(sku);

        // Lưu variant vào product
        product.getVariants().add(variant);

        // Lưu product để variant có ID
        Product saved = productRepository.save(product);


        Inventory inventory = new Inventory();
        inventory.setProductVariant(variant);
        inventory.setCurrentStockLevel(variant.getQuantityInStock());
        inventory.setLastUpdate(LocalDate.now());

        inventoryRepository.save(inventory);

        // ================================

        return productMapper.toProductResponse(saved);
    }


    // ======================== VARIANT: UPDATE ============================

    @Override
    @Transactional
    public ProductResponse updateVariant(Integer productId, Integer variantId, UpdateProductVariantRequest req) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("Sản phẩm không tồn tại"));

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new BusinessException("Biến thể không tồn tại"));

        if (!variant.getProduct().getProductId().equals(productId)) {
            throw new BusinessException("Biến thể không thuộc về sản phẩm này");
        }

        String newSize = req.getSize() != null && !req.getSize().isBlank()
                ? req.getSize().trim()
                : variant.getSize();
        String newColor = req.getColor() != null && !req.getColor().isBlank()
                ? req.getColor().trim()
                : variant.getColor();

        boolean duplicated = product.getVariants().stream()
                .anyMatch(v -> !v.getProductVariantId().equals(variantId)
                        && v.getSize().equalsIgnoreCase(newSize)
                        && v.getColor().equalsIgnoreCase(newColor));
        if (duplicated) {
            throw new BusinessException("Biến thể size " + newSize
                    + " - màu " + newColor + " đã tồn tại");
        }

        if (req.getSize() != null && !req.getSize().isBlank()) {
            variant.setSize(newSize);
        }
        if (req.getColor() != null && !req.getColor().isBlank()) {
            variant.setColor(newColor);
        }
        if (req.getPrice() != null) {
            if (req.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Giá biến thể phải lớn hơn 0");
            }
            variant.setPrice(req.getPrice());
        }
        if (req.getQuantityInStock() != null) {
            if (req.getQuantityInStock() < 0) {
                throw new BusinessException("Tồn kho không được âm");
            }
            variant.setQuantityInStock(req.getQuantityInStock());
        }

        productVariantRepository.save(variant);
        return productMapper.toProductResponse(product);
    }

    // ======================== VARIANT: DELETE ============================

    @Override
    @Transactional
    public void deleteVariant(Integer productId, Integer variantId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("Sản phẩm không tồn tại"));

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new BusinessException("Biến thể không tồn tại"));

        if (!variant.getProduct().getProductId().equals(productId)) {
            throw new BusinessException("Biến thể không thuộc về sản phẩm này");
        }

        // 4. Lấy inventory tương ứng
        Inventory inventory = inventoryRepository.findByProductVariant(variant)
                .orElse(null);

        // 5. Nếu còn tồn kho > 0 → cấm xóa
        if (inventory != null && inventory.getCurrentStockLevel() != null
                && inventory.getCurrentStockLevel() > 0) {
            throw new BusinessException(
                    "Không thể xóa biến thể vì còn tồn kho: " + inventory.getCurrentStockLevel()
            );
        }
        // 7. Xóa inventory nếu có
        if (inventory != null) {
            inventoryRepository.delete(inventory);
        }


        productVariantRepository.delete(variant);
    }

    // ======================== IMAGES: ADD ============================

    @Override
    @Transactional
    public ProductResponse addImages(Integer productId, AddProductImagesRequest req) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("Sản phẩm không tồn tại"));

        if (req.getImageUrls() == null || req.getImageUrls().isEmpty()) {
            throw new BusinessException("Danh sách imageUrls không được rỗng");
        }

        if (product.getImages() == null) {
            product.setImages(new ArrayList<>());
        }

        for (String url : req.getImageUrls()) {
            if (url == null || url.isBlank()) continue;
            ProductImage img = new ProductImage();
            img.setProduct(product);
            img.setImageURL(url.trim());
            product.getImages().add(img);
        }

        Product saved = productRepository.save(product);
        return productMapper.toProductResponse(saved);
    }

    // ======================== IMAGES: DELETE ============================

    @Override
    @Transactional
    public void deleteImage(Integer productId, Integer imageId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("Sản phẩm không tồn tại"));

        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException("Ảnh không tồn tại"));

        if (!image.getProduct().getProductId().equals(productId)) {
            throw new BusinessException("Ảnh không thuộc về sản phẩm này");
        }

        if (product.getImages() != null) {
            product.getImages().removeIf(img -> img.getProductImageId().equals(imageId));
        }

        productImageRepository.delete(image);
    }

    @Override
    public Page search(ProductFilter req, Pageable pageable) {
        Specification<Product> spec = Specification.where(null);

        if (req.getName() != null && !req.getName().isBlank()) {
            spec = spec.and(ProductSpectification.hasName(req.getName()));
        }

        if (req.getCategoryId() != null && req.getCategoryId() > 0) {
            spec = spec.and(ProductSpectification.hasCategory(req.getCategoryId()));
        }

        if (req.getMinPrice() != null) {
            spec = spec.and(ProductSpectification.hasMinPrice(req.getMinPrice()));
        }

        if (req.getMaxPrice() != null) {
            spec = spec.and(ProductSpectification.hasMaxPrice(req.getMaxPrice()));
        }

        Page<Product> page = productRepository.findAll(spec, pageable);

        return page.map(productMapper::toProductListResponse);
    }

    @Override
    public Page<ProductListResponse> getProductByCategpgys(Integer categoryId, Pageable pageable) {
        Category root = categoryRepository.findById(categoryId).orElseThrow(()->new BusinessException("Danh Muc Khong Co !"));

        List<Integer> list= getAllCategoryIdsIteractive(root);
        Page<Product> find= productRepository.findByCategory_IdIn(list, pageable);
        return find.map(productMapper::toProductListResponse);

    }

    // ======================== SKU HELPER ============================

    private String generateUniqueSku(Product product, CreateProductVariantRequest req) {
        String base = "P" +
                (product.getProductId() == null ? "NEW" : product.getProductId()) +
                "-" + normalize(req.getSize()) +
                "-" + normalize(req.getColor());

        String sku = base;
        int counter = 1;

        while (productVariantRepository.existsBySku(sku)) {
            counter++;
            sku = base + "-" + counter;
        }

        return sku;
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("\\s+", "-").toUpperCase();
    }

    private List<Integer> getAllCategoryIdsIteractive(Category root){
        List<Integer> categoryIds = new ArrayList<>(); // tao lisst luu id
        Queue<Category> categoryQueue = new LinkedList<>(); // tao 1 hang doi luu catggory

        categoryQueue.add(root); // bo root vao dau tien  // luc nao trong quue se co ao
        while (!categoryQueue.isEmpty()) { // catgory co ao ti tuc
            Category category = categoryQueue.poll(); // lay phan tu ban dau ra va xoa no ngay lap tu ckhoi queue
            categoryIds.add(category.getId());
            if (category.getChildren() != null && !category.getChildren().isEmpty()) { // neu no co con thi bo con no vao queue,
                // vi du duuyet ao thi ao co con la ao thun thi bi ao thun vao,, duyet tiep ao thun cho toi khi orng hti se co dc list id
                categoryQueue.addAll(category.getChildren());
            }
        }
        return categoryIds;
    }
}


