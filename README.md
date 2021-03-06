# Epic4j


# [一个更好的游戏领取项目](https://github.com/QIN2DIM/epic-awesome-gamer)推荐大家使用

## 本项目可能要暂停维护了


[Epic4j](https://github.com/huisunan/epic4j)|[EpicGamesClaimer](https://github.com/luminoleon/epicgames-claimer)

> 免费领取Epic周免游戏，本项目由EpicGamesClaimer而来

QQ交流群:551322748

## 开始

### window

#### ide下运行

需要环境 jdk8+,maven 拉取项目编译后运行

#### 命令行运行

[下载jar包](https://github.com/huisunan/epic4j/releases)

```shell
java -jar -Depic.email=[你的账号] -Depic.password[你的密码] epic4j.jar 
```
使用cookie
```shell
java -jar -Depic.email=[你的账号] -Depic.password[你的密码] -Depic.cookiePath=[你的cookie路径]  epic4j.jar 
```

### Docker

```shell
#docker拉取
docker pull huisunan/epic4j:latest
#密码登录
docker run -d -e EMAIL=[你的邮箱] -e PASSWORD=[你的密码] --name epic4j huisunan/epic4j:latest
#debug模式运行
docker run -d -e EMAIL=[你的邮箱] -e PASSWORD=[你的密码] -e LOG_LEVEL=debug --name epic4j huisunan/epic4j:latest
#cookie登录
docker run -d -e EMAIL=[你的邮箱] -e PASSWORD=[你的密码] -e COOKIE_PATH=[cookie路径] -v [本机cookie路径]:[cookie路径] --name epic4j huisunan/epic4j:latest

```

**挂载配置文件方式运行(推荐)**

[具体配置](#yaml)

```shell
# 创建数据目录
mkdir ~/epic4j
# 创建配置文件
vim ~/epic4j/application.yml
# 创建持久卷,用来保存用户数据，再升级容器时保存用户数据
docker volume create epic4jVolume
```

application.yml的配置如下

```yaml
epic:
  email: 你的邮箱
  password: 你的密码
  #开启自动更新,可选
  auto-update: true
```

运行docker容器,挂载配置文件到/opt/epic4j/config下

```shell
docker run -d -v ~/epic4j:/opt/epic4j/config -v epic4jVolume:/opt/epic4j/data --name myepic huisunan/epic4j:latest
```

#### 多用户配置

以上为单用户配置,还支持多用户配置

```yaml
epic:
  #开启自动更新,可选
  auto-update: true
  # 开启多用户支持
  multi-user: true
  users:
    - email: demo1
      password: pass1
    - email: demo2
      password: pass2
```

## 配置

### yaml

其中的参数值为默认值<sapan id="yaml"></sapn>

```yaml
epic:
  # 浏览器用户文件存储位置,默认为jar包同路径下data文件夹,不存在会新建目录
  dataPath: ./data
  # 浏览器启动参数
  driverArgs:
  # email邮箱地址
  email:
  # 密码
  password: password
  # headLess无头模式
  headLess: true
  # browserVersion指定chromium的版本,可能有一定风险
  browser-version:
  # crontab表达式,不填写的情况下是每天程序启动的时分秒运行一次
  cron:
  # noSandbox非沙盒运行
  no-sandbox: true
  # cookie cookie路径,如果路径不为空会加载cookie
  cookie-path:
  # 自动更新默认为false,true开启
  auto-update: false
  # 开启多用户 默认为false
  multi-user: false
  # 多用户信息
  users:
  # 错误时截图,默认为true
  error-screen-shoot: true
  # 操作超时时间ms,默认30s
  timeout: 30000
  # 操作间隔ms,间隔越短,轮询越快,适当控制
  interval: 100
```

### 环境变量

可以配置的环境变量

| 参数 | 说明 | 备注 |
| ---- | ---- | ----- |
|EMAIL|邮箱地址||
|PASSWORD|密码||
|LOG_LEVEL|日志级别|日志级别为debug可以看到更多的日志|
|COOKIE_PATH|cookie路径|cookie不为空则加载,docker下通过挂载目录的方式,加载cookie路径|
|CRON|cron表达式|定时任务([表达式验证](https://www.bejson.com/othertools/cronvalidate/))|

## 计划

|名称|状态|
|---|----|
|cookie登录|✅|
|i18n支持||
|消息推送||
|自动更新|✅|
|多账号批量处理|✅|
|可视化界面||

## 获取cookie
使用chrome浏览器安装[EditThisCookie](https://chrome.google.com/webstore/detail/editthiscookie/fngmhnnpilhplaeedifhccceomclgfbg)
![](doc/EditThisCookie.png)

获取网站的cookie
![](doc/ExportCookie.png)

新建文本文件保存cookie
