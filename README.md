# Epic4j

> 免费领取Epic周免游戏，本项目由EpicGamesClaimer而来

[EpicGamesClaimer](https://github.com/luminoleon/epicgames-claimer)

## 开始

### window

需要环境 jdk8+,maven 拉取项目编译后运行

### Docker

## 配置

### yaml

其中的参数值为默认值

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
  noSandbox: true
```

### 环境变量

可以配置的环境变量

| 参数 | 说明 | 备注 |
| ---- | ---- | ----- |
|EMAIL|邮箱地址||
|PASSWORD|密码||
|LOG_LEVEL|日志级别|日志级别为debug可以看到更多的日志,并且在有异常抛出时会截图到error/目录下|



