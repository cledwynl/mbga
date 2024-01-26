# 一些开发时所需资源

## 方便开发使用的代码片段

### 输出调用栈

```
val e = RuntimeException("<Start dump Stack !>")
e.fillInStackTrace()
YLog.debug("<Dump Stack>: ", e)
```
