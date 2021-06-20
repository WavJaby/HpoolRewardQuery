# HpoolRewardQuery
只限定Chia的查詢
## 功能
* Hpool的Chia池狀態
* 你的總容量
* 一小時的收入
* 預計一天的收入
* 總收入

## 使用步驟
1. 下載HpoolRewardQuery.zip並解壓成HpoolRewardQuery.jar
2. 進入你的Hpool網頁(需要登入狀態)
3. 按F12打開DevTool
4. 到Application取得auth_token

![](https://media.discordapp.net/attachments/832577590924935198/856091977766535168/unknown.png)

5. 到HpoolRewardQuery.jar的資料夾執行`java -jar HpoolRewardQuery.jar --token 取得的auth_token`

## 變數
* `-a` `--ALL` 取得所有收入
* `--token TOKEN` 放auth_token的地方
* `-s` `--start` 查詢開始時間(未完成)
* `-e` `--end` 查詢結束時間(未完成)
* `-t` `--today` 查詢今天的收入(未完成)
