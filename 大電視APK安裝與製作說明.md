# 📺 Mbcast 大電視 Android 專用端 (APK) 方案說明書

這套專案已完整為您寫好大電視專屬的 Android App 核心程式碼：
- **專案路徑**：`d:/PythonTest/Mbcast/android_tv_agent/`
- **核心功能**：
  1. 📱 **全螢幕無邊框沉浸式接收**：自動隱藏電視系統列與導航鍵，鎖定橫向顯示。
  2. ⚡ **螢幕常亮保證 (FLAG_KEEP_SCREEN_ON)**：防止大電視播放到一半進入休眠黑屏。
  3. 🚀 **開機自動啟動 (Boot Completed)**：學校每天早晨大電視通電開機，自動啟動常駐。
  4. 📡 **UDP 8080 心跳通報**：電視會自動向老師主控端發送 `HELLO` 封包，主控台表格自動顯示「🟢 大電視 在線」。
  5. 🎮 **遙控器選單支援**：按遙控器「Menu / 設定鍵」可隨時修改連線的主控端電腦 IP。

---

## 🛠️ 如何取得 / 產出 `.apk` 檔案？（3 種方式）

### 🌟 方案一：免裝 SDK，使用「線上 1 分鐘轉 APK 工具」（最推薦、最快）
大電視的核心運作本質是載入主控端的 `http://<主控端IP>:5050/media`，有非常成熟的免費線上工具能 1 分鐘直接轉出 APK：

1. **推薦工具**：
   - **Website 2 APK Builder** (Windows 免費免裝 SDK 工具)
   - **PWABuilder** (https://www.pwabuilder.com/ - 微軟官方出品，輸入網址直接下載 APK)
   - **AppsGeyser** (https://appsgeyser.com/ - 選擇 Website 轉 App，30 秒產出 APK)
2. **填寫參數**：
   - 網站網址 (URL)：`http://<您的主控電腦IP>:5050/media`
   - App 名稱：`Mbcast 大電視端`
3. 點擊生成後，直接下載 `.apk` 檔案，存入隨身碟即可！

---

### 💻 方案二：使用本目錄的完整 Android 原生專案編譯
若您或學校資訊組電腦有安裝 **Android Studio**：
1. 開啟 Android Studio，選擇 **Open an Existing Project**。
2. 選擇目錄：`d:\PythonTest\Mbcast\android_tv_agent`。
3. 點擊頂部功能表：**Build** ➔ **Build Bundle(s) / APK(s)** ➔ **Build APK(s)**。
4. 幾秒鐘後即可在 `app/build/outputs/apk/debug/app-debug.apk` 取得正式 APK！

---

### 🌐 方案三：大電視「PWA 網頁捷徑法」（免任何 APK，效果 100% 相同）
許多學校其實**完全不需要大費周章裝 APK**，直接利用 Android TV 內建功能：
1. 大電視打開 Chrome 瀏覽器，前往：`http://<主控端IP>:5050/media`。
2. 點擊瀏覽器右上角選單（三個點）➔ 點選 **「新增至主螢幕」 (Add to Home screen)** 或 **「安裝應用程式」**。
3. 大電視主畫面就會直接出現一個 **「Mbcast 學生端接收台」** 的獨立 App 圖示！
4. 點開後就是**獨立全螢幕視窗（無網址列、無按鈕）**，體驗與安裝 APK 完全一模一樣！
