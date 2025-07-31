-- 数据库整理脚本：取消所有外键约束
-- 执行日期：2025-07-31

USE todo_db;

-- 1. 删除 projects 表的外键约束
ALTER TABLE projects DROP FOREIGN KEY projects_ibfk_1;

-- 2. 删除 todos 表的外键约束
ALTER TABLE todos DROP FOREIGN KEY todos_ibfk_1;
ALTER TABLE todos DROP FOREIGN KEY todos_ibfk_2;
ALTER TABLE todos DROP FOREIGN KEY todos_ibfk_3;

-- 3. 删除 project_users 表的外键约束
ALTER TABLE project_users DROP FOREIGN KEY project_users_ibfk_1;
ALTER TABLE project_users DROP FOREIGN KEY project_users_ibfk_2;

-- 4. 创建 notifications 表（无外键约束）
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    type VARCHAR(20) NOT NULL DEFAULT 'system' COMMENT '通知类型：system-系统通知，project-项目通知，personal-个人通知',
    priority VARCHAR(20) NOT NULL DEFAULT 'normal' COMMENT '优先级：low-低，normal-普通，high-高，urgent-紧急',
    sender_id BIGINT COMMENT '发送者ID（管理员ID）',
    sender_name VARCHAR(50) COMMENT '发送者姓名',
    receiver_id BIGINT COMMENT '接收者ID（为空表示全体用户）',
    project_id BIGINT COMMENT '项目ID（项目通知时使用）',
    is_read BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已读',
    is_pushed BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已推送',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    expire_time TIMESTAMP NULL COMMENT '过期时间',
    extra_data JSON COMMENT '额外数据（JSON格式）',
    PRIMARY KEY (id),
    INDEX idx_receiver_id (receiver_id),
    INDEX idx_project_id (project_id),
    INDEX idx_sender_id (sender_id),
    INDEX idx_create_time (create_time),
    INDEX idx_type_priority (type, priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知表';

-- 5. 验证外键约束删除结果
SELECT 
    TABLE_NAME, 
    COLUMN_NAME, 
    CONSTRAINT_NAME, 
    REFERENCED_TABLE_NAME, 
    REFERENCED_COLUMN_NAME 
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = 'todo_db' 
AND REFERENCED_TABLE_NAME IS NOT NULL;

-- 6. 显示所有表的状态
SHOW TABLES;

-- 完成提示
SELECT '数据库整理完成：所有外键约束已删除，notifications表已创建' AS status;
