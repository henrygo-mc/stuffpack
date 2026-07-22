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

#### 7. 假玩家系统
- `/fakeplayer spawn <名称> [皮肤URL]` - 生成假玩家（需权限）
- `/fakeplayer remove <名称>` - 移除假玩家（需权限）
- `/fakeplayer list` - 列出所有假玩家（需权限）

#### 8. 私人门系统
- `/privatedoor [数量]` - 获取私人门（需OP权限）
- 私人门只有放置者可以打开，其他人无法开启
- 私人门只有放置者或管理员可以破坏

#### 9. 自定义音乐系统 ⚠️ 重要

- `/music list` - 查看所有音乐列表
- `/music give <歌曲ID>` - 获取音乐物品（右键播放/暂停，**全体玩家同时播放**）
- `/music add <歌曲ID> <歌曲名称>` - 添加音乐（需OP权限）
- `/music remove <歌曲ID>` - 移除音乐（需OP权限）
- `/music reload` - 重新生成资源包（需OP权限）
- 音乐文件需为 **OGG 格式**，放置于 `plugins/StuffPack/music/` 目录
- **资源包由插件自动生成**在 `plugins/StuffPack/resourcepack.zip`
- **资源包推送需手动交给 server.properties 管理**，插件不自动推送资源包(之后会发布其他插件来配合)
- **⚠️ 必须在 server.properties 中配置资源包 URL，玩家才能听到音乐**

**快速上手：**
1. 将 OGG 文件放入 `plugins/StuffPack/music/<ID>.ogg`
2. 游戏内输入 `/music add <ID> <歌曲名>`
3. 将生成的 `resourcepack.zip` 放到 HTTP 服务器（推荐配合 WebServ 插件）
4. 在 `server.properties` 中配置 `resource-pack` 下载地址
5. 重启服务器，玩家加入自动加载资源包
6. 用 `/music give <ID>` 获得音乐物品，右键全服播放！

### 模块开关

所有功能模块可在 `config.yml` 中单独启用/禁用：
```yaml
modules:
  login: true         # 登录系统
  domain: true        # 领地系统
  coin: true          # 经济/签到系统
  teleport: true      # TPA传送
  back: true          # 返回死亡点
  fakeplayer: true    # 假玩家系统
  picture: true       # 图片地图系统
  message: true       # 私信系统
  privatedoor: true   # 私密门系统
  music: true         # 自定义音乐系统
  performance: true   # 性能优化
```

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
| `stuffpack.music.give` | 获取音乐物品权限 | 所有人 |
| `stuffpack.music.admin` | 音乐管理权限（添加/删除/重载） | OP |

### 技术栈

- Java 21
- Paper API 1.21.11-R0.1-SNAPSHOT
- Purpur 1.21.11 服务端
- Maven 构建工具

### 编译方法

**方法一：使用 build.bat（推荐）**
```
在项目上层目录双击运行 build.bat
```

**方法二：手动编译**
```bash
cd dom
mvn clean package
```

编译后的 JAR 文件位于 `dom/target/stuffpack-1.2.0.jar`

### 安装方法

1. 将 `stuffpack-1.2.0.jar` 复制到服务器的 `plugins` 目录
2. 重启服务器

---

### 配置文件

插件会在首次运行时自动创建配置目录和数据文件：
- `plugins/StuffPack/domains.yml` - 领地数据
- `plugins/StuffPack/coins.yml` - 金币数据
- `plugins/StuffPack/login.yml` - 登录数据
- `plugins/StuffPack/messages.yml` - 留言数据
- `plugins/StuffPack/pictures.yml` - 图片数据
- `plugins/StuffPack/config.yml` - 主配置文件（含模块开关和音乐配置）
- `plugins/StuffPack/music.yml` - 音乐数据
- `plugins/StuffPack/music/` - 音乐文件目录（OGG格式）
- `plugins/StuffPack/resourcepack.zip` - 自动生成的资源包

### 音乐配置示例（config.yml）

