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
  "name": "path"
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

`// TODO`

#### 获取某个 Vtuber 的某一分组下的所有语音
```http request
GET /vtubers/:name/:group
```

`// TODO`

### 统计数据相关 API


