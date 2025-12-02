package vn.huuchuong.lcstorebackendweb.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.huuchuong.lcstorebackendweb.entity.*;
import vn.huuchuong.lcstorebackendweb.exception.BusinessException;
import vn.huuchuong.lcstorebackendweb.payload.invoice.InvoiceDTO;
import vn.huuchuong.lcstorebackendweb.payload.invoice.OrderDTO;
import vn.huuchuong.lcstorebackendweb.payload.invoice.UserDTO;
import vn.huuchuong.lcstorebackendweb.repository.IInvoiceRepository;
import vn.huuchuong.lcstorebackendweb.repository.IUserRepository;
import vn.huuchuong.lcstorebackendweb.repository.InvoiceItemRepository;
import vn.huuchuong.lcstorebackendweb.service.IInvoiceService;
import vn.huuchuong.lcstorebackendweb.utils.InvoiceMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceService implements IInvoiceService {

    private final IInvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final IUserRepository userRepository;

    @Transactional
    public Invoice createInvoiceForOrder(Order order) {

        // Check nếu order đã có invoice → tránh tạo trùng
        if (invoiceRepository.existsByOrder(order)) {
            return invoiceRepository.findByOrder(order);
        }

        Invoice invoice = new Invoice();
        invoice.setOrder(order);
        invoice.setInvoiceDate(LocalDateTime.now());
        invoice.setBuyerName(order.getUser().getFirstName()+" "+order.getUser().getLastName());
        invoice.setBuyerAddress(order.getShippingAddress());
        invoice.setBuyerEmail(order.getUser().getEmail());
        invoice.setBuyerPhone(order.getUser().getPhone());

        invoice.setTotalAmount(order.getTotalAmount());

        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Tạo từng invoice item
        for (OrderItem oi : order.getItems()) {

            BigDecimal lineTotal = oi.getUnitPrice()
                    .multiply(BigDecimal.valueOf(oi.getQuantity()));

            InvoiceItem ii = new InvoiceItem();
            ii.setInvoice(savedInvoice);
            ii.setOrderItem(oi);
            ii.setQuantity(oi.getQuantity());
            ii.setUnitPrice(oi.getUnitPrice());
            ii.setTotal(lineTotal);

            invoiceItemRepository.save(ii);
        }

        return invoiceRepository.findById(savedInvoice.getInvoiceId())
                .orElseThrow(() -> new BusinessException("Lỗi tải invoice sau khi tạo"));
    }

    @Override
    public Invoice createInvoiceForOrderIfNotExists(Order order) {

        return createInvoiceForOrder(order);
    }

    private Invoice doCreateInvoice(Order order) {
        User user = order.getUser();
        if (user == null) {
            throw new BusinessException("Đơn hàng không có thông tin user, không thể tạo hóa đơn");
        }

        // 2. Tạo invoice
        Invoice invoice = new Invoice();
        invoice.setOrder(order);
        invoice.setInvoiceDate(LocalDateTime.now());

        invoice.setBuyerName(user.getFirstName() + " " + user.getLastName());
        invoice.setBuyerAddress(order.getShippingAddress());
        invoice.setBuyerEmail(user.getEmail());
        invoice.setBuyerPhone(user.getPhone());


        invoice.setTotalAmount(order.getTotalAmount());

        Invoice saved = invoiceRepository.save(invoice);

        // 3. Tạo invoice item từ order item
        for (OrderItem oi : order.getItems()) {

            BigDecimal lineTotal = oi.getUnitPrice()
                    .multiply(BigDecimal.valueOf(oi.getQuantity()));

            InvoiceItem ii = new InvoiceItem();
            ii.setInvoice(saved);
            ii.setOrderItem(oi);
            ii.setQuantity(oi.getQuantity());
            ii.setUnitPrice(oi.getUnitPrice());
            ii.setTotal(lineTotal);

            invoiceItemRepository.save(ii);
        }

        // 4. Load lại nếu muốn chắc chắn đủ items
        return invoiceRepository.findById(saved.getInvoiceId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy hóa đơn sau khi tạo"));
    }


    public Page<InvoiceDTO> getInvoices(Pageable pageable) {
        return invoiceRepository.findAll(pageable)
                .map(InvoiceMapper::toDto);
    }


    public List<InvoiceDTO> getMyInvoices() {
        User currentUser = getCurrentUser();

        List<Invoice> invoices;

        if (isAdmin()) {
            invoices = invoiceRepository.findAllFetchOrderAndItems();
        } else {
            invoices = invoiceRepository.findByUserFetchOrderAndItems(currentUser);
        }

        return invoices.stream()
                .map(InvoiceMapper::toDto)
                .toList();
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("User không tồn tại"));
    }
    private boolean isAdmin() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;

        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    public InvoiceDTO getInvoiceById(Integer invoiceId) {

        if (isAdmin()) {
            Invoice invoice = invoiceRepository.findByIdFetchOrderAndItems(invoiceId)
                    .orElseThrow(() -> new BusinessException("Không tìm thấy hóa đơn với id: " + invoiceId));
            return InvoiceMapper.toDto(invoice);
        } else {
            User currentUser = getCurrentUser();
            Invoice invoice = invoiceRepository.findByIdAndUserFetchOrderAndItems(invoiceId, currentUser)
                    .orElseThrow(() -> new BusinessException("Không tìm thấy hóa đơn với id: " + invoiceId + " của bạn"));
            return InvoiceMapper.toDto(invoice);
        }
    }
}