```yaml
music:
  # 玩家加入时是否由插件自动推送资源包
  # ⚠️ 强烈建议设为 false，在 server.properties 中配置资源包
  auto-send-resourcepack: false

  # 资源包格式版本（Minecraft 不同版本需要不同的值）
  # 15 = 1.20 - 1.20.1
  # 18 = 1.20.4
  # 32 = 1.21
  # 34 = 1.21.3 / 1.21.4
  # 41 = 1.21.3+
  pack-format: 34
```

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

#### 9. Custom Music System ⚠️ Important

- `/music list` - View all music tracks
- `/music give <track ID>` - Get a music item (right-click to play/pause, **plays for ALL players**)
- `/music add <track ID> <track name>` - Add music (requires OP)
- `/music remove <track ID>` - Remove music (requires OP)
- `/music reload` - Regenerate the resource pack (requires OP)
- Music files must be in **OGG format**, placed in the `plugins/StuffPack/music/` directory
- The **resource pack is auto-generated** at `plugins/StuffPack/resourcepack.zip`
- **Resource pack delivery must be configured manually in server.properties**, the plugin does not auto-push resource packs (a companion plugin will be released later)
- **⚠️ You must configure the resource pack URL in server.properties for players to hear music**

**Quick Start:**
1. Place OGG files in `plugins/StuffPack/music/<ID>.ogg`
2. In-game, run `/music add <ID> <song name>`
3. Host the generated `resourcepack.zip` on an HTTP server (recommended: use with WebServ plugin)
4. Configure the `resource-pack` download URL in `server.properties`
5. Restart the server, players will auto-load the resource pack on join
6. Use `/music give <ID>` to get the music item, right-click to play for everyone!

### Module Configuration

All feature modules can be enabled/disabled individually in `config.yml`:
```yaml
modules:
  login: true         # Login System
  domain: true        # Land Claim System
  coin: true          # Economy/Sign-in System
  teleport: true      # TPA Teleport
  back: true          # Back to previous location
  fakeplayer: true    # Fake Player System
  picture: true       # Picture Map System
  message: true       # Offline Messaging System
  privatedoor: true   # Private Door System
  music: true         # Custom Music System
  performance: true   # Performance Optimization
```

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
| `stuffpack.music.give` | Get music items | All players |
| `stuffpack.music.admin` | Music management (add/remove/reload) | OP |

### Tech Stack

- Java 21
- Paper API 1.21.11-R0.1-SNAPSHOT
- Purpur 1.21.11 Server
- Maven Build Tool

### Compilation

**Method 1: Use build.bat (recommended)**
```
Double-click build.bat in the project parent directory
```

**Method 2: Manual compilation**
```bash
cd dom
mvn clean package
```

The compiled JAR file is located at `dom/target/stuffpack-1.2.0.jar`

### Installation

1. Copy `stuffpack-1.2.0.jar` to the server's `plugins` directory
2. Restart the server

---

### Configuration Files

The plugin will automatically create configuration directories and data files on first run:
- `plugins/StuffPack/domains.yml` - Territory data
- `plugins/StuffPack/coins.yml` - Coin data
- `plugins/StuffPack/login.yml` - Login data
- `plugins/StuffPack/messages.yml` - Message data
- `plugins/StuffPack/pictures.yml` - Picture data
- `plugins/StuffPack/config.yml` - Main configuration (includes module toggles and music settings)
- `plugins/StuffPack/music.yml` - Music data
- `plugins/StuffPack/music/` - Music files directory (OGG format)
- `plugins/StuffPack/resourcepack.zip` - Auto-generated resource pack

### Music Configuration Example (config.yml)

```yaml
music:
  # Auto-send resource pack when players join
  # ⚠️ Strongly recommended to set to false, configure in server.properties instead
  auto-send-resourcepack: false

  # Resource pack format version (different Minecraft versions require different values)
  # 15 = 1.20 - 1.20.1
  # 18 = 1.20.4
  # 32 = 1.21
  # 34 = 1.21.3 / 1.21.4
  # 41 = 1.21.3+
  pack-format: 34
```

### Developer

HenryGo-mc

### License

MIT License
