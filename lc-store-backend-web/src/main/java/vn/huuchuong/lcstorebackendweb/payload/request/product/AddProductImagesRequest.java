package vn.huuchuong.lcstorebackendweb.payload.request.product;



import lombok.Data;

import java.util.List;

@Data
public class AddProductImagesRequest {

    private List<String> imageUrls;
}

