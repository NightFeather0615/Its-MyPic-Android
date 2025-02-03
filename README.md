# It's MyPic!!!!! for Android
快速搜尋 MyGo!!!!! & Ave Mujica 圖片

## Demo
https://github.com/user-attachments/assets/039c424a-abfe-4cc4-b3db-d90e0e75b362

## Download
* [GitHub Release](https://github.com/NightFeather0615/Its-MyPic-Android/releases/latest)

## Usage
* 喚醒
  * 點擊「開始溝通」啟動常駐背景服務
    * 常駐服務運行時，點擊通知可開啟覆蓋層 
  * 點擊[快速設定方塊](https://support.google.com/android/answer/9083864)開啟單次覆蓋層
    * 點擊「新增快速設定方塊」新增方塊
* 互動
  * 在搜尋框輸入台詞即可開始搜尋
    * 預設詞語替換
      * Uppercase -> Lowercase
      * 妳 -> 你 / 你 -> 妳
      * `\n`, `,`, ` ` -> `Empty`
  * 圖片
    * 點按複製圖片
    * 長按下載圖片 (`/Downloads/ItsMyPic`)
  * 點擊空白處可關閉覆蓋層

## Troubleshooting
* [系統已拒絕將存取權授予應用程式 / 受限制的設定](https://support.google.com/android/answer/12623953)
  1. 在 Android 裝置上開啟「設定」應用程式。
  2. 輕觸「應用程式」。
  3. 輕觸要解除受限制設定的應用程式。
     * 提示：如果找不到所需應用程式，請先輕觸「查看所有應用程式」或「應用程式資訊」。
  4. 依序輕觸「更多」圖示 更多 下一步「解除受限制的設定」。
  5. 按照畫面上的指示操作。

## Roadmap
* [x] Download Image
* [ ] Search Histroy
* [ ] Improved Search Algo
