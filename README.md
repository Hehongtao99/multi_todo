# 多人待办系统

一个基于Spring Boot + Vue.js + Electron的桌面待办应用，支持团队协作。

## 项目结构

```
multi_todo/
├── src/                     # Spring Boot后端
│   ├── main/
│   │   ├── java/
│   │   └── resources/
├── frontend/                # Vue.js前端
│   ├── src/
│   ├── electron/
│   └── package.json
├── pom.xml                  # Maven配置
├── database.sql             # 数据库初始化脚本
└── README.md
```

## 功能特性

- 🔐 用户登录注册（无参数校验）
- 👥 权限管理（普通用户/管理员）
- 📋 项目管理（创建、分配、查看）
- 👤 用户管理（仅管理员）
- ✅ 待办事项管理（待开发）
- 💻 桌面应用打包

## 技术栈

### 后端
- Spring Boot 2.7.14
- MyBatis Plus
- MySQL
- Lombok

### 前端
- Vue.js 3
- Element Plus
- Axios
- Vue Router
- Electron（桌面应用打包）

## 快速开始

### 1. 数据库配置

执行 `database.sql` 脚本创建数据库和表结构：

```sql
-- 连接到远程MySQL数据库
mysql -h 115.120.221.163 -u root -p123456

-- 执行初始化脚本
source database.sql
```

### 2. 后端启动

```bash
# 编译并启动Spring Boot应用
mvn spring-boot:run
```

后端将在 http://localhost:8080 启动

### 3. 前端开发

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端将在 http://localhost:5173 启动

### 4. 桌面应用打包

```bash
cd frontend

# 构建前端资源
npm run build

# 打包为桌面应用
npm run electron-build
```

## API接口

### 用户相关

#### 用户注册
- **POST** `/api/user/register`
- **参数**:
  ```json
  {
    "username": "用户名",
    "password": "密码",
    "confirmPassword": "确认密码"
  }
  ```

#### 用户登录
- **POST** `/api/user/login`
- **参数**:
  ```json
  {
    "username": "用户名",
    "password": "密码"
  }
  ```

#### 获取用户列表（仅管理员）
- **GET** `/api/user/list`
- **请求头**: `userId`, `userAuth`

### 项目相关

#### 创建项目（仅管理员）
- **POST** `/api/project/create`
- **请求头**: `userId`, `userAuth`
- **参数**:
  ```json
  {
    "projectName": "项目名称",
    "projectDescription": "项目描述"
  }
  ```

#### 获取项目列表
- **GET** `/api/project/list`
- **请求头**: `userId`, `userAuth`
- **说明**: 管理员获取所有项目，普通用户获取分配给自己的项目

#### 分配项目给多个用户（仅管理员）
- **PUT** `/api/project/assign`
- **请求头**: `userId`, `userAuth`
- **参数**:
  ```json
  {
    "projectId": "项目ID",
    "userIds": ["用户ID1", "用户ID2", "用户ID3"]
  }
  ```

## 数据库表结构

### users 用户表
- `id` - 用户ID（主键，自增）
- `username` - 用户名（唯一）
- `password` - 密码
- `auth` - 权限（user/admin）
- `created_time` - 创建时间
- `updated_time` - 更新时间

### projects 项目表
- `id` - 项目ID（主键，自增）
- `project_name` - 项目名称
- `project_description` - 项目描述
- `creator_id` - 创建人ID（外键关联users表）
- `created_time` - 创建时间
- `updated_time` - 更新时间

### project_users 项目用户关联表
- `id` - 关联ID（主键，自增）
- `project_id` - 项目ID（外键关联projects表）
- `user_id` - 用户ID（外键关联users表）
- `assigned_time` - 分配时间

## 开发规范

- 后端严格遵循MVC架构
- Controller层只负责请求接收和响应
- 业务逻辑全部在Service实现类中
- 使用Lombok减少模板代码
- 前端使用Vue3组合式API
- 统一的错误处理和返回格式

## 测试账号

- 管理员：admin / admin123
- 普通用户：user1 / 123456、user2 / 123456、user3 / 123456

## 功能说明

### 权限控制
- **管理员权限**：
  - 查看所有用户（不包括管理员）
  - 创建项目并分配给用户
  - 查看所有项目
  - 重新分配项目
  - 用户管理页面

- **普通用户权限**：
  - 只能查看分配给自己的项目
  - 无法创建或分配项目
  - 无法访问用户管理

### 项目管理流程
1. 管理员登录系统
2. 在项目管理页面创建新项目（只填写项目名称和描述）
3. 项目创建后，在项目列表中点击"分配用户"按钮
4. 选择要分配的用户（支持多选），一个项目可以分配给多个用户
5. 被分配用户登录后可在项目管理页面查看分配给自己的项目
6. 管理员可以随时重新分配项目的用户 