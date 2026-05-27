/**
 * AutoScript 完整 API 参考
 * 
 * ==================== 基础操作 ====================
 * click(x, y) - 点击坐标
 * longClick(x, y, duration) - 长按坐标
 * swipe(x1, y1, x2, y2, duration) - 滑动
 * input(text) - 输入文字
 * back() - 返回键
 * home() - 主页键
 * recent() - 最近任务
 * sleep(millis) - 延迟（毫秒）
 * log(message) - 输出日志
 * toast(message) - 显示提示
 * 
 * ==================== 节点操作 ====================
 * clickText(text) - 通过文本查找并点击
 * clickId(id) - 通过ID查找并点击
 * setTextById(id, text) - 设置输入框文本
 * findText(text) - 查找文本是否存在
 * findId(id) - 查找ID是否存在
 * 
 * ==================== 识图操作 ====================
 * findImage(path, confidence) - 查找图片位置
 * findAndClick(path, confidence) - 查找并点击图片
 * findAllImages(path, confidence) - 查找所有匹配图片
 * 
 * ==================== 颜色操作 ====================
 * getColor(x, y) - 获取指定坐标颜色
 * findColor(hex, left, top, right, bottom) - 查找颜色位置
 * colorMatch(x, y, hex, threshold) - 判断颜色是否匹配
 * 
 * ==================== 文件操作 ====================
 * readFile(path) - 读取文件内容
 * writeFile(path, content) - 写入文件
 * appendFile(path, content) - 追加到文件
 * fileExists(path) - 检查文件是否存在
 * deleteFile(path) - 删除文件
 * listFiles(path) - 列出目录下的文件
 * 
 * ==================== HTTP 请求 ====================
 * httpGet(url) - 发送 GET 请求
 * httpPost(url, body, contentType) - 发送 POST 请求
 * 
 * ==================== 定时器 ====================
 * setTimeout(script, delay) - 延时执行脚本
 * setInterval(script, interval) - 循环执行脚本
 * clearTimeout(id) - 清除定时器
 * clearInterval(id) - 清除循环定时器
 * 
 * ==================== JSON 操作 ====================
 * JSON.parse(str) - 解析 JSON 字符串
 * JSON.stringify(obj) - 转换为 JSON 字符串
 */

// ==================== 示例1: 基础自动化 ====================

// 打开应用
click(540, 960);
sleep(1000);

// 点击按钮
clickText("确定");
sleep(500);

// 输入文字
input("Hello AutoScript!");
sleep(300);

// 返回
back();
sleep(500);

// ==================== 示例2: 识图自动化 ====================

// 查找并点击图片
var found = findAndClick("/sdcard/button.png");
if (found) {
    log("点击成功");
    sleep(1000);
} else {
    log("未找到按钮");
}

// 查找所有匹配图片并点击
var images = findAllImages("/sdcard/icon.png");
log("找到 " + images.length + " 个图标");
for (var i = 0; i < images.length; i++) {
    click(images[i].x, images[i].y);
    sleep(500);
}

// ==================== 示例3: 颜色判断自动化 ====================

// 获取颜色
var color = getColor(540, 960);
log("当前颜色: " + color);

// 等待颜色变化
while (!colorMatch(540, 960, "#00FF00")) {
    log("等待变绿...");
    sleep(1000);
}
log("已变绿！");

// 查找红色按钮并点击
var redBtn = findColor("#FF0000");
if (redBtn) {
    click(redBtn.x, redBtn.y);
}

// ==================== 示例4: 文件操作 ====================

// 读取配置
var config = readFile("/sdcard/config.json");
if (config) {
    var data = JSON.parse(config);
    log("用户名: " + data.username);
}

// 保存日志
writeFile("/sdcard/log.txt", "开始执行: " + new Date().toString());
appendFile("/sdcard/log.txt", "\n执行完成");

// 列出文件
var files = listFiles("/sdcard/scripts");
for (var i = 0; i < files.length; i++) {
    log("文件: " + files[i]);
}

// ==================== 示例5: HTTP 请求 ====================

// GET 请求
var response = httpGet("https://api.example.com/data");
if (response.success) {
    var data = JSON.parse(response.body);
    log("获取数据成功");
}

// POST 请求
var postData = JSON.stringify({name: "test", value: 123});
var postResult = httpPost("https://api.example.com/submit", postData, "application/json");
log("提交结果: " + postResult.statusCode);

// ==================== 示例6: 定时器 ====================

// 延时执行
setTimeout("log('3秒后执行')", 3000);

// 循环执行
var count = 0;
var timerId = setInterval("count++; log('第 ' + count + ' 次')", 2000);

// 5秒后停止
setTimeout("clearInterval('" + timerId + "'); log('已停止')", 10000);

// ==================== 示例7: 综合应用 ====================

// 自动签到脚本
function autoSign() {
    // 打开应用
    home();
    sleep(500);
    
    // 查找并点击应用图标
    if (findAndClick("/sdcard/app_icon.png")) {
        sleep(2000);
        
        // 查找签到按钮
        if (clickText("签到")) {
            sleep(1000);
            
            // 确认签到
            clickText("确认");
            sleep(500);
            
            // 保存签到记录
            var now = new Date().toString();
            appendFile("/sdcard/sign_log.txt", now + " 签到成功\n");
            
            toast("签到成功！");
        } else {
            log("未找到签到按钮");
        }
    } else {
        log("未找到应用图标");
    }
    
    home();
}

// 执行签到
autoSign();
