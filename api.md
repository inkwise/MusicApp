# 音乐服务器 API 文档

## 基础信息

- **基础URL**: `http://localhost:8080/api/v1`
- **认证方式**: Bearer Token (JWT)
- **内容类型**: `application/json`
- **文件上传**: `multipart/form-data`

---

## 认证接口

### 用户注册

```
POST /auth/register
```

**请求体**:
```json
{
    "username": "string (必填, 3-50字符)",
    "password": "string (必填, 至少6字符)",
    "email": "string (可选)"
}
```

**响应**:
```json
{
    "message": "注册成功",
    "token": "jwt_token_string",
    "user": {
        "id": 1,
        "username": "testuser",
        "email": "test@example.com"
    }
}
```

---

### 用户登录

```
POST /auth/login
```

**请求体**:
```json
{
    "username": "string (必填)",
    "password": "string (必填)"
}
```

**响应**:
```json
{
    "message": "登录成功",
    "token": "jwt_token_string",
    "user": {
        "id": 1,
        "username": "testuser",
        "email": "test@example.com"
    }
}
```

---

### 获取用户信息

```
GET /profile
```

**请求头**: `Authorization: Bearer <token>`

**响应**:
```json
{
    "user": {
        "id": 1,
        "username": "testuser",
        "email": "test@example.com",
        "avatar": "",
        "created_at": "2024-01-01T00:00:00Z"
    }
}
```

---

## 音乐接口

### 上传音乐

```
POST /music/upload
```

**请求头**: `Authorization: Bearer <token>`

**请求体**: `multipart/form-data`
- `file`: 音频文件 (支持: mp3, wav, flac, m4a, ogg, wma)

**响应**:
```json
{
    "message": "上传成功",
    "music": {
        "id": 1,
        "title": "歌曲标题",
        "artist": { "id": 1, "name": "艺术家名" },
        "album": "专辑名",
        "genre": "流派",
        "duration": 240.0,
        "size": 12345678,
        "bitrate": 320,
        "sample_rate": 44100,
        "channels": 2,
        "channel_count": 2,
        "format": "mp3",
        "codec": "MPEG Audio Layer 3",
        "lyrics": "",
        "oss_url": "https://...",
        "cover_url": "https://...",
        "download_url": "/api/v1/music/1/proxy-download",
        "stream_url": "/api/v1/music/1/stream"
    }
}
```

---

### 获取音乐列表

```
GET /music/list
```

**请求头**: `Authorization: Bearer <token>`

**查询参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| page_size | int | 否 | 每页数量，默认20 |
| keyword | string | 否 | 搜索关键词（支持空格分隔多关键词） |
| genre | string | 否 | 按流派过滤 |
| artist | string | 否 | 按艺术家过滤 |
| album | string | 否 | 按专辑过滤 |
| format | string | 否 | 按格式过滤 |
| sort_by | string | 否 | 排序字段：created_at, title, duration, bitrate |
| sort_order | string | 否 | 排序方向：asc, desc |

**响应**:
```json
{
    "data": [
        {
            "id": 1,
            "title": "歌曲标题",
            "artist": { "id": 1, "name": "艺术家名" },
            "album": "专辑名",
            "genre": "流行",
            "duration": 240.0,
            "format": "mp3",
            "oss_url": "https://...",
            "cover_url": "https://...",
            "download_url": "/api/v1/music/1/proxy-download",
            "stream_url": "/api/v1/music/1/stream"
        }
    ],
    "pagination": {
        "page": 1,
        "page_size": 20,
        "total": 100,
        "total_pages": 5
    },
    "note": "URL有效期为1小时"
}
```

---

### 获取音乐详情

```
GET /music/:id
```

**请求头**: `Authorization: Bearer <token>`

**响应**:
```json
{
    "music": {
        "id": 1,
        "title": "歌曲标题",
        "artist": { "id": 1, "name": "艺术家名" },
        "album": "专辑名",
        "genre": "流行",
        "duration": 240.0,
        "format": "mp3",
        "oss_url": "https://...",
        "cover_url": "https://...",
        "download_url": "/api/v1/music/1/proxy-download",
        "stream_url": "/api/v1/music/1/stream"
    },
    "url_expire": "1小时后过期"
}
```

---

### 更新音乐信息

```
PUT /music/:id
```

**请求头**: `Authorization: Bearer <token>`

**请求体**:
```json
{
    "title": "新标题 (可选)",
    "artist": "新艺术家名 (可选)",
    "album": "新专辑名 (可选)",
    "genre": "新流派 (可选)",
    "lyrics": "新歌词 (可选)"
}
```

