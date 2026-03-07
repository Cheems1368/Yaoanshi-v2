# 🎨 UI颜色使用指南

## 🔑 核心品牌色

### 主品牌蓝
- **颜色值**: `#2979FF`
- **用途**: 主要按钮、交互高亮、重要元素
- **代码引用**: `@color/brand_primary`

### 辅色浅蓝
- **颜色值**: `#DBEAFF`
- **用途**: 背景渐变起点、次要元素
- **代码引用**: `@color/brand_secondary`

### 文字主色
- **颜色值**: `#333333`
- **用途**: 标题、主要内容文字
- **代码引用**: `@color/text_main`

## 📱 背景系统

### 背景渐变
- **起点**: `#DBEAFF` (浅蓝)
- **终点**: `#FFFFFF` (白色)
- **用途**: 页面背景
- **代码引用**: 
  - `@color/background_gradient_start`
  - `@color/background_gradient_end`
  - `@drawable/bg_gradient_background`

## 🎯 文字颜色系统

### 主文字色
- **颜色值**: `#333333`
- **用途**: 标题、主要内容
- **代码引用**: `@color/text_main`

### 次级文字色
- **颜色值**: `#556C8A`
- **用途**: 次要说明、提示文字
- **代码引用**: `@color/text_secondary`

### 提示文字色
- **颜色值**: `#BDBDBD`
- **用途**: 输入框提示、禁用状态
- **代码引用**: `@color/text_hint`

## 🔘 按钮系统

### 主要按钮
- **背景色**: `transparent`
- **边框色**: `#2979FF`
- **文字色**: `#2979FF`
- **用途**: 主要操作按钮
- **代码引用**: 
  - `@color/button_background`
  - `@color/button_border`
  - `@color/button_primary`
  - `@drawable/bg_button_primary`

## 💬 提示框系统

### 提示框背景
- **颜色值**: `#CFE7FF` (半透明)
- **用途**: 系统提示、消息提醒
- **代码引用**: `@color/toast_background`

### 提示框文字
- **颜色值**: `#556C8A`
- **用途**: 提示框内文字
- **代码引用**: `@color/toast_text`

## 🎨 使用示例

### 在布局文件中使用

```xml
<!-- 渐变背景 -->
<LinearLayout
    android:background="@drawable/bg_gradient_background"
    ... />

<!-- 主要按钮 -->
<Button
    android:background="@drawable/bg_button_primary"
    android:textColor="@color/button_primary"
    ... />

<!-- 提示框 -->
<TextView
    android:background="@drawable/bg_toast_message"
    android:textColor="@color/toast_text"
    ... />
```

### 在代码中使用

```kotlin
// 设置背景渐变
view.setBackgroundResource(R.drawable.bg_gradient_background)

// 设置按钮样式
button.setBackgroundResource(R.drawable.bg_button_primary)
button.setTextColor(ContextCompat.getColor(context, R.color.button_primary))

// 设置提示框样式
toastView.setBackgroundResource(R.drawable.bg_toast_message)
toastView.setTextColor(ContextCompat.getColor(context, R.color.toast_text))
```

### 在主题中使用

```xml
<style name="Button.Primary" parent="Widget.MaterialComponents.Button">
    <item name="backgroundTint">@color/button_background</item>
    <item name="strokeColor">@color/button_border</item>
    <item name="android:textColor">@color/button_primary</item>
</style>
```

## 📋 颜色对照表

| 名称 | 颜色值 | 用途 | 代码引用 |
|------|--------|------|----------|
| 主品牌蓝 | #2979FF | 主要按钮、交互高亮 | `@color/brand_primary` |
| 辅色浅蓝 | #DBEAFF | 背景渐变起点 | `@color/brand_secondary` |
| 文字主色 | #333333 | 标题、主要内容 | `@color/text_main` |
| 次级文字色 | #556C8A | 次要说明、提示 | `@color/text_secondary` |
| 按钮主色 | #2979FF | 按钮边框、文字 | `@color/button_primary` |
| 按钮背景 | transparent | 按钮背景 | `@color/button_background` |
| 提示框背景 | #CFE7FF | 系统提示、消息 | `@color/toast_background` |
| 提示框文字 | #556C8A | 提示框内文字 | `@color/toast_text` |

## 🎯 设计原则

1. **一致性**: 在整个应用中使用统一的颜色系统
2. **可读性**: 确保文字与背景有足够的对比度
3. **层次感**: 使用不同深浅的颜色创建视觉层次
4. **品牌识别**: 主品牌蓝色作为核心识别色
5. **用户体验**: 颜色应该增强而不是干扰用户体验

## 🔧 自定义颜色

如需添加新的颜色，请在 `colors.xml` 中添加：

```xml
<color name="custom_color_name">#HEXCODE</color>
```

然后在使用时引用：

```xml
android:color="@color/custom_color_name"
```

