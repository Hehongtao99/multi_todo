-- 创建数据库
CREATE DATABASE IF NOT EXISTS todo_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE todo_db;

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    auth VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '权限：user-普通用户，admin-管理员',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 创建项目表
CREATE TABLE IF NOT EXISTS projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '项目ID',
    project_name VARCHAR(100) NOT NULL COMMENT '项目名称',
    project_description TEXT COMMENT '项目描述',
    creator_id BIGINT NOT NULL COMMENT '创建人ID',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (creator_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

-- 创建项目用户关联表（支持一个项目分配给多个用户）
CREATE TABLE IF NOT EXISTS project_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    assigned_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_project_user (project_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目用户关联表';

-- 插入测试数据
INSERT INTO users (username, password, auth) VALUES 
('admin', 'admin123', 'admin'),
('user1', '123456', 'user'),
('user2', '123456', 'user'),
('user3', '123456', 'user');

-- 插入测试项目数据
INSERT INTO projects (project_name, project_description, creator_id) VALUES 
('网站重构项目', '对公司官网进行全面重构，提升用户体验', 1),
('移动端开发', '开发公司移动端应用，支持iOS和Android', 1),
('数据分析系统', '构建数据分析和报表系统', 1);

-- 创建待办事项表
CREATE TABLE IF NOT EXISTS todos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '待办ID',
    title VARCHAR(200) NOT NULL COMMENT '待办标题',
    description TEXT COMMENT '待办描述',
    status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending-待处理，in_progress-进行中，completed-已完成',
    priority VARCHAR(20) NOT NULL DEFAULT 'medium' COMMENT '优先级：low-低，medium-中，high-高',
    project_id BIGINT NOT NULL COMMENT '所属项目ID',
    assignee_id BIGINT COMMENT '分配给的用户ID',
    creator_id BIGINT NOT NULL COMMENT '创建人ID',
    due_date TIMESTAMP NULL COMMENT '截止日期',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (assignee_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (creator_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='待办事项表';

-- 插入项目用户关联数据（一个项目可分配给多个用户）
INSERT INTO project_users (project_id, user_id) VALUES
(1, 2), -- 网站重构项目分配给user1
(1, 3), -- 网站重构项目也分配给user2
(2, 3), -- 移动端开发分配给user2
(2, 4), -- 移动端开发也分配给user3
(3, 4); -- 数据分析系统分配给user3

-- 插入测试待办数据
INSERT INTO todos (title, description, status, priority, project_id, assignee_id, creator_id, due_date) VALUES
('设计首页原型', '设计网站首页的UI原型图，包含导航、轮播图、产品展示等模块', 'pending', 'high', 1, 2, 1, '2024-02-15 18:00:00'),
('开发用户登录功能', '实现用户登录、注册、密码重置等功能', 'in_progress', 'high', 1, 3, 1, '2024-02-20 18:00:00'),
('编写API文档', '编写后端API接口文档，包含所有接口的参数说明', 'pending', 'medium', 1, 2, 1, '2024-02-25 18:00:00'),
('iOS端界面开发', '开发iOS应用的主要界面和交互逻辑', 'pending', 'high', 2, 3, 1, '2024-03-01 18:00:00'),
('Android端功能测试', '对Android应用进行功能测试和性能优化', 'pending', 'medium', 2, 4, 1, '2024-03-05 18:00:00'),
('数据库设计', '设计数据分析系统的数据库结构', 'completed', 'high', 3, 4, 1, '2024-02-10 18:00:00');