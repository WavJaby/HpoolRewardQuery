# HpoolRewardQuery
Only for chia
## Stuff can do
* get Hpool chia status
* get your capacity
* get reward per hour
* get reward per dat
* get all reward

## How to use
1. Download HpoolRewardQuery.zip and un zip to HpoolRewardQuery.jar
2. Go to hpool website https://www.hpool.co(login status required)
3. press F12 to open DevTool
4. go to Application tab to get auth_token

![](https://media.discordapp.net/attachments/832577590924935198/856091977766535168/unknown.png)

5. Go to the folder of `HpoolRewardQuery.jar` and runcommand: `java -jar HpoolRewardQuery.jar --token AUTH_TOKEN_YOU_GET`

## args(put after `HpoolRewardQuery.jar`)
* `-a` `--ALL` get all reward
* `--token TOKEN` your auth_token
* `-s` `--start` start time(not done yet)
* `-e` `--end` end time(not done yet)
* `-t` `--today` reward today(not done yet)

</br>
</br>

***

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
2. 進入你的Hpool網頁https://www.hpool.co(需要登入狀態)
3. 按F12打開DevTool
4. 到Application取得auth_token

![](https://media.discordapp.net/attachments/832577590924935198/856091977766535168/unknown.png)

5. 到`HpoolRewardQuery.jar`的資料夾執行`java -jar HpoolRewardQuery.jar --token 取得的auth_token`

## 變數(放在`HpoolRewardQuery.jar`後面)
* `-a` `--ALL` 取得所有收入
* `--token TOKEN` 放auth_token的地方
* `-s` `--start` 查詢開始時間(未完成)
* `-e` `--end` 查詢結束時間(未完成)
* `-t` `--today` 查詢今天的收入(未完成)
