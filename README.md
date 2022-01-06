# Epic4j

> 免费领取Epic周免游戏，本项目由EpicGamesClaimer而来

[EpicGamesClaimer](https://github.com/luminoleon/epicgames-claimer)

## 开始

### window

#### ide下运行

需要环境 jdk8+,maven 拉取项目编译后运行

#### 命令行运行

[下载jar包](https://github.com/huisunan/epic4j/releases)

```shell
java -jar -Depic.email=[你的账号] -Depic.password[你的密码] epic4j.jar 
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
mkdir ~/epic4j
vim ~/epic4j/application.yml
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
docker run -d -v ~/epic4j:/opt/epic4j/config --name myepic huisunan/epic4j:latest
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
  # epic站点url
  epicUrl: https://www.epicgames.com/store/en-US/
  # email邮箱地址
  email:
  # 密码
  password: password
  # headLess无头模式
  headLess: true
  # browserVersion指定chromium的版本,可能有一定风险
  browser-version:
  # checkLoginUrl epic登录判断api
  check-login-url: https://www.epicgames.com/account/v2/ajaxCheckLogin
  # userInfoUrl获取用户信息url
  user-info-url: https://www.epicgames.com/account/v2/personal/ajaxGet?sessionInvalidated=true
  # freeGameUrl免费游戏url
  free-game-url: https://store-site-backend-static.ak.epicgames.com/freeGamesPromotions?locale={}&country={}&allowCountries={}
  # storeUrl商店url
  store-url: https://www.epicgames.com/store/en-US/p/{}
  # crontab表达式,不填写的情况下是每天程序启动的时分秒运行一次
  cron:
  # noSandbox非沙盒运行
  no-sandbox: true
  # cookie cookie路径,如果路径不为空会加载cookie
  cookie-path:
  # 自动更新默认为false,true开启
  auto-update: false
```

### 环境变量

可以配置的环境变量

| 参数 | 说明 | 备注 |
| ---- | ---- | ----- |
|EMAIL|邮箱地址||
|PASSWORD|密码||
|LOG_LEVEL|日志级别|日志级别为debug可以看到更多的日志,并且在有异常抛出时会截图到error/目录下|
|COOKIE_PATH|cookie路径|cookie不为空则加载,docker下通过挂载目录的方式,加载cookie路径|
|CRON|cron表达式|定时任务([表达式验证](https://www.bejson.com/othertools/cronvalidate/))|

## 计划

|名称|状态|
|---|----|
|cookie登录|✅|
|i18n支持||
|消息推送||
|自动更新|✅|
|多账号批量处理||

