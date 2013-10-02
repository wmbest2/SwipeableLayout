SwipeableLayout
==============

A swipeable - auto resizing view group for android

Usage
---

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
