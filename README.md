# Battery Widget 🔋🎧

This is a minimalist and functional Android widget, inspired by a random design I saw, created to show the battery status of your phone and connected Bluetooth headphones directly on your home screen.

## ✨ Main Features

The widget offers a clear and quick visualization of essential information:

*   **Dual Indicator:** Simultaneously displays the battery percentage of the **phone** (left) and the **Bluetooth headphones** (right).
*   **Intuitive Visual Feedback:**
    *   **Active Charging:** When the phone is charging, the arc changes to a **vibrant green** color, and a **⚡ lightning bolt** icon is displayed.
    *   **Low Battery:** If the battery drops below **20%**, the arc changes to **red** for a quick warning.
*   **Minimalist Design:** The dark capsule design integrates seamlessly with any dark theme or wallpaper.

## 💡 Project Motivation

> [!NOTE]
> I've created a widget to show the battery of my phone and Bluetooth headphones simply because I saw a similar widget on social media and my Android phone didn't have one by default. Now mine does!

## 🛠️ Technical Implementation

The widget is implemented using standard Android `AppWidgetProvider` APIs, `BroadcastReceiver` to listen for system battery changes, and a custom Bitmap generator (`ArcBitmapGenerator.kt`) to dynamically draw the progress arcs and color changes.

> [!TIP]
> The design uses a horizontal `LinearLayout` for the main container and two nested vertical `LinearLayouts` for each device (phone and headphones), allowing for clean organization of icons, arcs, and percentages.

## 🚀 Usage and Installation

> [!IMPORTANT]
> This is a source code project. To use it, you must clone the repository, open it in **Android Studio**, and compile the APK on your device.
