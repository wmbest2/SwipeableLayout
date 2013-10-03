SwipeableLayout
==============

A swipeable - auto resizing view group for android

Usage
---

build.gradle

```groovy
compile 'com.wmbest.widget:swipeable-layout:1.0.+@aar'
```

-- or --

pom.xml

```xml
<dependency>
  <groupId>com.wmbest.widget</groupId>
  <artifactId>swipeable-layout</artifactId>
  <version>1.0.+</version>
  <type>aar</type>
</dependency>
```

layout.xml

```xml
<com.wmbest.widget.SwipeableLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    xmlns:swipe="http://schemas.android.com/apk/res-auto"
    swipe:frontView="@+id/front"
    swipe:backView="@+id/back"
    swipe:direction="right"
    >
    <View
        android:id="@id/back"
        android:layout_width="match_parent"
        android:layout_height="200dip"
        android:background="#ff00ff00"
        />
    <View
        android:id="@id/front"
        android:layout_width="match_parent"
        android:layout_height="45dip"
        android:background="#ff0000ff"
        />
</com.wmbest.widget.SwipeableLayout>
```

Attributes
---
 * frontView - The Swipeable View - **required**
 * backView  - The Background View - **required**
 * tabView - This View is used to simulate the background view 
    when the frontView is closed but doesn't cover
 * direction - Swipe Direction (Default left)
 * peekSize - Amount to keep from sliding over
 * grabSize - Amount of touch area for swiping closed

Demo
---
![Imgur](http://i.imgur.com/mmHBRob.gif)

```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:paddingLeft="@dimen/activity_horizontal_margin"
    android:paddingRight="@dimen/activity_horizontal_margin"
    android:paddingTop="@dimen/activity_vertical_margin"
    android:paddingBottom="@dimen/activity_vertical_margin"
    android:orientation="vertical"
    tools:context=".MainActivity">

    <com.wmbest.widget.SwipeableLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        xmlns:swipe="http://schemas.android.com/apk/res-auto"
        swipe:frontView="@+id/front"
        swipe:backView="@+id/back"
        swipe:direction="right"
        >
        <View
            android:id="@id/back"
            android:layout_width="match_parent"
            android:layout_height="200dip"
            android:background="#ff00ff00"
            />
        <View
            android:id="@id/front"
            android:layout_width="match_parent"
            android:layout_height="45dip"
            android:background="#ff0000ff"
            />
    </com.wmbest.widget.SwipeableLayout>


    <com.wmbest.widget.SwipeableLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="20dip"
        xmlns:swipe="http://schemas.android.com/apk/res-auto"
        swipe:frontView="@+id/front"
        swipe:backView="@+id/back"
        >
        <TextView
            android:id="@id/back"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:padding="40dip"
            android:background="#ff0000ff"
            android:text="BACK" />
        <TextView
            android:id="@id/front"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:background="#7f00ff00"
            android:text="FRONT" />
    </com.wmbest.widget.SwipeableLayout>


</LinearLayout>
```