**响应**:
```json
{
    "message": "更新成功",
    "music": {
        "id": 1,
        "title": "新标题",
        "artist": { "id": 1, "name": "新艺术家名" },
        "album": "新专辑名",
        "genre": "新流派",
        "lyrics": "新歌词",
        "duration": 240.0,
        "format": "mp3",
        "oss_url": "https://...",
        "cover_url": "https://...",
        "download_url": "/api/v1/music/1/proxy-download",
        "stream_url": "/api/v1/music/1/stream"
    }
}
```

---

### 下载音乐（重定向）

```
GET /music/:id/download
```

**请求头**: `Authorization: Bearer <token>`

**说明**: 重定向到OSS签名URL进行直接下载

---

### 下载音乐（代理）

```
GET /music/:id/proxy-download
```

**请求头**: `Authorization: Bearer <token>`

**说明**: 通过服务器代理下载，解决跨域和权限问题

**响应头**:
- `Content-Type`: `application/octet-stream`
- `Content-Disposition`: `attachment; filename*=UTF-8''filename.mp3`
- `X-Music-Title`: 歌曲标题
- `X-Music-Artist`: 艺术家名

---

### 流式播放音乐

```
GET /music/:id/stream
```

**说明**: 无需认证，返回音频内容，支持在线播放

**响应头**:
- `Content-Type`: `audio/mpeg` (根据格式自动设置)
- `Accept-Ranges`: `bytes`
- `Content-Disposition`: `inline; filename*=UTF-8''filename.mp3`

---

### 刷新音乐URL

```
GET /music/:id/refresh-url
```

**请求头**: `Authorization: Bearer <token>`

**响应**:
```json
{
    "url": "https://...",
    "cover_url": "https://...",
    "expire_in": "1小时"
}
```

---

### 删除音乐

```
DELETE /music/:id
```

**请求头**: `Authorization: Bearer <token>`

**响应**:
```json
{
    "message": "删除成功"
}
```

---

### 搜索建议

```
GET /music/search/suggestions
```

**请求头**: `Authorization: Bearer <token>`

**查询参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | string | 是 | 搜索关键词 |

**响应**:
```json
{
    "titles": ["歌曲1", "歌曲2"],
    "artists": ["艺术家1", "艺术家2"],
    "albums": ["专辑1", "专辑2"]
}
```

---

## 艺术家接口

### 获取艺术家列表

```
GET /artists/
```

**请求头**: `Authorization: Bearer <token>`

**查询参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| page_size | int | 否 | 每页数量，默认50 |

**响应**:
```json
{
    "data": [
        {
            "id": 1,
            "name": "艺术家名",
            "description": "描述",
            "avatar_url": "https://..."
        }
    ],
    "pagination": {
        "page": 1,
        "page_size": 50,
        "total": 10
    }
}
```

---

### 获取艺术家详情

```
GET /artists/:id
```

**请求头**: `Authorization: Bearer <token>`

**响应**:
```json
{
    "artist": {
        "id": 1,
        "name": "艺术家名",
        "description": "描述",
        "avatar_url": "https://...",
        "musics": [
            {
                "id": 1,
                "title": "歌曲标题",
                "album": "专辑名",
                "duration": 240.0
            }
        ]
    }
}
```

---

## 歌单接口

### 创建歌单

```
POST /playlists
```

**请求头**: `Authorization: Bearer <token>`

**请求体**:
```json
{
    "name": "歌单名 (必填)",
    "description": "描述 (可选)",
    "cover_url": "封面URL (可选)"
}
```

**响应**:
```json
{
    "message": "创建成功",
    "playlist": {
        "id": 1,
        "name": "歌单名",
        "description": "描述",
        "cover_url": "https://...",
        "user_id": "1",
        "created_at": "2024-01-01T00:00:00Z"
    }
}
```

---

### 获取歌单列表

```
GET /playlists
```

**请求头**: `Authorization: Bearer <token>`

**查询参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| page_size | int | 否 | 每页数量，默认20 |
| user_id | string | 否 | 按用户过滤 |

**响应**:
```json
{
    "data": [
        {
            "id": 1,
            "name": "歌单名",
            "description": "描述",
            "cover_url": "https://...",
            "user_id": "1",
            "created_at": "2024-01-01T00:00:00Z"
        }
    ],
    "pagination": {
        "page": 1,
        "page_size": 20,
        "total": 5
    }
}
```

---

