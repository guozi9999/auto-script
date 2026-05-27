# AutoScript

一个简单的 Android 自动化脚本工具，类似 Auto.js，使用 JavaScript 编写自动化脚本。

## 功能

- ✅ 点击指定坐标
- ✅ 长按操作
- ✅ 滑动操作
- ✅ 输入文字
- ✅ 返回/主页/最近任务
- ✅ 脚本编辑和保存
- ✅ 悬浮窗控制
- ✅ 日志输出

## 权限要求

1. **无障碍服务** - 用于执行点击、滑动等操作
2. **悬浮窗权限** - 用于显示控制按钮

## 使用方法

### 1. 安装应用

使用 Android Studio 打开项目，编译安装到手机。

### 2. 开启权限

1. 打开应用，点击"悬浮窗"按钮，授予悬浮窗权限
2. 进入系统设置 → 无障碍 → 找到 AutoScript → 开启

### 3. 编写脚本

在编辑区输入 JavaScript 脚本，或点击"示例"选择预设脚本。

### 4. 运行脚本

点击"▶ 运行"按钮执行脚本。

## API 参考

```javascript
// 点击坐标 (x, y)
click(540, 960);

// 长按坐标 (x, y, duration_ms)
longClick(540, 960, 1000);

// 滑动 (x1, y1, x2, y2, duration_ms)
swipe(540, 1500, 540, 500, 300);

// 输入文字（需要先点击输入框）
input("Hello World");

// 按键操作
back();    // 返回
home();    // 主页
recent();  // 最近任务

// 延迟（毫秒）
sleep(1000);

// 输出日志
log("这是一条日志");

// 显示 Toast 提示
toast("操作完成");
```

## 示例脚本

### 连续点击
```javascript
for (var i = 0; i < 10; i++) {
    click(540, 960);
    log("第 " + (i+1) + " 次点击");
    sleep(200);
}
```

### 滑动浏览
```javascript
// 向下滑动 5 次
for (var i = 0; i < 5; i++) {
    swipe(540, 1500, 540, 500, 300);
    sleep(1000);
}
```

### 输入文字
```javascript
click(540, 500);  // 点击输入框
sleep(300);
input("AutoScript 很好用！");
```

## 技术栈

- Kotlin
- Rhino JS 引擎
- Android Accessibility Service
- ViewBinding

## 项目结构

```
app/src/main/java/com/guozi/autoscript/
├── MainActivity.kt          # 主界面
├── ScriptRunner.kt           # 脚本执行器
├── JsEngine.kt              # JS 引擎封装
├── AutoAccessibilityService.kt  # 无障碍服务
└── FloatingWindowService.kt    # 悬浮窗服务
```

## 注意事项

1. 需要 Android 7.0 (API 24) 及以上版本
2. 首次使用需要授予无障碍服务权限
3. 部分手机需要关闭"电池优化"才能正常使用后台服务
4. 坐标基于屏幕分辨率，不同手机可能需要调整

## License

MIT
