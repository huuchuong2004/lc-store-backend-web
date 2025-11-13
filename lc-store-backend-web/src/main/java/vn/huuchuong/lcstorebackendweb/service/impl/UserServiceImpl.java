package vn.huuchuong.lcstorebackendweb.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.huuchuong.lcstorebackendweb.base.BaseResponse;
import vn.huuchuong.lcstorebackendweb.entity.Role;
import vn.huuchuong.lcstorebackendweb.entity.User;
import vn.huuchuong.lcstorebackendweb.exception.BusinessException;
import vn.huuchuong.lcstorebackendweb.payload.request.user.ProfileUpdateRequest;
import vn.huuchuong.lcstorebackendweb.payload.request.user.SetAdminRequest;
import vn.huuchuong.lcstorebackendweb.payload.request.user.UpdateUserRequest;
import vn.huuchuong.lcstorebackendweb.payload.request.user.UserFilterRequest;
import vn.huuchuong.lcstorebackendweb.payload.response.LoadUserResponse;
import vn.huuchuong.lcstorebackendweb.payload.response.UpdateUserResponse;
import vn.huuchuong.lcstorebackendweb.repository.IUserRepository;
import vn.huuchuong.lcstorebackendweb.service.IUserService;
import vn.huuchuong.lcstorebackendweb.spectification.UserSpectification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j // tao log
public class UserServiceImpl implements IUserService {

    private final IUserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<User> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable).getContent();
    }

    @Override
    public LoadUserResponse loadUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("User not found"));

        return modelMapper.map(user, LoadUserResponse.class);
    }

    @Override
    public BaseResponse deleteByUsername(String username) {
        User user =userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("User not found"));
        userRepository.delete(user);
       BaseResponse baseResponse = new BaseResponse();
       baseResponse.setData(null);
       baseResponse.setMessage("User deleted successfully");
       return baseResponse;
    }

    @Override
    public BaseResponse<UpdateUserResponse> updateUser(String username, UpdateUserRequest req) {
        // 1) Tìm user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("User not found"));

        // 2) Cập nhật các trường có giá trị (partial update)
        if (req.getFirstName() != null && !req.getFirstName().isBlank()) {
            user.setFirstName(req.getFirstName().trim());
        }
        if (req.getLastName() != null && !req.getLastName().isBlank()) {
            user.setLastName(req.getLastName().trim());
        }
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            String email = req.getEmail().trim();
            // (tuỳ chọn) Check trùng email nếu bạn có hàm repo tương ứng
            // if (userRepository.existsByEmailAndIdNot(email, user.getId())) {
            //     throw new BusinessException("Email already in use");
            // }
            user.setEmail(email);
        }
        if (req.getPhone() != null && !req.getPhone().isBlank()) {
            user.setPhone(req.getPhone().trim());
        }

        // ✅ amount (BigDecimal) – chỉ cập nhật khi không null
        if (req.getAmount() != null) {
            // (tuỳ chọn) kiểm tra không âm
            // if (req.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            //     throw new BusinessException("Amount must be non-negative");
            // }
            user.setAmount(req.getAmount());
        }

        // ✅ isActive (Boolean) – chỉ cập nhật khi không null
        if (req.getIsActive() != null) {
            user.setIsActive(req.getIsActive());
        }

        // ✅ role (Enum) – CHỈ cho phép ADMIN cập nhật
        if (req.getRole() != null) {
            boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getAuthorities()
                    .stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
            if (!isAdmin) {
                throw new org.springframework.security.access.AccessDeniedException("Only ADMIN can update role");
            }
            user.setRole(req.getRole());
        }

        // 3) Lưu DB
        userRepository.save(user);

        // 4) Map sang response DTO
        UpdateUserResponse response = new UpdateUserResponse();
        modelMapper.map(user, response);

        // 5) Đóng gói BaseResponse
        BaseResponse<UpdateUserResponse> baseResponse = new BaseResponse<>();
        baseResponse.setData(response);
        baseResponse.setMessage("User updated successfully");
        return baseResponse;
    }

    @Override
    public BaseResponse setRoleAdmin(SetAdminRequest req) {
        // tim user
        User user = userRepository.findByUsername(req.getUsername()).orElseThrow(() -> new BusinessException("User not found"));

        if (user.getRole() == Role.ADMIN) {
            throw new BusinessException("User is already an admin");
        }

        // set lai role
        user.setRole(Role.ADMIN);

        // Luu lai thong tin user
        userRepository.save(user);

        BaseResponse<String> response = new BaseResponse<>(); //,String> de thong bao la base response se luu klieu du liee nao
        response.setData(null);
        response.setMessage("Role admin set successfully");
        return response;



    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> search(UserFilterRequest req, Pageable pageable) {
        String username  = req.getUsername();
        String email     = req.getEmail();
        String phone     = req.getPhone();
        String firstName = req.getFirstName();
        String lastName  = req.getLastName();

        Specification<User> spec = (root, query, cb) -> cb.conjunction();

        if (StringUtils.hasText(username)) {
            spec = spec.and(UserSpectification.hasUsername(username)); // sửa typo: UserSpecification
        }
        if (StringUtils.hasText(email)) {
            spec = spec.and(UserSpectification.hasEmail(email));
        }
        if (StringUtils.hasText(phone)) {
            spec = spec.and(UserSpectification.hasPhone(phone));
        }
        if (StringUtils.hasText(firstName)) {
            spec = spec.and(UserSpectification.hasFirstName(firstName));
        }
        if (StringUtils.hasText(lastName)) {
            spec = spec.and(UserSpectification.hasLastName(lastName));
        }

        return userRepository.findAll(spec, pageable);
    }

    @Override
    public void patch(UUID id, ProfileUpdateRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User không tồn tại"));

        if (StringUtils.hasText(req.getFirstName())) {
            user.setFirstName(req.getFirstName().trim());
        }
        if (StringUtils.hasText(req.getLastName())) {
            user.setLastName(req.getLastName().trim());
        }
        if (StringUtils.hasText(req.getEmail())) {
            user.setEmail(req.getEmail().trim());
        }
        if (StringUtils.hasText(req.getPhone())) {
            user.setPhone(req.getPhone().trim());
        }

        userRepository.save(user);
    }


}

