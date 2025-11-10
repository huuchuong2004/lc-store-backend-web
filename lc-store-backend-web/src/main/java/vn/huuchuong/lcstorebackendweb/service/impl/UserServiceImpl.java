package vn.huuchuong.lcstorebackendweb.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.huuchuong.lcstorebackendweb.entity.User;
import vn.huuchuong.lcstorebackendweb.repository.IUserRepository;
import vn.huuchuong.lcstorebackendweb.service.IUserService;

import java.util.List;
@Service
@RequiredArgsConstructor
@Slf4j // tao log
public class UserServiceImpl implements IUserService {

    private final IUserRepository userRepository;
    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }
}
