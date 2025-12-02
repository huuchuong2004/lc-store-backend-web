package vn.huuchuong.lcstorebackendweb.controller;


import com.fasterxml.jackson.databind.ser.Serializers;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.huuchuong.lcstorebackendweb.base.BaseResponse;
import vn.huuchuong.lcstorebackendweb.entity.Invoice;
import vn.huuchuong.lcstorebackendweb.payload.invoice.InvoiceDTO;
import vn.huuchuong.lcstorebackendweb.payload.response.cart.CartResponse;
import vn.huuchuong.lcstorebackendweb.service.impl.InvoiceService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<Page<InvoiceDTO>>> getInvoices(Pageable pageable) {
        return ResponseEntity.ok(
                BaseResponse.success(invoiceService.getInvoices(pageable),
                        "Lấy danh sách hóa đơn thành công")
        );
    }

    @GetMapping("/my")
    public ResponseEntity<BaseResponse<List<InvoiceDTO>>> getMyInvoices() {
        return ResponseEntity.ok(
                BaseResponse.success(invoiceService.getMyInvoices(),
                        "Lấy danh sách hóa đơn của bạn thành công")
        );
    }

    @GetMapping("/{invoiceId}")
    public ResponseEntity<BaseResponse<InvoiceDTO>> getInvoiceById(@PathVariable Integer invoiceId) {
        return ResponseEntity.ok(
                BaseResponse.success(invoiceService.getInvoiceById(invoiceId),
                        "Lấy hóa đơn thành công"));
    }

}