### 获取歌单详情

```
GET /playlists/:id
```

**请求头**: `Authorization: Bearer <token>`

**响应**:
```json
{
    "playlist": {
        "id": 1,
        "name": "歌单名",
        "description": "描述",
        "cover_url": "https://...",
        "user_id": "1",
        "musics": [
            {
                "id": 1,
                "title": "歌曲标题",
                "artist": { "id": 1, "name": "艺术家名" },
                "album": "专辑名",
                "duration": 240.0
            }
        ]
    }
}
```

---

### 更新歌单

```
PUT /playlists/:id
```

**请求头**: `Authorization: Bearer <token>`

**请求体**:
```json
{
    "name": "新歌单名 (可选)",
    "description": "新描述 (可选)",
    "cover_url": "新封面URL (可选)"
}
```

**响应**:
```json
{
    "message": "更新成功",
    "playlist": {
        "id": 1,
        "name": "新歌单名",
        "description": "新描述",
        "cover_url": "https://..."
    }
}
```

---

### 删除歌单

```
DELETE /playlists/:id
```

**请求头**: `Authorization: Bearer <token>`

**响应**:
```json
{
    "message": "删除成功"
}
```

---

### 添加歌曲到歌单

```
POST /playlists/:id/music
```

**请求头**: `Authorization: Bearer <token>`

**请求体**:
```json
{
    "music_id": 1
}
```

**响应**:
```json
{
    "message": "添加成功",
    "data": {
        "id": 1,
        "playlist_id": 1,
        "music_id": 1,
        "added_at": "2024-01-01T00:00:00Z",
        "sort_order": 1
    }
}
```

---

### 批量添加歌曲到歌单

```
POST /playlists/:id/music/batch
```

**请求头**: `Authorization: Bearer <token>`

**请求体**:
```json
{
    "music_ids": [1, 2, 3]
}
```

**响应**:
```json
{
    "message": "成功添加 3 首歌曲，跳过 0 首",
    "added": 3,
    "skipped": 0
}
```

---

### 从歌单移除歌曲

```
DELETE /playlists/:id/music/:musicId
```

**请求头**: `Authorization: Bearer <token>`

**响应**:
```json
{
    "message": "移除成功"
}
```

---

### 获取歌单中的歌曲

```
GET /playlists/:id/music
```

**请求头**: `Authorization: Bearer <token>`

**响应**:
```json
{
    "songs": [
        {
            "id": 1,
            "title": "歌曲标题",
            "artist": { "id": 1, "name": "艺术家名" },
            "album": "专辑名",
            "duration": 240.0,
            "format": "mp3"
        }
    ],
    "total": 10
}
```

---

### 重新排序歌单中的歌曲

```
PUT /playlists/:id/music/reorder
```

**请求头**: `Authorization: Bearer <token>`

**请求体**:
```json
{
    "music_ids": [3, 1, 2]
}
```

**响应**:
```json
{
    "message": "排序更新成功"
}
```

---

### 批量删除音乐

```
DELETE /playlists/music/batch
```

**请求头**: `Authorization: Bearer <token>`

**请求体**:
```json
{
    "music_ids": [1, 2, 3]
}
```

**响应**:
```json
{
    "message": "成功删除 3 首歌曲"
}
```

---

## 健康检查

### 服务器健康状态

```
GET /health
```

**响应**:
```json
{
    "status": "ok"
}
```

---

## 管理面板

### 管理面板页面

```
GET /admin/
```

**说明**: 返回浏览器管理面板HTML页面，支持PC和移动端自适应

**功能**:
- 音乐管理（列表、搜索、播放、编辑、删除、添加到歌单）
- 歌单管理（创建、查看、删除、添加/移除歌曲）
- 艺术家管理（列表、详情）
- 上传音乐
- 个人中心（查看用户信息、退出登录）
- 悬浮播放器（在当前页面播放音乐）

---

## 错误响应格式

所有接口在出错时返回以下格式：

```json
{
    "error": "错误描述信息"
}
```

常见HTTP状态码：
| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证（缺少或无效的令牌） |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 409 | 资源冲突（如用户名已存在） |
| 500 | 服务器内部错误 |

---

## 认证说明

1. 调用 `/auth/register` 或 `/auth/login` 获取JWT令牌
2. 在后续请求的 `Authorization` 头中添加 `Bearer <token>`
3. 令牌有效期为24小时
4. 令牌过期后需要重新登录获取新令牌
5. 流媒体播放接口 `/music/:id/stream` 无需认证
