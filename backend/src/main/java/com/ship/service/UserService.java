package com.ship.service;

import com.ship.entity.User;
import com.ship.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final Map<String, User> tokenStore = new ConcurrentHashMap<>();

    @Autowired
    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    // Base64加密
    private String encodePassword(String password) {
        return Base64.getEncoder().encodeToString(password.getBytes());
    }

    // 登录
    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user != null && user.getPassword().equals(encodePassword(password))) {
            userMapper.updateLastLoginTime(user.getId());
            return user;
        }
        return null;
    }

    // 生成token
    public String generateToken(User user) {
        String token = UUID.randomUUID().toString();
        tokenStore.put(token, user);
        return token;
    }

    // 验证token
    public User validateToken(String token) {
        return tokenStore.get(token);
    }

    // 注销
    public void logout(String token) {
        tokenStore.remove(token);
    }

    // 注册
    public boolean register(User user) {
        if (userMapper.findByUsername(user.getUsername()) != null) {
            return false;
        }
        user.setPassword(encodePassword(user.getPassword()));
        user.setRole("user");
        return userMapper.insert(user) > 0;
    }

    // 获取用户信息
    public User getUserById(Integer id) {
        return userMapper.findById(id);
    }

    // 获取所有用户
    public List<User> getAllUsers() {
        return userMapper.findAll();
    }

    // 更新用户信息
    public boolean updateUserInfo(User user) {
        return userMapper.updateUserInfo(user) > 0;
    }

    // 修改密码
    public boolean changePassword(Integer userId, String oldPassword, String newPassword) {
        User user = userMapper.findById(userId);
        if (user != null && user.getPassword().equals(encodePassword(oldPassword))) {
            return userMapper.updatePassword(userId, encodePassword(newPassword)) > 0;
        }
        return false;
    }

    // 更新头像
    public boolean updateAvatar(Integer userId, String avatarUrl) {
        return userMapper.updateAvatar(userId, avatarUrl) > 0;
    }

    // 获取登录日志
    public List<Map<String, Object>> getLoginLogs(Integer userId) {
        return userMapper.getLoginLogs(userId);
    }

    // 获取收藏
    public List<Map<String, Object>> getFavorites(Integer userId) {
        return userMapper.getFavorites(userId);
    }

    // 添加收藏
    public boolean addFavorite(Integer userId, String type, Integer favoriteId, String note) {
        return userMapper.insertFavorite(userId, type, favoriteId, note) > 0;
    }

    // 删除收藏
    public boolean deleteFavorite(Integer favoriteId, Integer userId) {
        return userMapper.deleteFavorite(favoriteId, userId) > 0;
    }

    // 获取消息
    public List<Map<String, Object>> getNotifications(Integer userId) {
        return userMapper.getNotifications(userId);
    }

    // 标记已读
    public boolean markNotificationRead(Integer notificationId, Integer userId) {
        return userMapper.markNotificationRead(notificationId, userId) > 0;
    }

    // 全部已读
    public void markAllNotificationsRead(Integer userId) {
        userMapper.markAllNotificationsRead(userId);
    }
}