# Make Bilibili Great Again!

## 功能

目前只支持国际版(com.bilibili.app.in)，兼容性见文末

### 底部Tab

* [X] 允许干掉首页
* [X] 允许干掉动态

### 首页

* [X] 干掉竖屏

### 搜索页

* [X] 干掉热搜，简单修改代码也可干掉建议搜索，等设置界面做出来以后这里可以自由配置

### 视频播放器

* [X] 干掉播放器内展示的“云视听小电视”等activityMeta（不确定未来这个字段里还会塞些什么乱七八糟的东西，目前要么没有要么就是这个云视听，直接全部干掉了）
  ![video_player_activity_meta](./app/src/main/res/drawable/video_player_activity_meta.png)
* [X] 干掉播放器内展示的关注弹窗、投票弹窗等（包括UP主弹幕）

### 视频详情页

* [X] 视频标题左边的“热门”等标签
  ![video_detail_label](./app/src/main/res/drawable/video_detail_label.png)
* [X] 更纯粹的链接分享
  原来分享链接是`https://b23.tv/xxxxxxx`这样的，让它变成`https://www.bilibili.com/video/BVxxxxx`这样的

### 视频评论区

* [X] 点击评论区文字不会自动弹出回复区
  ![comment_view_quick_reply](./app/src/main/res/drawable/comment_view_quick_reply.png)

### 设置

* [X] 打开B站->我的->设置->在标题上加入MBGA字样，点击打开设置界面

## 感谢

> 本项目参考、使用了下列开源项目的部分或全部内容

* [Yuki Hook API](https://github.com/HighCapable/YukiHookAPI)
* [FuckBilibiliVote](https://github.com/zerorooot/FuckBilibiliVote)
* [不要竖屏](https://github.com/WankkoRee/Portrait2Landscape)

## 兼容性

下面仅列出测试过的版本，相近版本大概率能兼容

* MBGA v1.0.0 ~ v1.0.2
  * 兼容国际版 未知 ~ 3.18.2 ~ 未知
