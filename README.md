# AutoScript

一个简单易用的 Android 自动化脚本工具，类似 Auto.js，使用 JavaScript 编写自动化脚本。

[![Android CI](https://github.com/guozi9999/auto-script/actions/workflows/android.yml/badge.svg)](https://github.com/guozi9999/auto-script/actions/workflows/android.yml)

## ✨ 功能特性

### 基础操作
- ✅ 点击指定坐标
- ✅ 长按操作
- ✅ 滑动操作
- ✅ 输入文字
- ✅ 返回/主页/最近任务
- ✅ 延迟等待
- ✅ Toast 提示
- ✅ 日志输出

### 节点操作（基于无障碍服务）
- ✅ 通过文本查找并点击
- ✅ 通过 ID 查找并点击
- ✅ 设置输入框文本
- ✅ 查找文本/ID 元素

### 识图操作（需要截屏权限）
- ✅ 屏幕截图
- ✅ 模板匹配查找图片
- ✅ 查找并点击图片
- ✅ OCR 文字识别（支持中文）

### 颜色操作（需要截屏权限）
- ✅ 获取指定坐标颜色
- ✅ 查找指定颜色位置
- ✅ 颜色匹配判断

### 文件操作
- ✅ 读取/写入/追加文件
- ✅ 文件存在判断
- ✅ 删除/列出文件

### 网络操作
- ✅ HTTP GET 请求
- ✅ HTTP POST 请求

### 定时器
- ✅ setTimeout 延时执行
- ✅ setInterval 循环执行
- ✅ clearTimeout/clearInterval 清除定时器

### 其他功能
- ✅ 脚本编辑器
- ✅ 脚本保存/加载
- ✅ 悬浮窗控制
- ✅ 示例脚本库
- ✅ JSON 解析/序列化

## 📦 安装方法

### 方式一：下载 APK
1. 前往 [Releases](https://github.com/guozi9999/auto-script/releases) 页面
2. 下载最新的 `app-debug.apk`
3. 在手机上安装（需要开启"允许安装未知来源应用"）

### 方式二：编译安装
1. 克隆项目
```bash
git clone https://github.com/guozi9999/auto-script.git
```
2. 使用 Android Studio 打开项目
3. 连接手机或模拟器
4. 点击 Run 运行

## 🔧 权限设置

### 1. 无障碍服务（必需）
用于执行点击、滑动、节点查找等操作。

1. 打开 AutoScript 应用
2. 点击提示对话框中的「去设置」按钮
3. 在无障碍设置中找到「AutoScript」
4. 开启无障碍服务

### 2. 悬浮窗权限（可选）
用于显示悬浮控制按钮，方便在其他应用上层操作。

1. 点击应用中的「悬浮窗」按钮
2. 在弹出的设置页面中允许悬浮窗权限

### 3. 截屏权限（识图/颜色功能需要）
用于屏幕截图、图片查找、颜色识别等功能。

1. 点击应用中的「截屏」按钮
2. 在弹出的权限请求中点击「授权」

## 📖 使用方法

### 基本流程

1. **编写脚本**：在编辑区输入 JavaScript 脚本
2. **运行脚本**：点击「▶ 运行」按钮
3. **查看日志**：在日志区查看执行结果
4. **停止脚本**：点击「⏹ 停止」按钮

### 使用示例脚本

点击「示例」按钮，选择预设的示例脚本快速上手。

### 保存和加载脚本

- **保存**：输入文件名，点击「保存」按钮
- **加载**：点击「加载」按钮，选择已保存的脚本

### 积木编程

积木编程用于不直接手写 JavaScript 的场景。它会把每个积木按顺序转换成脚本代码，再发送到主界面运行或保存为 `.js` 文件。

#### 基本操作

1. 在主界面点击「积木编程 (可视化)」进入编辑器。
2. 点击「添加积木块」，选择点击、滑动、输入、等待、识图、颜色判断、条件分支等积木。
3. 点击列表中的积木可以编辑参数，例如坐标、文本、颜色值；图片路径和文件路径会从文件池中选择。
4. 长按积木可以删除。
5. 拖动积木可以调整执行顺序。
6. 顶部「生成代码」可以查看转换后的 JavaScript。
7. 顶部「保存」会保存为脚本文件，顶部「运行」会把生成的脚本发送到主界面执行。

#### 条件分支

条件分支用于表达“当某件事发生时做 A，否则做 B”。点击「添加积木块」后选择「条件分支」，再选择一个条件类型，系统会自动添加「条件」「否则执行」「结束条件分支」三块骨架，不需要手动配对。

常用条件积木：

- 「屏幕上出现文字」：当 `findText("文本")` 为 true 时执行成立分支。
- 「屏幕上没有文字」：当 `findText("文本")` 为 false 时执行成立分支。
- 「找到图片」：当 `findImage("图片路径")` 找到模板图时执行成立分支。
- 「没有找到图片」：当 `findImage("图片路径")` 未找到模板图时执行成立分支。
- 「位置颜色匹配」：当 `colorMatch(x, y, "#RRGGBB")` 为 true 时执行成立分支。
- 「文件存在」：当 `fileExists("文件路径")` 为 true 时执行成立分支。
- 「高级：自定义条件」：直接填写 JavaScript 条件表达式，例如 `count > 3`。

示例积木顺序：

```text
当屏幕上出现“登录”时，执行下面的积木
  点击文本：登录
否则执行下面的积木
  记录日志：没有找到登录按钮
结束这个条件分支
```

生成的脚本类似：

```javascript
if (findText("登录")) {
    clickText("登录");
} else {
    log("没有找到登录按钮");
}
```

如果条件积木没有正确配对，保存、运行和生成代码时会提示错误。

### 使用悬浮窗

1. 开启悬浮窗权限
2. 点击「悬浮窗」按钮启动悬浮窗
3. 悬浮窗提供快速运行/停止功能
4. 可以在其他应用上层操作

## 📚 API 参考

### 基础操作

#### `click(x, y)`
点击指定坐标。
```javascript
click(540, 960);  // 点击屏幕中央
```

#### `longClick(x, y, duration?)`
长按指定坐标。
```javascript
longClick(540, 960);          // 长按 1 秒
longClick(540, 960, 2000);    // 长按 2 秒
```

#### `swipe(x1, y1, x2, y2, duration?)`
从 (x1, y1) 滑动到 (x2, y2)。
```javascript
swipe(540, 1500, 540, 500);        // 上滑，300ms
swipe(540, 500, 540, 1500);        // 下滑
swipe(200, 960, 800, 960, 500);    // 右滑，500ms
```

#### `input(text)`
输入文字（需要先点击输入框）。
```javascript
click(540, 500);      // 点击输入框
sleep(300);           // 等待输入框获取焦点
input("你好");        // 输入文字
```

#### `back()`
按返回键。
```javascript
back();
```

#### `home()`
按主页键。
```javascript
home();
```

#### `recent()`
按最近任务键。
```javascript
recent();
```

#### `sleep(millis)`
延迟指定毫秒。
```javascript
sleep(1000);  // 延迟 1 秒
```

#### `toast(message)`
显示 Toast 提示。
```javascript
toast("操作完成");
```

#### `log(message)`
输出日志到日志区。
```javascript
log("开始执行...");
```

### 节点操作

#### `clickText(text)`
通过文本查找并点击元素。
```javascript
clickText("确定");
clickText("登录");
```

#### `clickId(id)`
通过资源 ID 查找并点击元素。
```javascript
clickId("com.example.app:id/btn_login");
```

#### `setTextById(id, text)`
设置指定 ID 输入框的文本。
```javascript
setTextById("com.example.app:id/et_username", "admin");
```

#### `findText(text)`
查找文本元素是否存在。
```javascript
if (findText("登录成功")) {
    log("已登录");
}
```

#### `findId(id)`
查找指定 ID 元素是否存在。
```javascript
if (findId("com.example.app:id/btn_submit")) {
    clickId("com.example.app:id/btn_submit");
}
```

### 识图操作

#### `findImage(templatePath, confidence?)`
在屏幕中查找图片，返回坐标对象 `{x, y}` 或 `null`。
```javascript
var pos = findImage("/sdcard/template.png");
if (pos) {
    log("找到位置: " + pos.x + ", " + pos.y);
}

// 指定置信度（0.0-1.0，默认 0.8）
var pos = findImage("/sdcard/button.png", 0.9);
```

#### `findAndClick(templatePath, confidence?)`
查找图片并点击，返回 `true`/`false`。
```javascript
if (findAndClick("/sdcard/button.png")) {
    log("点击成功");
} else {
    log("未找到");
}
```

### 颜色操作

#### `getColor(x, y)`
获取指定坐标的颜色，返回十六进制字符串。
```javascript
var color = getColor(540, 960);
log("颜色: " + color);  // 输出: #FF0000
```

#### `findColor(targetColor, left?, top?, right?, bottom?)`
查找指定颜色的位置，返回坐标对象或 `null`。
```javascript
var pos = findColor("#FF0000");
if (pos) {
    click(pos.x, pos.y);
}

// 在指定区域查找
var pos = findColor("#FF0000", 100, 100, 500, 500);
```

#### `colorMatch(x, y, targetColor, threshold?)`
判断指定坐标颜色是否匹配。
```javascript
if (colorMatch(540, 960, "#FF0000")) {
    log("颜色匹配");
}

// 指定容差（默认 30）
if (colorMatch(540, 960, "#FF0000", 50)) {
    log("颜色近似匹配");
}
```

### 文件操作

#### `readFile(path)`
读取文件内容。
```javascript
var content = readFile("/sdcard/config.txt");
log(content);
```

#### `writeFile(path, content)`
写入文件（覆盖）。
```javascript
writeFile("/sdcard/data.txt", "你好，自动脚本！");
```

#### `appendFile(path, content)`
追加内容到文件。
```javascript
appendFile("/sdcard/log.txt", "时间: " + new Date() + "\n");
```

#### `fileExists(path)`
判断文件是否存在。
```javascript
if (fileExists("/sdcard/config.txt")) {
    log("配置文件存在");
}
```

#### `deleteFile(path)`
删除文件。
```javascript
deleteFile("/sdcard/temp.txt");
```

#### `listFiles(path)`
列出目录下的文件。
```javascript
var files = listFiles("/sdcard/scripts");
log("文件数量: " + files.length);
```

### 网络操作

#### `httpGet(url)`
发送 GET 请求，返回 `{statusCode, body, success}`。
```javascript
var result = httpGet("https://api.example.com/data");
if (result.success) {
    log("响应: " + result.body);
}
```

#### `httpPost(url, body, contentType?)`
发送 POST 请求。
```javascript
var result = httpPost(
    "https://api.example.com/submit",
    '{"key": "value"}',
    "application/json"
);
log("状态码: " + result.statusCode);
```

### 定时器

#### `setTimeout(script, delay)`
延时执行脚本。
```javascript
setTimeout("click(540, 960);", 2000);  // 2 秒后点击
```

#### `setInterval(script, interval)`
循环执行脚本。
```javascript
var id = setInterval("log('tick');", 1000);  // 每秒执行
```

#### `clearTimeout(id)` / `clearInterval(id)`
清除定时器。
```javascript
var id = setInterval("log('tick');", 1000);
setTimeout("clearInterval('" + id + "');", 5000);  // 5 秒后停止
```

### JSON 操作

#### `JSON.parse(text)`
解析 JSON 字符串。
```javascript
var data = JSON.parse('{"name": "test", "value": 123}');
log(data.name);
```

#### `JSON.stringify(obj)`
序列化对象为 JSON 字符串。
```javascript
var json = JSON.stringify({name: "test", value: 123});
writeFile("/sdcard/data.json", json);
```

## 📝 示例脚本

### 连续点击
```javascript
for (var i = 0; i < 10; i++) {
    click(540, 960);
    log("第 " + (i+1) + " 次点击");
    sleep(200);
}
log("完成");
```

### 滑动浏览
```javascript
// 向下滑动 5 次
for (var i = 0; i < 5; i++) {
    swipe(540, 1500, 540, 500, 300);
    log("滑动 " + (i+1));
    sleep(1000);
}
```

### 通过文本点击
```javascript
// 点击"确定"按钮
if (clickText("确定")) {
    log("点击成功");
} else {
    log("未找到按钮");
}
```

### 识图点击
```javascript
// 查找并点击图片
var templatePath = "/sdcard/button.png";

for (var i = 0; i < 5; i++) {
    if (findAndClick(templatePath)) {
        log("第 " + (i+1) + " 次点击成功");
        sleep(500);
    } else {
        log("未找到图片，停止");
        break;
    }
}
```

### 颜色判断
```javascript
// 等待颜色变化
var targetColor = "#00FF00";
var x = 540, y = 960;

for (var i = 0; i < 30; i++) {
    if (colorMatch(x, y, targetColor)) {
        log("颜色已变化");
        click(x, y);
        break;
    }
    log("等待中... " + (i+1));
    sleep(1000);
}
```

### OCR 文字识别
```javascript
// 需要截屏权限
var text = ocrScreen();
log("识别结果: " + text);

if (text.indexOf("成功") >= 0) {
    log("识别到成功");
}
```

### 自动签到脚本
```javascript
// 打开应用
launchApp("com.example.app");
sleep(2000);

// 点击签到按钮
if (findAndClick("/sdcard/sign_in_btn.png")) {
    sleep(1000);
    
    // 确认签到
    if (clickText("确认")) {
        log("签到成功");
        appendFile("/sdcard/sign_log.txt", 
            new Date() + " 签到成功\n");
    }
} else {
    log("未找到签到按钮");
}
```

### 文件读写
```javascript
// 读取配置
var config = readFile("/sdcard/config.txt");
log("配置内容: " + config);

// 写入日志
var logContent = "执行时间: " + new Date() + "\n";
appendFile("/sdcard/app_log.txt", logContent);
```

### 网络请求
```javascript
// GET 请求
var result = httpGet("https://httpbin.org/get");
if (result.success) {
    var data = JSON.parse(result.body);
    log("IP: " + data.origin);
}

// POST 请求
var postData = JSON.stringify({name: "test", value: 123});
var result = httpPost("https://httpbin.org/post", postData);
log("响应: " + result.body);
```

## ⚠️ 注意事项

1. **系统要求**：Android 7.0 (API 24) 及以上版本
2. **无障碍服务**：首次使用需要手动开启
3. **电池优化**：部分手机需要关闭电池优化才能正常使用后台服务
4. **坐标适配**：坐标基于屏幕分辨率，不同手机可能需要调整
5. **截屏权限**：识图和颜色功能需要截屏权限，每次启动需要重新授权
6. **文件路径**：建议使用 `/sdcard/` 或应用私有目录

## 🛠️ 技术栈

- **语言**：Kotlin
- **JS 引擎**：Rhino 1.7.14
- **OCR**：Google ML Kit（支持中文）
- **构建工具**：Gradle 8.2
- **最低版本**：Android 7.0 (API 24)
- **目标版本**：Android 14 (API 34)

## 📁 项目结构

```
app/src/main/java/com/guozi/autoscript/
├── MainActivity.kt              # 主界面
├── JsEngine.kt                  # JS 引擎封装
├── ScriptRunner.kt              # 脚本执行器
├── AutoAccessibilityService.kt  # 无障碍服务
├── FloatingWindowService.kt     # 悬浮窗服务
├── ScreenCapture.kt             # 屏幕截图
├── ImageFinder.kt               # 图片查找
├── ColorHelper.kt               # 颜色操作
├── OcrHelper.kt                 # OCR 文字识别
├── NodeHelper.kt                # 节点操作
├── ScriptExtensions.kt          # 扩展功能（文件、网络、定时器）
├── ScriptManager.kt             # 脚本管理
├── ScriptDebugger.kt            # 脚本调试
├── GestureRecorder.kt           # 手势录制
├── SettingsManager.kt           # 设置管理
├── SettingsActivity.kt          # 设置界面
├── TutorialManager.kt           # 教程管理
├── CodeEditor.kt                # 代码编辑器
└── ScriptPacker.kt              # 脚本打包
```

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

1. Fork 本项目
2. 创建功能分支：`git checkout -b feature/your-feature`
3. 提交更改：`git commit -m 'Add your feature'`
4. 推送分支：`git push origin feature/your-feature`
5. 提交 Pull Request

## 📄 License

[MIT License](LICENSE)

## 🔗 相关链接

- [GitHub 仓库](https://github.com/guozi9999/auto-script)
- [问题反馈](https://github.com/guozi9999/auto-script/issues)
- [Release 下载](https://github.com/guozi9999/auto-script/releases)
