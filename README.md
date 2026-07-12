# StuffPack - Minecraft Plugin

## 项目简介

A minecraft paper server plugin which is stuffed with stuff and is continually being stuffed more.

## 功能列表

### 1. 登录系统
- `/reg <密码> <确认密码>` - 注册账号
- `/login <密码>` - 登录账号
- `/cp <旧密码> <新密码> <确认密码>` - 修改密码

### 2. 金币系统
- `/coin balance` - 查看金币余额
- `/coin pay <玩家> <金额>` - 向玩家转账
- `/coin give <玩家> <金额>` - 管理员给予玩家金币（需权限）
- `/coin take <玩家> <金额>` - 管理员扣除玩家金币（需权限）
- `/coin balance <玩家>` - 管理员查看他人金币余额（需权限）

### 3. 领地系统
- 使用木杵左键设置点1，右键设置点2
- `/dom create <名称>` - 创建领地（消耗体积等量金币）
- `/dom` - 打开领地管理菜单
- `/dom tp <ID/编号>` - 传送到领地
- `/dom addadmin <领地ID> <玩家>` - 添加领地管理员
- `/dom name <领地ID> <新名称>` - 修改领地名称
- `/dom ad <领地名称>` - 管理员强制进入任意领地后台（需权限）
- `/dom ad delete <领地名称>` - 管理员强制删除任意领地（需权限）

### 4. 传送系统
- `/tpa <玩家>` - 请求传送到玩家身边
- `/tpahere <玩家>` - 请求玩家传送到你身边
- `/tpaccept` - 接受传送请求
- `/tpignore` - 忽略传送请求
- `/back` - 返回上一个位置

### 5. 留言系统
- `/offms <玩家> <内容>` - 给离线玩家留言，玩家上线后会收到鲜艳颜色的提示
- `/offms server <内容>` - 向服务器发送留言，所有玩家可查看
- `/rd` - 标记所有留言为已读
- `/msread` - 查看所有留言（显示已读/未读状态）

### 6. 图片系统
- `/pic give <URL> <是否可破坏>` - 获取自定义图片地图
- 小图片会自动放大至填满整张地图（128x128像素）

## 权限系统

| 权限节点 | 描述 | 默认值 |
| :--- | :--- | :--- |
| `stuffpack.admin` | 管理员权限 | OP |
| `stuffpack.coin.give` | 给予金币权限 | OP |
| `stuffpack.coin.take` | 扣除金币权限 | OP |
| `stuffpack.coin.balance.other` | 查看他人余额权限 | OP |
| `stuffpack.picture.use` | 使用图片系统权限 | OP |

## 技术栈

- Java 21
- Paper API 1.21.11-R0.1-SNAPSHOT
- Purpur 1.21.11 服务端
- Maven 构建工具

## 编译方法

```bash
mvn clean package
```

编译后的 JAR 文件位于 `target/stuffpack-1.0.0.jar`

## 安装方法

1. 将 `stuffpack-1.0.0.jar` 复制到服务器的 `plugins` 目录
2. 重启服务器

## 配置文件

插件会在首次运行时自动创建配置目录和数据文件：
- `plugins/StuffPack/domains.yml` - 领地数据
- `plugins/StuffPack/coins.yml` - 金币数据
- `plugins/StuffPack/login.yml` - 登录数据
- `plugins/StuffPack/messages.yml` - 留言数据
- `plugins/StuffPack/pictures.yml` - 图片数据

## 开发者

henrygo-mc

## 许可证

MIT License
