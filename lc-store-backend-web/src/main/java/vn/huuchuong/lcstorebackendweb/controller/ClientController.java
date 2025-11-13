package vn.huuchuong.lcstorebackendweb.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import vn.huuchuong.lcstorebackendweb.base.BaseEntity;
import vn.huuchuong.lcstorebackendweb.base.BaseResponse;
import vn.huuchuong.lcstorebackendweb.entity.User;
import vn.huuchuong.lcstorebackendweb.payload.request.user.ProfileUpdateRequest;
import vn.huuchuong.lcstorebackendweb.payload.request.user.SetAdminRequest;
import vn.huuchuong.lcstorebackendweb.payload.request.user.UpdateUserRequest;
import vn.huuchuong.lcstorebackendweb.payload.request.user.UserFilterRequest;
import vn.huuchuong.lcstorebackendweb.payload.response.LoadUserResponse;
import vn.huuchuong.lcstorebackendweb.service.IUserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class ClientController {

    private final IUserService userService;


    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<List<User>>>  getUsers(@PageableDefault(size = 10, sort = "username", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(new BaseResponse<>(userService.getUsers(pageable), "Lay List User Thanh Cong"));
    }


    @PreAuthorize("#username == authentication.name or hasAuthority('ROLE_ADMIN')")
    @GetMapping("/{username}")
    public ResponseEntity<BaseResponse<LoadUserResponse>> loadUser(@P("username") @PathVariable String username) {
        return ResponseEntity.ok(new BaseResponse<>(userService.loadUser(username),"Day la thong tin user"));
    }

    @DeleteMapping("/{username}")
    @PreAuthorize("#username == authentication.name or hasAuthority('ROLE_ADMIN')") // Neu user la nguoi goi thi tu dong load thongtin ser , con ko chi co admin
    public ResponseEntity<BaseResponse> deleteUser(@PathVariable String username) {
        return ResponseEntity.ok(new BaseResponse<>(userService.deleteByUsername(username),"Xoa Thanh Cong User"+username));
    }

    @PutMapping("/{username}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse> updateUser(@PathVariable String username, @RequestBody @Valid  UpdateUserRequest user) {
        return ResponseEntity.ok(new BaseResponse<>(userService.updateUser(username,user),"Update Thanh Cong User"+username));
    }
    @PatchMapping("/{id}")
    @PreAuthorize("@authz.isSelf(#id, authentication) || hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<Void>> patch(
            @PathVariable UUID id,
            @Valid @RequestBody ProfileUpdateRequest req
    ) {
        userService.patch(id, req);
        return ResponseEntity.ok(new BaseResponse<>(null, "Cập nhật thông tin thành công"));
    }



    @PostMapping("/set-admin")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse> setAdmin(@RequestBody @Valid SetAdminRequest req) {

        return ResponseEntity.ok(new BaseResponse<>(userService.setRoleAdmin(req),"Set Role Admin Thanh Cong cho User"+req.getUsername()));

    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<Page<User>>> search(
            @Valid @ModelAttribute UserFilterRequest req,
            @PageableDefault(size = 10, sort = "username", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<User> page = userService.search(req, pageable);
        return ResponseEntity.ok(new BaseResponse<>(page, "Lấy danh sách thành công"));
    }










}
