package vn.huuchuong.lcstorebackendweb.service;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.huuchuong.lcstorebackendweb.base.BaseResponse;
import vn.huuchuong.lcstorebackendweb.entity.User;
import vn.huuchuong.lcstorebackendweb.payload.request.user.ProfileUpdateRequest;
import vn.huuchuong.lcstorebackendweb.payload.request.user.SetAdminRequest;
import vn.huuchuong.lcstorebackendweb.payload.request.user.UpdateUserRequest;
import vn.huuchuong.lcstorebackendweb.payload.request.user.UserFilterRequest;
import vn.huuchuong.lcstorebackendweb.payload.response.LoadUserResponse;

import java.util.List;
import java.util.UUID;

public interface IUserService {

    List<User> getUsers(Pageable pageable);

    LoadUserResponse loadUser(String username);
    BaseResponse deleteByUsername(String username);

    BaseResponse updateUser(String username, UpdateUserRequest user);

    BaseResponse setRoleAdmin(@Valid SetAdminRequest req);

    Page<User> search(UserFilterRequest req, Pageable pageable);


    void patch(UUID id, @Valid ProfileUpdateRequest req);
}
