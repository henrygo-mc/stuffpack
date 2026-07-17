# StuffPack - Minecraft Plugin

---

## 中文 / Chinese

### 项目简介

一个装满东西的 Minecraft Paper 服务器插件，并且还在持续添加更多内容。

### 功能列表

#### 1. 登录系统
- `/reg <密码> <确认密码>` - 注册账号
- `/login <密码>` - 登录账号
- `/cp <旧密码> <新密码> <确认密码>` - 修改密码

#### 2. 金币系统
- `/coin balance` - 查看金币余额
- `/coin pay <玩家> <金额>` - 向玩家转账
- `/coin give <玩家> <金额>` - 管理员给予玩家金币（需权限）
- `/coin take <玩家> <金额>` - 管理员扣除玩家金币（需权限）
- `/coin balance <玩家>` - 管理员查看他人金币余额（需权限）

#### 3. 领地系统
- 使用木杵左键设置点1，右键设置点2
- `/dom create <名称>` - 创建领地（消耗体积等量金币）
- `/dom` - 打开领地管理菜单
- `/dom tp <ID/编号>` - 传送到领地
- `/dom addadmin <领地ID> <玩家>` - 添加领地管理员
- `/dom name <领地ID> <新名称>` - 修改领地名称
- `/dom move <领地ID/名称> <x偏移> <y偏移> <z偏移>` - 移动领地（如 x+1 y-1 z+0）
- `/dom ad <领地名称>` - 管理员强制进入任意领地后台（需权限）
- `/dom ad delete <领地名称>` - 管理员强制删除任意领地（需权限）

#### 4. 传送系统
- `/tpa <玩家>` - 请求传送到玩家身边
- `/tpahere <玩家>` - 请求玩家传送到你身边
- `/tpaccept` - 接受传送请求
- `/tpignore` - 忽略传送请求
- `/back` - 返回上一个位置

#### 5. 留言系统
- `/offms <玩家> <内容>` - 给离线玩家留言，玩家上线后会收到鲜艳颜色的提示
- `/offms server <内容>` - 向服务器发送留言，所有玩家可查看
- `/rd` - 标记所有留言为已读
- `/msread` - 查看所有留言（显示已读/未读状态）

#### 6. 图片系统
- `/pic give <URL> <是否可破坏>` - 获取自定义图片地图
- 小图片会自动放大至填满整张地图（128x128像素）

#### 7. 假玩家系统fakeplayer spawn <名称> [皮肤URL]
- `/fakeplayer spawn <名称> [皮肤URL]` - 生成假玩家（需权限）
- `/fakeplayer remove <名称>` - 移除假玩家（需权限）
- `/fakeplayer list` - 列出所有假玩家（需权限）

#### 8. 私人门系统
- `/privatedoor [数量]` - 获取私人门（需OP权限）
- 私人门只有放置者可以打开，其他人无法开启
- 私人门只有放置者或管理员可以破坏

### 权限系统

| 权限节点 | 描述 | 默认值 |
| :--- | :--- | :--- |
| `stuffpack.admin` | 管理员权限 | OP |
| `stuffpack.coin.give` | 给予金币权限 | OP |
| `stuffpack.coin.take` | 扣除金币权限 | OP |
| `stuffpack.coin.balance.other` | 查看他人余额权限 | OP |
| `stuffpack.picture.use` | 使用图片系统权限 | OP |
| `stuffpack.fakeplayer` | 使用假玩家系统权限 | OP |
| `stuffpack.privatedoor` | 使用私人门系统权限 | OP |

### 技术栈

- Java 21
- Paper API 1.21.11-R0.1-SNAPSHOT
- Purpur 1.21.11 服务端
- Maven 构建工具

### 编译方法

```bash
mvn clean package
```

编译后的 JAR 文件位于 `target/stuffpack-1.0.0.jar`

### 安装方法

1. 将 `stuffpack-1.0.0.jar` 复制到服务器的 `plugins` 目录
2. 重启服务器

### 配置文件

