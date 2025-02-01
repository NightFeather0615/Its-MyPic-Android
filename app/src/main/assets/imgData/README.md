# MyPicDB
用於存放MyPic系列背後資料的repo

## 使用方式

### 讀取data.json
其中的資料格式為list\[dict\]
字典中具體格式如下
```json
  {
    "text": "小祥",
    "episode": "1-3",
    "frame_start": 752,
    "frame_end": 768,
    "segment_id": 0
  },
```
### 讀取對應圖片
預期檔案名為
> `{episode}_{frame_start}.jpg`

若想直接存取可以請求
> `https://media.githubusercontent.com/media/jeffpeng3/MyPicDB/assets/images/{episode}_{frame_start}.jpg`
