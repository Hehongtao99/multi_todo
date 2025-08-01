-- 个人信息字段升级脚本
-- 为users表添加真实姓名和头像字段

-- 添加真实姓名字段
ALTER TABLE users ADD COLUMN real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名';

-- 添加头像字段
ALTER TABLE users ADD COLUMN avatar VARCHAR(500) DEFAULT NULL COMMENT '头像URL';

-- 更新现有用户的姓名为用户名（可选）
UPDATE users SET real_name = username WHERE real_name IS NULL; 