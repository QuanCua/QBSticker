QBSticker
=========

A view which can add Text and zoom,drag,rotate,delete it

## Screenshots
![](https://github.com/QuanCua/QBSticker/blob/main/screenshots/example1.jpg)

## Setting Gradle
1. Add the JitPack repository to your build file
```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
	
2. Add the dependency
```
dependencies {
	implementation 'com.github.QuanCua:QBSticker:1.0'
}
```     

## Usage
**Tips**: QBStickerView extends FrameLayout
#### In layout
```xml
<com.quanbd.qbsticker.QBStickerView
        android:id="@+id/qbStickerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:isEnableTransformIcon="true"
        app:isEnableDeleteIcon="true"
        app:lineSelectedColor="#ffffff"/>
```

## Attributes
```
app:isEnableTransformIcon: turn ON transform icon (default: true)
app:isEnableDeleteIcon: turn ON delete icon (default: true)
app:lineSelectedColor: change border line color (default: white)
```

## Options
```java
qbStickerView.addText()
qbStickerView.duplicateText()
qbStickerView.updateSize(width, height)
```
You can change textColor, textSize, alignment...just like a normal TextView

Also you can custom the icon:
```java
qbStickerView.setTransformIcon(R.drawable.duoibau1)
qbStickerView.setDeleteIcon(R.drawable_duoibau2)
```
