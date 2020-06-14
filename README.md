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

##### 返回数据
```json
{
  "name": "path",
   ...
}
```

|参数|说明|
|:---:|:----:|
|name|Vtuber 的名字|
|path|该 Vtuber 的资源路径|

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

##### 返回数据
```json
{
  "name": "Vtuber 的名字",
  "groups": [ group, group, ... ]
}
```

每一个 `group` 都具有如下的结构
```json
{
    "name": "组的名字",
    "desc": {
        "zh": "中文翻译",
        "en": "英文翻译",
        "ja": "日文翻译"
    },
    "voices": [ voice, voice, ... ]
}
```

每一个 `voice` 都具有如下的结构
```json
{
    "name": "音频名字",
    "url": "音频路径",
    "group": "音频所属组",
    "desc": {
        "zh": "中文翻译",
        "en": "英文翻译",
        "ja": "日文翻译"
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

##### 返回数据
返回的数据为一个 `group` （关于 `group` 的结构请查看上文）

### 统计数据相关 API

### 管理权限相关 API


