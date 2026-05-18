# 北洋之炬

> Kindle of Peiyang, abbreviated as KOP.

北洋之炬是面向天津大学教务场景的校园工具 App，聚焦课程表、教室查询、成绩与考试、入校码和课程通知。界面继续采用 Miuix 组件体系，并保留悬浮底栏与液态玻璃效果。

## 应用信息

- **应用名**：北洋之炬
- **英文名**：Kindle of Peiyang
- **简称**：KOP
- **包名**：`me.tju244.kop`
- **最低 SDK**：26
- **目标 SDK**：37

## 功能概览

- **OOBE 引导**：首次启动可配置天外天个人中心账号、教务网账号、界面偏好和通知偏好。
- **课程表**：展示当天全部课程，并提供完整课程表、自定义课程、本地持久化和冲突处理。
- **教室查询**：查询自习室与空教室状态，支持校区、教学楼、节次和空闲状态筛选。
- **成绩与考试**：展示最近考试、未安排考试、成绩列表和加权统计。
- **入校码**：绑定教务网账号后生成入校码。
- **课程通知**：支持精确闹钟、Android 16 Live Update 兼容样式和小米超级岛展示。
- **界面设置**：支持深浅色模式、OPPO Sans 内置字体、导航栏样式、液态玻璃效果和平板横屏导航位置。

## 技术栈

- Kotlin
- Jetpack Compose
- Miuix UI / Miuix Preference / Miuix Icons
- AndroidX Lifecycle / ViewModel / DataStore
- Retrofit 2 / OkHttp / Gson
- ZXing
- Shizuku API
- Focus Notification API

## 项目结构

```text
app/src/main/java/me/tju244/kop/
├── auth/          # 本地设置与账号状态
├── core/          # 网络基础设施
├── notification/  # 系统通知、课程提醒与超级岛桥接
├── tju/           # 教务网、成绩、考试、入校码、教室查询
└── ui/            # Compose 页面、Miuix 页面结构与视觉效果
```

## 常用命令

```powershell
.\gradlew.bat :app:compileDebugKotlin --no-daemon --stacktrace
.\gradlew.bat :app:assembleDebug
```

## 项目地址

- GitHub: [ImCZFy/Kindle-of-Peiyang](https://github.com/ImCZFy/Kindle-of-Peiyang)
- Issues: [问题反馈](https://github.com/ImCZFy/Kindle-of-Peiyang/issues)

## 致谢

- [twtstudio/WePeiYang-Flutter](https://github.com/twtstudio/WePeiYang-Flutter/)：天外天工作室维护的微北洋 Flutter 客户端，为校园服务功能和体验设计提供了重要参考。

---

最后更新：2026 年 5 月 18 日
