<div align="center">

[![License](https://img.shields.io/badge/License-GPL%203.0-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Spring%20Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.2-4FC08D.svg)](https://vuejs.org/)
[![JavaWeb](https://img.shields.io/badge/JavaWeb-AI%2B-orange.svg)]()

</div>

   <h1 align="center">Tlias 智能教学管理系统</h1>
   <p align="center">🎨 科技风格 UI · 前后端分离 · 数据可视化</p>

<br/>

<p align="center">
  <a href="#项目介绍">📋 项目介绍</a> •
  <a href="#演示">🎬 演示</a> •
  <a href="#使用说明">🚀 使用说明</a> •
  <a href="#友情链接">🔗 友情链接</a>
</p>

<br/>

<details>
<summary><h1 id="项目介绍">📋 项目介绍</h1></summary>

1. `黑马24年 JavaWeb + AI 教程`(见`友情链接`部分)的 Tlias 管理系统前后端源码

2. 在教程基础上改进(包括 AI 辅助优化)
    * 最大不同在 UI: **界面全面优化为科技风格**(主打花里胡哨)
    * **补充教程没有实现的功能**:
      - 修改密码
      - 统计学生总数/未分配班级学生/班级总数/班级开课状态(在读/结课/未开)
      - 统计操作总数/各种操作数量及平均响应时间
      - 统计登录人数/登录失败用户排名/登录成功和失败比例
      - 404 界面(登录后，浏览器地址栏输入错误路径，可见该界面)
    * **优化父工程、几个子模块的 pom.xml 结构**，可以在父工程的 pom.xml **更加清晰方便地管理整个项目的依赖**
    * **优化全局异常处理器**，能够处理多种异常(业务异常、数据重复、数据访问、系统异常)、返回合适的 message
    * **业务类接口补充 `@Validated` 参数检验**,比如 `@NotNull`, `@Positive`, `@NotEmpty` 等注解，简化参数检验
    * **业务实现类补充额外参数检验、业务异常抛出**
    * `Controller` 返回对象 `Result` 使用泛型，且实现了 `Serializable` 接口(教程只是简单定义)
    * **统计数据使用专门的 `View Object` 包装返回**,教程1个对象封装2个链表，或者直接返回 Map，而这里1个对象只封装2个对应字段、再创建对象链表，适用性更好

3. **涉及知识点**

    <details>
    <summary>📚 展开查看详细知识点</summary>

    **后端 (SpringBoot3 + MyBatis)**

    | 知识点 | 版本 | 项目中的应用 |
    |--------|------|-------------|
    | **Java** | JDK 17+ | 开发语言，项目使用 JDK 21 构建 |
    | **Spring Boot** | 3.5 | 启动类 `@SpringBootApplication(scanBasePackages)` 扫描跨模块 Bean |
    | **Maven** | 3.6+ | 多模块聚合 `tlias-pojo` / `tlias-util` / `tlias-business`，统一版本管理 |
    | **MyBatis** | 3.0 | 简单 SQL 用 `@Select/@Insert`，动态条件查询用 XML |
    | **MySQL** | 8.0 | 关系型数据库，存储业务数据 |
    | **PageHelper** | 1.4 | `PageHelper.startPage()` + `Page<T>` 强转实现分页 |
    | **JWT** | 0.9 | `JwtUtils` 生成/解析 Token，`TokenInterceptor` 拦截校验 |
    | **阿里云 OSS** | 3.18 | 配置文件存储，实现头像/文件上传 |
    | **Lombok** | 1.18 | `@Data/@Slf4j` 减少 getter/setter/日志样板代码 |
    | **三层架构** | — | `Controller` → `Service` → `Mapper`，职责分离 |
    | **RESTful API** | — | `@GetMapping` / `@PostMapping` / `@PutMapping` / `@DeleteMapping` 实现 CRUD |
    | **统一响应封装** | — | `Result<T>` 泛型类统一返回 `{code, msg, data}` |
    | **数据对象分层** | — | `POJO`(实体) / `DTO`(入参) / `VO`(出参) / `PageResult`(分页) |
    | **ThreadLocal** | — | `CurrentHolder` 线程隔离存储当前登录用户 ID |
    | **拦截器** | — | `TokenInterceptor` 拦截所有请求，校验 JWT 后放行 |
    | **全局异常处理** | — | `@RestControllerAdvice` 统一捕获业务异常/数据重复/数据库异常/系统异常 |
    | **AOP 日志记录** | — | 自定义 `@LogOperation` 注解 + `@Aspect` 环绕通知记录操作日志 |
    | **声明式事务** | — | `@Transactional(readOnly=true/rollbackFor)` 控制事务边界 |
    | **参数校验** | — | `@Validated` + `@NotNull/@NotEmpty/@Positive` 简化入参校验 |

    **前端 (Vue3 + Vite3)**

    | 知识点 | 版本 | 项目中的应用 |
    |--------|------|-------------|
    | **Node.js** | 18+ | 项目使用 Node.js 22 构建 |
    | **Vue** | 3.2 | `<script setup>` + `ref/reactive` 响应式开发 |
    | **Vue Router** | 4.1 | `createRouter` 配置路由，`beforeEach` 路由守卫拦截未登录 |
    | **Vite** | 3.0 | 构建工具，`vite.config.js` 配置 `/api` 代理到后端解决跨域 |
    | **Axios** | 1.7 | 创建实例 + 请求拦截器(自动带 token) + 响应拦截器(统一错误处理) |
    | **Element Plus** | 2.4 | 表格、表单、对话框、消息提示等 UI 组件 |
    | **Echarts** | 6.0 | 员工/学员/班级/操作/登录等数据统计可视化图表 |
    | **组件化开发** | — | 按功能模块划分视图组件 (`views/emp`, `views/clazz` 等) |

    </details>

4. **功能模块**

    <details>
    <summary>🧩 展开查看系统模块</summary>

    | 模块 | 功能说明 |
    |------|---------|
    | **登录认证** | JWT Token 登录、路由守卫拦截、修改密码 |
    | **部门管理** | 部门的增删改查 |
    | **员工管理** | 员工信息的增删改查、条件分页查询、头像上传 |
    | **学员管理** | 学员信息的增删改查、条件分页查询 |
    | **班级管理** | 班级信息的增删改查、班级学员分配 |
    | **数据统计** | 员工/学员/班级数量统计、操作日志统计、登录日志统计、可视化图表展示 |
    | **系统管理** | 操作日志查询、登录日志查询 |

    </details>

</details>


<details>
<summary><h1 id="演示">🎬 演示</h1></summary>

* 登录界面展示

   ![登录界面展示](/assets/demo/Tlias-Login.png "登录界面展示")

* 内部界面一览

   <u>[仓库内视频入口](/assets/demo/Tlias-Demo.mp4)</u>

</details>


<details>
<summary><h1 id="使用说明">🚀 使用说明</h1></summary>

1. **本地跑通步骤**
   1. **导入源码**
      * IDEA 新建 `Empty Project`
      * 在<u>[ Release 界面](https://github.com/existed-name/Tlias-System-TechStyle/releases)</u>下载源码压缩包 `Source code.zip`
      * 把源码解压到该项目目录
      * 手动右键项目根目录的 pom.xml → `Add as maven project` → 加载依赖
   2. **配置数据源**
      * IDEA 右侧 Database 面板 → 点击 `+` 新建 `Data Source`,填写 `username`(root) 和 `password`(123456)
      * 来到 `/assets/database/tlias.sql` → 选中全部 → `Ctrl + Enter` 运行
      * 这样就把数据迁移过来了
   3. **修改项目配置**
      * 来到 `tlias-business` 模块的 `src/main/resources/application.yaml`
      * 在数据源配置(`datasource`)把 `username`, `password` 改成自己的
      * 最下面的 `aliyun` 部分，把 `bucketName`, `region` 改成自己的对象存储服务的信息，`endpoint` 也要把 `beijing` 改成自己的 `region`
   4. **启动后端**: 在 `tlias-business` 模块找到启动类并运行
   5. **后端接口测试**
      * 电脑浏览器访问 `localhost:8081` 看看有没有 JSON 数据
      * ApiFox/Postman 等等工具，先访问 `localhost:8081/login`，在 `Body` 请求体里面的 `JSON`，填入登录用户和密码(可见 `LoginController`)
      
         ```json
         {
            "username": "linchong",
            "password": "123456"
         }
         ```
        
      * 登录拿到 token，然后访问 `localhost:8081/depts`(或者其他路径),在 `Headers` 请求头添加参数 `token`,把刚刚复制到的 token 粘贴过来，就有登录状态了
   6. **启动前端**
      * 来到项目的 `vue-tlias-management` 文件夹
      * IDEA 右键该文件夹 → `Open In` → `Terminal` → `npm install` 安装依赖
      * 进入 `package.json` → `"dev": "vite",` 那里点击旁边的绿色小箭头，启动前端
      * 从 Terminal 给出的 `localhost:5173`(类似链接)进去到浏览器，就可以看到前端界面了

</details>


<details>
<summary><h1 id="友情链接">🔗 友情链接</h1></summary>

1. **在线讲义地址**: <u>https://heuqqdmbyk.feishu.cn/wiki/QPxOw66KlikKhKk1TOlcqxHXnDd</u>
2. **视频教程地址**: <u>https://www.bilibili.com/video/BV1yGydYEE3H/?spm_id_from=333.1391.0.0&vd_source=3c1b635a04921aed8ee939defc8bdc1b</u>
3. **课程资料**: 微信小程序 → `黑马程序员教程库` → 找到当前课程，进入百度网盘小程序 → 主要用到`资料`里面的原型、接口文档
4. 以及电脑浏览器搜索 `Tlias` 就可以找到不少内容
5. **个人博客**(持续更新)
    * <u>[Linux部署SpringBoot项目](https://github.com/existed-name/Personal-Blogs/blob/main/%E6%8A%80%E6%9C%AF%E6%95%99%E7%A8%8B%E7%B1%BB/%E9%A1%B9%E7%9B%AE%E9%83%A8%E7%BD%B2/Linux%E9%83%A8%E7%BD%B2SpringBoot%E9%A1%B9%E7%9B%AE.md)</u>
    * <u>[SpringBoot项目部署:从裸机部署到Docker容器化](https://github.com/existed-name/Personal-Blogs/blob/main/%E6%8A%80%E6%9C%AF%E6%95%99%E7%A8%8B%E7%B1%BB/%E9%A1%B9%E7%9B%AE%E9%83%A8%E7%BD%B2/SpringBoot%E9%A1%B9%E7%9B%AE%E9%83%A8%E7%BD%B2-%E4%BB%8E%E8%A3%B8%E6%9C%BA%E9%83%A8%E7%BD%B2%E5%88%B0Docker%E5%AE%B9%E5%99%A8%E5%8C%96.md)</u>
    * ↑ 不过我因为实验课下载过 VirtualBox + Debian，就没有用教程的 VMware + CentOS
    * <u>[个人博客仓库](https://github.com/existed-name/Personal-Blogs/tree/main)</u>
    * <u>[知乎博客](https://www.zhihu.com/people/14-94-73-44/posts)</u>

</details>