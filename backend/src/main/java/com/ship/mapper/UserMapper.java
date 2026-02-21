package com.ship.mapper;

import com.ship.entity.User;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;
import com.ship.entity.User;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(@Param("id") Integer id);

    @Insert("INSERT INTO user(username, password, real_name, email, role, avatar) " +
            "VALUES(#{username}, #{password}, #{realName}, #{email}, #{role}, #{avatar})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE user SET last_login_time = NOW() WHERE id = #{id}")
    int updateLastLoginTime(@Param("id") Integer id);

    @Select("SELECT * FROM user ORDER BY created_time DESC")
    List<User> findAll();

    @Update("UPDATE user SET real_name=#{realName}, email=#{email}, phone=#{phone}, department=#{department}, position=#{position} WHERE id=#{id}")
    int updateUserInfo(User user);

    @Update("UPDATE user SET password=#{password} WHERE id=#{id}")
    int updatePassword(@Param("id") Integer id, @Param("password") String password);

    @Update("UPDATE user SET avatar=#{avatar} WHERE id=#{id}")
    int updateAvatar(@Param("id") Integer id, @Param("avatar") String avatar);

    @Select("SELECT * FROM user_login_log WHERE user_id=#{userId} ORDER BY login_time DESC LIMIT 20")
    List<Map<String, Object>> getLoginLogs(@Param("userId") Integer userId);

    @Select("SELECT f.*, k.fault_type, k.fault_level FROM user_favorite f " +
            "LEFT JOIN fault_knowledge k ON f.favorite_id = k.id " +
            "WHERE f.user_id=#{userId} AND f.favorite_type='fault' " +
            "ORDER BY f.created_time DESC")
    List<Map<String, Object>> getFavorites(@Param("userId") Integer userId);

    @Insert("INSERT INTO user_favorite(user_id, favorite_type, favorite_id, note) VALUES(#{userId}, #{type}, #{favoriteId}, #{note})")
    int insertFavorite(@Param("userId") Integer userId, @Param("type") String type,
                       @Param("favoriteId") Integer favoriteId, @Param("note") String note);

    @Delete("DELETE FROM user_favorite WHERE id=#{id} AND user_id=#{userId}")
    int deleteFavorite(@Param("id") Integer id, @Param("userId") Integer userId);

    @Select("SELECT * FROM user_notification WHERE user_id=#{userId} ORDER BY created_time DESC LIMIT 50")
    List<Map<String, Object>> getNotifications(@Param("userId") Integer userId);

    @Update("UPDATE user_notification SET is_read=1, read_time=NOW() WHERE id=#{id} AND user_id=#{userId}")
    int markNotificationRead(@Param("id") Integer id, @Param("userId") Integer userId);

    @Update("UPDATE user_notification SET is_read=1, read_time=NOW() WHERE user_id=#{userId} AND is_read=0")
    void markAllNotificationsRead(@Param("userId") Integer userId);


}