<img width="827" height="585" alt="image" src="https://github.com/user-attachments/assets/4986b1f8-02b9-4651-bba4-4e3e73ae72d8" />
> [!NOTE]  
> I've created a widget to show the battery of my phone and Bluetooth headphones simply because I saw a widget that did that and my phone didn't have one.

> [!TIP]
> Optional information to help a user be more successful.
> <img width="827" height="585" alt="image" src="https://github.com/user-attachments/assets/69a5d31a-bdb2-4a3f-a6ae-7adb4614b019" />


> [!IMPORTANT]  
> Crucial information necessary for users to succeed.

> [!WARNING]  
> Critical content demanding immediate user attention due to potential risks.

> [!CAUTION]
> Negative potential consequences of an action.


Battery Widget 🔋🎧
This is a minimalist and functional Android widget, inspired by a random design I saw, created to show the battery status of your phone and connected Bluetooth headphones directly on your home screen.

✨ Main Features
> The widget offers a clear and quick visualization of essential information:
> Dual Indicator: Simultaneously displays the battery percentage of the phone (left) and the Bluetooth headphones (right).
> Intuitive Visual Feedback:
> Active Charging: When the phone is charging, the main battery arc changes to a vibrant green color, and a ⚡ lightning bolt icon is displayed.
> Low Battery: If the phone or headphone battery drops below 20%, the corresponding arc changes to red for a quick warning.
> Minimalist Design: The dark capsule design integrates seamlessly with any dark theme or wallpaper.

💡 Project Motivation
> [!NOTE]
> I've created a widget to show the battery of my phone and Bluetooth headphones simply because I saw a similar widget on social media and my Android phone didn't have one by default. Now mine does!
🛠️ Technical Implementation
> The widget is implemented using standard Android `AppWidgetProvider` APIs, `BroadcastReceiver` to listen for system battery changes, and a custom Bitmap generator (`ArcBitmapGenerator.kt`) to dynamically draw the progress arcs and color changes.
> [!TIP]
> The design uses a horizontal `LinearLayout` for the main container and two nested vertical LinearLayouts for each device (phone and headphones), allowing for clean organization of icons, arcs, and percentages.
🚀 Usage and Installation
> [!IMPORTANT]
>This is a source code project. To use it, you must clone the repository, open it in Android Studio, and compile the APK on your device.
