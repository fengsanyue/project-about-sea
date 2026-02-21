package com.ship.controller;

import com.ship.entity.User;
import com.ship.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ship.entity.User;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:5173")
public class UserProfileController {

    @Autowired
    private UserService userService;

    // 获取用户信息
    @GetMapping("/info")
    public Map<String, Object> getUserInfo(@RequestAttribute(value = "currentUser", required = false) User currentUser) {
        System.out.println("========== 获取用户信息 ==========");
        System.out.println("当前用户: " + (currentUser != null ? currentUser.getUsername() : "null"));

        Map<String, Object> result = new HashMap<>();

        try {
            // 如果 currentUser 为 null，尝试从 token 获取
            if (currentUser == null) {
                result.put("code", 401);
                result.put("message", "用户未登录");
                return result;
            }

            // 从数据库获取最新用户信息
            User user = userService.getUserById(currentUser.getId());
            System.out.println("数据库查询结果: " + user);

            if (user == null) {
                result.put("code", 404);
                result.put("message", "用户不存在");
                return result;
            }

            // 移除敏感信息
            user.setPassword(null);

            result.put("code", 200);
            result.put("data", user);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("message", "服务器错误: " + e.getMessage());
        }

        System.out.println("返回结果: " + result);
        return result;
    }

    // 修改密码
    @PostMapping("/change-password")
    public Map<String, Object> changePassword(@RequestBody Map<String, String> request,
                                              @RequestAttribute("currentUser") User currentUser) {
        System.out.println("========== 开始修改密码 ==========");
        System.out.println("当前用户ID: " + currentUser.getId());

        Map<String, Object> result = new HashMap<>();

        try {
            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");

            System.out.println("原密码: " + oldPassword);
            System.out.println("新密码: " + newPassword);

            // 调用service修改密码
            boolean success = userService.changePassword(currentUser.getId(), oldPassword, newPassword);

            if (success) {
                result.put("code", 200);
                result.put("message", "密码修改成功，请重新登录");
            } else {
                result.put("code", 400);
                result.put("message", "原密码错误");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("message", "服务器错误: " + e.getMessage());
        }

        return result;
    }

    // 上传头像
    @PostMapping("/avatar")
    public Map<String, Object> uploadAvatar(@RequestParam("file") MultipartFile file,
                                            @RequestAttribute("currentUser") User currentUser) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                result.put("code", 400);
                result.put("message", "文件不能为空");
                return result;
            }

            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                result.put("code", 400);
                result.put("message", "只能上传JPG或PNG图片");
                return result;
            }

            // 创建上传目录
            String uploadDir = System.getProperty("user.dir") + "/uploads/avatars/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成文件名
            String fileName = currentUser.getId() + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            // 保存文件
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 生成访问URL
            String avatarUrl = "/uploads/avatars/" + fileName;

            // 更新数据库
            userService.updateAvatar(currentUser.getId(), avatarUrl);

            result.put("code", 200);
            result.put("data", avatarUrl);
            result.put("message", "上传成功");

        } catch (IOException e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("message", "上传失败: " + e.getMessage());
        }
        return result;
    }

    // 获取登录日志
    @GetMapping("/login-logs")
    public Map<String, Object> getLoginLogs(@RequestAttribute("currentUser") User currentUser) {
        List<Map<String, Object>> logs = userService.getLoginLogs(currentUser.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", logs);
        return result;
    }

    // 获取收藏列表
    @GetMapping("/favorites")
    public Map<String, Object> getFavorites(@RequestAttribute("currentUser") User currentUser) {
        List<Map<String, Object>> favorites = userService.getFavorites(currentUser.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", favorites);
        return result;
    }

    // 添加收藏
    @PostMapping("/favorites")
    public Map<String, Object> addFavorite(@RequestBody Map<String, Object> request,
                                           @RequestAttribute("currentUser") User currentUser) {
        String type = (String) request.get("type");
        Integer favoriteId = (Integer) request.get("favoriteId");
        String note = (String) request.get("note");

        boolean success = userService.addFavorite(currentUser.getId(), type, favoriteId, note);

        Map<String, Object> result = new HashMap<>();
        result.put("code", success ? 200 : 400);
        result.put("message", success ? "收藏成功" : "收藏失败");
        return result;
    }

    // 删除收藏
    @DeleteMapping("/favorites/{id}")
    public Map<String, Object> deleteFavorite(@PathVariable Integer id,
                                              @RequestAttribute("currentUser") User currentUser) {
        boolean success = userService.deleteFavorite(id, currentUser.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("code", success ? 200 : 400);
        result.put("message", success ? "取消收藏成功" : "操作失败");
        return result;
    }

    // 获取消息列表
    @GetMapping("/notifications")
    public Map<String, Object> getNotifications(@RequestAttribute("currentUser") User currentUser) {
        List<Map<String, Object>> notifications = userService.getNotifications(currentUser.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", notifications);
        return result;
    }

    // 标记消息已读
    @PutMapping("/notifications/{id}/read")
    public Map<String, Object> markNotificationRead(@PathVariable Integer id,
                                                    @RequestAttribute("currentUser") User currentUser) {
        boolean success = userService.markNotificationRead(id, currentUser.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("code", success ? 200 : 400);
        result.put("message", success ? "已读" : "操作失败");
        return result;
    }

    // 标记所有消息已读
    @PutMapping("/notifications/read-all")
    public Map<String, Object> markAllNotificationsRead(@RequestAttribute("currentUser") User currentUser) {
        userService.markAllNotificationsRead(currentUser.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "全部已读");
        return result;
    }
}