插件会在首次运行时自动创建配置目录和数据文件：
- `plugins/StuffPack/domains.yml` - 领地数据
- `plugins/StuffPack/coins.yml` - 金币数据
- `plugins/StuffPack/login.yml` - 登录数据
- `plugins/StuffPack/messages.yml` - 留言数据
- `plugins/StuffPack/pictures.yml` - 图片数据

### 开发者

HenryGo-mc

### 许可证

MIT License

---

## English / 英文

### Project Description

A minecraft paper server plugin which is stuffed with stuff and is continually being stuffed more.

### Features

#### 1. Login System
- `/reg <password> <confirm password>` - Register an account
- `/login <password>` - Login to your account
- `/cp <old password> <new password> <confirm password>` - Change password

#### 2. Coin Economy System
- `/coin balance` - Check coin balance
- `/coin pay <player> <amount>` - Transfer coins to another player
- `/coin give <player> <amount>` - Admin: Give coins to player (requires permission)
- `/coin take <player> <amount>` - Admin: Take coins from player (requires permission)
- `/coin balance <player>` - Admin: Check another player's balance (requires permission)

#### 3. Land Claim System
- Use a wooden hoe to set point 1 (left-click) and point 2 (right-click)
- `/dom create <name>` - Create a territory (costs coins equal to volume)
- `/dom` - Open territory management menu
- `/dom tp <ID/number>` - Teleport to a territory
- `/dom addadmin <territory ID> <player>` - Add territory admin
- `/dom name <territory ID> <new name>` - Rename territory
- `/dom move <territory ID/name> <x offset> <y offset> <z offset>` - Move territory (e.g., x+1 y-1 z+0)
- `/dom ad <territory name>` - Admin: Force access to any territory (requires permission)
- `/dom ad delete <territory name>` - Admin: Force delete any territory (requires permission)

#### 4. Teleportation System
- `/tpa <player>` - Request to teleport to a player
- `/tpahere <player>` - Request a player to teleport to you
- `/tpaccept` - Accept teleport request
- `/tpignore` - Ignore teleport request
- `/back` - Return to previous location

#### 5. Offline Messaging System
- `/offms <player> <message>` - Leave a message for offline players, they will receive a colorful notification when online
- `/offms server <message>` - Send a message to the server, all players can view it
- `/rd` - Mark all messages as read
- `/msread` - View all messages (shows read/unread status)

#### 6. Picture System
- `/pic give <URL> <breakable>` - Get a custom image map
- Small images will be automatically enlarged to fill the entire map (128x128 pixels)

#### 7. Fake Player System
- `/fakeplayer spawn <name> [skin URL]` - Spawn a fake player (requires permission)
- `/fakeplayer remove <name>` - Remove a fake player (requires permission)
- `/fakeplayer list` - List all fake players (requires permission)

#### 8. Private Door System
- `/privatedoor [amount]` - Get private doors (requires OP)
- Only the placer can open private doors
- Only the placer or admins can break private doors

### Permission System

| Permission Node | Description | Default |
| :--- | :--- | :--- |
| `stuffpack.admin` | Administrator permission | OP |
| `stuffpack.coin.give` | Give coins permission | OP |
| `stuffpack.coin.take` | Take coins permission | OP |
| `stuffpack.coin.balance.other` | View other's balance permission | OP |
| `stuffpack.picture.use` | Use picture system permission | OP |
| `stuffpack.fakeplayer` | Use fake player system permission | OP |
| `stuffpack.privatedoor` | Use private door system permission | OP |

### Tech Stack

- Java 21
- Paper API 1.21.11-R0.1-SNAPSHOT
- Purpur 1.21.11 Server
- Maven Build Tool

### Compilation

```bash
mvn clean package
```

Compiled JAR file is located at `target/stuffpack-1.0.0.jar`

### Installation

1. Copy `stuffpack-1.0.0.jar` to the server's `plugins` directory
2. Restart the server

### Configuration Files

The plugin will automatically create configuration directories and data files on first run:
- `plugins/StuffPack/domains.yml` - Territory data
- `plugins/StuffPack/coins.yml` - Coin data
- `plugins/StuffPack/login.yml` - Login data
- `plugins/StuffPack/messages.yml` - Message data
- `plugins/StuffPack/pictures.yml` - Picture data

### Developer

HenryGo-mc

### License

MIT License
