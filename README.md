vtbtn-server
=====================

为**所有**按钮项目设计的服务器，提供如下功能:

- 按钮数据
- 统计数据

## 使用方法

API 采用 RESTful 风格设计，请求和响应的数据格式一律为 `application/json`

### 按钮数据相关 API

#### 获取所有 Vtuber 的列表
```http request
GET /vtubers
```

##### 响应
```json
{
  "<Vtuber 的名字>": "<该 Vtuber 的资源路径>",
   ...
}
```

例如：
```json
{
  "fubuki": "/vtubers/fubuki"
}
```

说明当前服务器上共存储了一位 Vtuber 的按钮信息（fubuki），获取该 Vtuber 的所有按钮数据
（语音/分组）都应该请求 `/vtuber/fubuki` 路径下的资源

#### 获取某个 Vtuber 的所有语音
```http request
GET /vtubers/:name
```
|参数|说明|
|:---:|:----:|
|name|Vtuber 的名字|

##### 响应
```json
{
  "name": "<Vtuber 的名字>",
  "groups": [ group, group, ... ]
}
```

每一个 `group` 都具有如下的结构
```json
{
  "name": "<组的名字>",
  "desc": {
    "zh": "<中文翻译>",
    "en": "<英文翻译>",
    "ja": "<日文翻译>"
  },
  "voices": [ voice, voice, ... ]
}
```

每一个 `voice` 都具有如下的结构
```json
{
  "name": "<音频名字>",
  "url": "<音频路径>",
  "group": "<音频所属组>",
  "desc": {
    "zh": "<中文翻译>",
    "en": "<英文翻译>",
    "ja": "<日文翻译>"
  }
}
```

#### 获取某个 Vtuber 的某一分组下的所有语音
```http request
GET /vtubers/:name/:group
```

|参数|说明|
|:---:|:----:|
|name|Vtuber 的名字|
|group|组名|

##### 响应
返回的数据为一个 `group` （关于 `group` 的结构请查看上文）

#### 新增一个组 (Group)
```http request
GET /vtubers/:name/add-group
```

该操作会在 Vtuber 名为 `:name` 的语音数据库中新增一个组。

注意：该操作需要对应 Vtuber 的**管理员**权限

|参数|说明|
|:---:|:----:|
|name|Vtuber 的名字|

##### 请求体
```json
{
  "name": "<组的名字>",
  "desc": {
    "zh": "<中文翻译>",
    "en": "<英文翻译>",
    "ja": "<日文翻译>"
  }
}
```

##### 响应
如果操作成功，服务器返回 `200 OK`

如果操作失败，服务器可能返回以下任一错误码:
- 403: 权限不足
- 500: 服务器内部错误 ~~(可以提 issue 了)~~

无论是哪种错误，响应体中均会包含如下格式的信息
```json
{
  "msg": "<操作失败的原因>"
}
```

#### 新增一条语音 (Voice)
```http request
GET /vtubers/:name/:group/add-voice
```

该操作会在 Vtuber 名为 `:name` 的语音数据库中新增一条语音，并且该语音的组被设置为 `:group`。

注意：该操作需要对应 Vtuber 的**管理员**权限

|参数|说明|
|:---:|:----:|
|name|Vtuber 的名字|
|group|组名|

##### 请求体
```json
{
  "name": "<音频名字>",
  "url": "<音频路径>",
  "desc": {
    "zh": "<中文翻译>",
    "en": "<英文翻译>",
    "ja": "<日文翻译>"
  }
}
```

##### 响应
如果操作成功，服务器返回 `200 OK`

如果操作失败，服务器可能返回以下任一错误码:
- 403: 权限不足
- 500: 服务器内部错误 ~~(可以提 issue 了)~~

无论是哪种错误，响应体中均会包含如下格式的信息
```json
{
  "msg": "<操作失败的原因>"
}
```

### 统计数据相关 API

### 管理权限相关 API

#### 你好
```http request
GET /users/hi
```

~~一个没什么用的 API，~~
~~可以用于测试是否已经登录~~

#### 登录
```http request
POST /users/login
```

##### 请求体
```json
{
  "uid": "<用户 ID>",
  "password": "<用户密码>"
}
```

**为了您和用户的身心健康，请勿直接传输明文密码**

##### 响应
若登录成功，服务器返回 `200 OK`

若失败，服务器返回 `403 Forbidden` 并且响应体包含如下格式的信息
```json
{
  "msg": "<登陆失败的原因>"
}
```

#### 注册
```http request
POST /users/register
```

##### 请求体
```json
{
  "uid": "<用户 ID>",
  "password": "<用户密码>",
  "name": "<用户昵称>",
  "email": "<用户邮箱>"
}
```

**为了您和用户的身心健康，请勿直接传输明文密码**

##### 响应
若登录成功，服务器返回 `200 OK`，并会自动设置 `Set-Cookie` 响应头

若失败，服务器返回 `403 Forbidden` 并且响应体包含如下格式的信息
```json
{
  "msg": "<注册失败的原因>"
}
```

#### 修改可管理的 Vtuber 列表
```http request
POST /users/change-admin-vtuber
```

注意：该操作需要**超级管理员**用户

##### 请求体
```json
{
  "uid": "<被修改的用户 ID>",
  "add": [],
  "remove": []
}
```

参数说明:
- `add`: 将要添加到可管理列表中的 Vtuber 的名字
- `remove`: 将要从可管理列表中移除的 Vtuber 的名字

如果 `remove` 中包含 `add` 中的元素，则该元素并不会添加到可管理列表中

##### 响应
如果操作成功，服务器返回 `200 OK`

如果操作失败，服务器可能返回以下任一错误码:
- 403: 权限不足
- 500: 服务器内部错误 ~~(可以提 issue 了)~~

无论是哪种错误，响应体中均会包含如下格式的信息
```json
{
  "msg": "<操作失败的原因>"
}
```


