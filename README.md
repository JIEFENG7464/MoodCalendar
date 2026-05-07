# 心情日历 · MoodCalendar

一个简洁、精致的心情记录 Android 应用。

## 功能

- **📅 日历记录** — 每天记录心情，支持一天多条
- **😊 20 种 SVG 表情** — 每个表情都有对应的精致 OpenMoji 图标
- **🔍 搜索筛选** — 可按文字搜索，也可按表情图标筛选
- **📊 统计页面** — 周/月/年维度查看心情分布
- **🎨 多主题** — 琥珀/水鸭绿/粉/橙 四种配色 + 深色模式
- **⏰ 每日提醒** — 定时提醒记录心情（支持 Android 12+ 精确闹钟）
- **💾 本地存储** — Room 数据库，数据不出手机

## 下载

👉 [Releases 页面](https://github.com/JIEFENG7464/MoodCalendar/releases) 下载最新 APK

## 技术栈

- **语言**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **架构**: MVVM (ViewModel + Room)
- **数据库**: Room (KSP)
- **图标**: OpenMoji SVG

## 构建

```bash
# 调试包
./gradlew assembleDebug

# 正式包（需要签名配置）
./gradlew assembleRelease
```

## 开源协议

[MIT](LICENSE)
