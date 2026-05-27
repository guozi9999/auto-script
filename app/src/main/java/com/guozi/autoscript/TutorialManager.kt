package com.guozi.autoscript

import android.content.Context
import android.util.Log

/**
 * 教程管理器
 * 提供内置教程和示例脚本
 */
class TutorialManager(private val context: Context) {
    
    companion object {
        private const val TAG = "TutorialManager"
    }
    
    /**
     * 获取所有教程
     */
    fun getTutorials(): List<Tutorial> {
        return listOf(
            Tutorial(
                id = "basics",
                title = "基础入门",
                description = "学习 AutoScript 的基本操作",
                steps = listOf(
                    TutorialStep(
                        title = "点击操作",
                        description = "使用 click(x, y) 函数点击屏幕上的指定位置",
                        code = """
                            // 点击屏幕中央
                            click(540, 960);
                            log("点击完成");
                        """.trimIndent()
                    ),
                    TutorialStep(
                        title = "滑动操作",
                        description = "使用 swipe(x1, y1, x2, y2) 函数滑动屏幕",
                        code = """
                            // 从下往上滑动（上滑）
                            swipe(540, 1500, 540, 500, 300);
                            log("上滑完成");
                            
                            sleep(1000);
                            
                            // 从上往下滑动（下滑）
                            swipe(540, 500, 540, 1500, 300);
                            log("下滑完成");
                        """.trimIndent()
                    ),
                    TutorialStep(
                        title = "输入文字",
                        description = "使用 input(text) 函数在输入框中输入文字",
                        code = """
                            // 先点击输入框
                            click(540, 500);
                            sleep(300);
                            
                            // 输入文字
                            input("Hello AutoScript!");
                            log("输入完成");
                        """.trimIndent()
                    ),
                    TutorialStep(
                        title = "按键操作",
                        description = "使用 back()、home()、recent() 函数模拟按键",
                        code = """
                            // 返回键
                            back();
                            sleep(500);
                            
                            // 主页键
                            home();
                            sleep(500);
                            
                            // 最近任务
                            recent();
                            log("按键操作完成");
                        """.trimIndent()
                    )
                )
            ),
            
            Tutorial(
                id = "control_flow",
                title = "流程控制",
                description = "学习循环和条件判断",
                steps = listOf(
                    TutorialStep(
                        title = "循环点击",
                        description = "使用 for 循环重复执行点击操作",
                        code = """
                            // 循环点击 5 次
                            for (var i = 0; i < 5; i++) {
                                click(540, 960);
                                log("第 " + (i+1) + " 次点击");
                                sleep(500);
                            }
                            log("循环完成");
                        """.trimIndent()
                    ),
                    TutorialStep(
                        title = "条件判断",
                        description = "使用 if/else 进行条件判断",
                        code = """
                            // 获取颜色
                            var color = getColor(540, 960);
                            log("颜色: " + color);
                            
                            // 判断颜色
                            if (colorMatch(540, 960, "#FF0000")) {
                                log("是红色");
                            } else if (colorMatch(540, 960, "#00FF00")) {
                                log("是绿色");
                            } else {
                                log("是其他颜色");
                            }
                        """.trimIndent()
                    ),
                    TutorialStep(
                        title = "等待条件",
                        description = "使用 while 循环等待某个条件满足",
                        code = """
                            // 等待颜色变化
                            var maxWait = 10; // 最多等待10次
                            var count = 0;
                            
                            while (!colorMatch(540, 960, "#00FF00") && count < maxWait) {
                                log("等待变绿... " + (count+1));
                                sleep(1000);
                                count++;
                            }
                            
                            if (count < maxWait) {
                                log("已变绿！");
                            } else {
                                log("等待超时");
                            }
                        """.trimIndent()
                    )
                )
            ),
            
            Tutorial(
                id = "node_operations",
                title = "节点操作",
                description = "学习通过文本和ID操作UI元素",
                steps = listOf(
                    TutorialStep(
                        title = "点击文本",
                        description = "使用 clickText(text) 通过文本查找并点击按钮",
                        code = """
                            // 查找并点击"确定"按钮
                            if (clickText("确定")) {
                                log("点击确定成功");
                            } else {
                                log("未找到确定按钮");
                            }
                            
                            sleep(500);
                            
                            // 查找并点击"取消"按钮
                            clickText("取消");
                        """.trimIndent()
                    ),
                    TutorialStep(
                        title = "点击ID",
                        description = "使用 clickId(id) 通过资源ID查找并点击元素",
                        code = """
                            // 通过ID点击按钮
                            var success = clickId("com.example.app:id/btn_submit");
                            if (success) {
                                log("点击成功");
                            } else {
                                log("未找到按钮");
                            }
                        """.trimIndent()
                    ),
                    TutorialStep(
                        title = "输入文本",
                        description = "使用 setTextById(id, text) 设置输入框文本",
                        code = """
                            // 设置输入框文本
                            setTextById("com.example.app:id/et_input", "Hello World");
                            log("文本设置完成");
                        """.trimIndent()
                    ),
                    TutorialStep(
                        title = "查找元素",
                        description = "使用 findText(text) 和 findId(id) 查找元素是否存在",
                        code = """
                            // 查找文本
                            if (findText("登录")) {
                                log("找到登录按钮");
                                clickText("登录");
                            }
                            
                            // 查找ID
                            if (findId("com.example.app:id/btn_login")) {
                                log("找到登录按钮");
                                clickId("com.example.app:id/btn_login");
                            }
                        """.trimIndent()
                    )
                )
            ),
            
            Tutorial(
                id = "image_recognition",
                title = "识图操作",
                description = "学习图片查找和识别",
                steps = listOf(
                    TutorialStep(
                        title = "查找图片",
                        description = "使用 findImage(path) 在屏幕上查找图片位置",
                        code = """
                            // 查找图片
                            var pos = findImage("/sdcard/template.png");
                            if (pos) {
                                log("找到图片位置: " + pos.x + ", " + pos.y);
                            } else {
                                log("未找到图片");
                            }
                        """.trimIndent()
                    ),
                    TutorialStep(
                        title = "查找并点击",
                        description = "使用 findAndClick(path) 查找图片并点击",
                        code = """
                            // 查找并点击图片
                            var found = findAndClick("/sdcard/button.png");
                            if (found) {
                                log("点击成功");
                            } else {
                                log("未找到图片");
                            }
                        """.trimIndent()
                    ),
                    TutorialStep(
                        title = "查找多个图片",
                        description = "使用 findAllImages(path) 查找所有匹配的图片",
                        code = """
                            // 查找所有匹配的图片
                            var images = findAllImages("/sdcard/icon.png");
                            log("找到 " + images.length + " 个匹配");
                            
                            // 依次点击
                            for (var i = 0; i < images.length; i++) {
                                click(images[i].x, images[i].y);
                                log("点击第 " + (i+1) + " 个");
                                sleep(500);
                            }
                        """.trimIndent()
                    )
                )
            ),
            
            Tutorial(
                id = "color_operations",
                title = "颜色操作",
                description = "学习颜色识别和匹配",
                steps = listOf(
                    TutorialStep(
                        title = "获取颜色",
                        description = "使用 getColor(x, y) 获取指定坐标的颜色",
                        code = """
                            // 获取颜色
                            var color = getColor(540, 960);
                            log("颜色: " + color);
                        """.trimIndent()
                    ),
                    TutorialStep(
                        title = "颜色匹配",
                        description = "使用 colorMatch(x, y, hex) 判断颜色是否匹配",
                        code = """
                            // 判断颜色是否匹配
                            if (colorMatch(540, 960, "#FF0000")) {
                                log("颜色匹配红色");
                            } else {
                                log("颜色不匹配");
                            }
                        """.trimIndent()
                    ),
                    TutorialStep(
                        title = "查找颜色",
                        description = "使用 findColor(hex) 在屏幕上查找指定颜色的位置",
                        code = """
                            // 查找红色
                            var pos = findColor("#FF0000");
                            if (pos) {
                                log("找到红色位置: " + pos.x + ", " + pos.y);
                                click(pos.x, pos.y);
                            } else {
                                log("未找到红色");
                            }
                        """.trimIndent()
                    )
                )
            ),
            
            Tutorial(
                id = "file_operations",
                title = "文件操作",
                description = "学习文件读写操作",
                steps = listOf(
                    TutorialStep(
                        title = "写入文件",
                        description = "使用 writeFile(path, content) 写入文件",
                        code = """
                            // 写入文件
                            writeFile("/sdcard/test.txt", "Hello AutoScript!");
                            log("写入完成");
                        """.trimIndent()
                    ),
                    TutorialStep(
                        title = "读取文件",
                        description = "使用 readFile(path) 读取文件内容",
                        code = """
                            // 读取文件
                            var content = readFile("/sdcard/test.txt");
                            if (content) {
                                log("文件内容: " + content);
                            } else {
                                log("读取失败");
                            }
                        """.trimIndent()
                    ),
                    TutorialStep(
                        title = "追加内容",
                        description = "使用 appendFile(path, content) 追加内容到文件",
                        code = """
                            // 追加内容
                            appendFile("/sdcard/log.txt", "时间: " + new Date().toString() + "\n");
                            log("追加完成");
                        """.trimIndent()
                    )
                )
            ),
            
            Tutorial(
                id = "practical_examples",
                title = "实战案例",
                description = "学习实际应用场景",
                steps = listOf(
                    TutorialStep(
                        title = "自动签到",
                        description = "自动打开应用并签到",
                        code = """
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
                        """.trimIndent()
                    ),
                    TutorialStep(
                        title = "自动刷视频",
                        description = "自动滑动浏览短视频",
                        code = """
                            // 自动刷视频脚本
                            var count = 0;
                            var maxCount = 20;
                            
                            while (count < maxCount) {
                                // 上滑切换下一个视频
                                swipe(540, 1500, 540, 500, 300);
                                log("第 " + (count+1) + " 个视频");
                                
                                // 随机等待 3-8 秒
                                var waitTime = 3000 + Math.floor(Math.random() * 5000);
                                sleep(waitTime);
                                
                                count++;
                            }
                            
                            log("刷视频完成");
                        """.trimIndent()
                    ),
                    TutorialStep(
                        title = "定时任务",
                        description = "使用定时器执行周期任务",
                        code = """
                            // 每隔5秒执行一次
                            var count = 0;
                            var timerId = setInterval("count++; log('执行第 ' + count + ' 次'); click(540, 960);", 5000);
                            
                            // 30秒后停止
                            setTimeout("clearInterval('" + timerId + "'); log('任务结束');", 30000);
                            
                            log("定时任务已启动");
                        """.trimIndent()
                    )
                )
            )
        )
    }
    
    /**
     * 获取教程
     */
    fun getTutorial(id: String): Tutorial? {
        return getTutorials().find { it.id == id }
    }
    
    /**
     * 教程
     */
    data class Tutorial(
        val id: String,
        val title: String,
        val description: String,
        val steps: List<TutorialStep>
    )
    
    /**
     * 教程步骤
     */
    data class TutorialStep(
        val title: String,
        val description: String,
        val code: String
    )
}
