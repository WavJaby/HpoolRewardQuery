package com.hpool;

import com.wavjaby.json.JsonArray;
import com.wavjaby.json.JsonObject;
import util.DateToMillis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static com.hpool.RevenueStatus.SETTLEMENT;
import static com.hpool.RevenueStatus.UNSETTLEMENT;

public class HPool {
    private String token;
    private boolean all = false;
    private boolean today = false;
    private long startTime = -1, endTime = -1;
    private final String newL = System.lineSeparator();

    HPool(String[] args) {
        DateToMillis dtm = new DateToMillis("_", "/", ":");
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-s":
                case "--start":
                    startTime = dtm.dateToMillis(args[++i]);
                    break;
                case "-e":
                case "--end":
                    endTime = dtm.dateToMillis(args[++i]);
                    break;
                case "-a":
                case "--ALL":
                    all = true;
                    break;
                case "-t":
                case "--today":
                    today = true;
                    break;
                case "--token":
                    token = args[++i];
                    break;
                default:
                    System.out.println("unknown arg: " + args[i]);
                    break;

            }
        }
        buildCookie();

        //取得Hpool資訊
        System.out.println(getHpoolCoinInfo());

        //取得plot資料
        String plotInfo = getOnlinePlotInfo();
        if (plotInfo == null)
            return;
        System.out.println(plotInfo);

        //取得收入
        System.out.println(getRevenue());

//        System.out.println(newL + "item get: " + getItemCount);
    }

    private String getRevenue() {
        StringBuilder outPut = new StringBuilder();

        //取得收入資料
        int pageCount = 1;
        int total;
        int numberInPage;
        System.out.print("\rget page 0/*");
        do {
            String result = getDataFromUrl("https://www.hpool.co/api/pool/miningdetail?language=zh&type=chia&count=100&page=" + (pageCount++), true);
            if (result == null)
                return null;

            //process result
            JsonObject jsonObject = new JsonObject(result);
            JsonObject data = jsonObject.get("data");

            total = data.getInteger("total");
            numberInPage = data.getInteger("page");

            System.out.print("\rget page " + (pageCount - 1) + "/" + (total / numberInPage + 1));
            if (processData(data.getJsonArray("list")))
                break;

        } while (((pageCount - 1) * numberInPage) < total);
        System.out.println();

        //取得已結算
        if (all) getSettlement();

        //印出資料
        String message;
        long timePassSecond;
        if (all) {
            message = "";
            //time pass
            timePassSecond = (rewardEndTime - rewardStartTime);
        } else {
            message = "Since last settlement, ";
            //time pass
            timePassSecond = (System.currentTimeMillis() / 1000 - rewardStartTime);
        }
        outPut.append(message).append("pass ").append(calculateTimeLeft(timePassSecond)).append(newL);


        // allTime(sec) / hour(sec)
        double perHour = ((double) timePassSecond / (60 * 60));
        outPut.append("reward per hour: ").append(String.format("%,.8f", chaiReward / perHour)).append(newL);
        outPut.append("reward per day: ").append(String.format("%,.8f", (chaiReward / perHour) * 24)).append(newL);

        //total
        outPut.append(String.format("reward: %,.8f", chaiReward));
        return outPut.toString();
    }

    int getItemCount = 0;

    private boolean processData(JsonArray list) {
        for (Object i : list.items()) {
            JsonObject data = (JsonObject) i;
            String status = data.getString("status_str");
            switch (status) {
                case UNSETTLEMENT:
                    double reward = data.getDouble("huge_reward");
                    if (reward == 0)
                        reward = data.getDouble("block_reward");
                    chaiReward += reward;

                    getItemCount++;
                    updateRecordTime(data);
                    break;
                case SETTLEMENT:
                default:
                    return true;
            }
        }
        return false;
    }

    private void getSettlement() {
        int pageCount = 1;
        int total;
        int numberInPage = 100;
        System.out.print("\rget page 0/*");
        do {
            String result = getDataFromUrl("https://www.hpool.co/api/pool/miningincomerecord?language=zh&type=chia&count=" + numberInPage + "&page=" + (pageCount++), true);
            if (result == null)
                return;

            //process result
            JsonObject jsonObject = new JsonObject(result);
            JsonObject data = jsonObject.get("data");
            total = data.getInteger("total");

            //parse data
            for (Object i : data.getJsonArray("list").items()) {
                JsonObject settleData = (JsonObject) i;
                chaiReward += settleData.getDouble("amount");

                getItemCount++;
                updateRecordTime(settleData);
            }


            System.out.print("\rget page " + (pageCount - 1) + "/" + (total / numberInPage + 1));
        } while (((pageCount - 1) * numberInPage) < total);
        System.out.println();
    }


//    private double chairReward = 0;
//    private JsonObject lastData;
//    private int lastSettlement = 0;
//
//    private void readData(JsonObject data) {
//        String height = data.getString("height");
//        if (height.contains("-")) {
//            if (lastSettlement > 0) {
//                if (lastSettlement == 1) {
//                    chairReward += getChairReward(lastData);
//                    lastSettlement++;
//                }
//                chairReward += getChairReward(data);
//                return;
//            }
//            lastData = data;
//            lastSettlement = 1;
//            return;
//        }
//
//        chairReward += getChairReward(data);
//        lastSettlement = 0;
////
////        System.out.println(height);
////        System.out.println(reward);
////        System.out.println(data.getLong("record_time"));
//    }

    long rewardStartTime = -1;
    long rewardEndTime = -1;
    private double chaiReward = 0;

    private void updateRecordTime(JsonObject data) {
        //time second
        long time = data.getLong("record_time");
        if (rewardStartTime == -1) {
            rewardStartTime = time;
            rewardEndTime = time;
        } else {
            if (time < rewardStartTime)
                rewardStartTime = time;
            if (time > rewardEndTime)
                rewardEndTime = time;
        }
    }

    private String calculateTimeLeft(long time) {
        StringBuilder builder = new StringBuilder();
        if (time > 59) {
            long min = time / 60;
            if (min > 59) {
                long hr = min / 60;
                if (hr > 23) {
                    int day = (int) (hr / 24);
                    builder.append(day).append(" day ");
                }
                hr %= 24;
                builder.append(hr).append(" hour ");
            }
            min %= 60;
            builder.append(min).append(" minute ");
        }
        int sec = (int) (time % 60);
        builder.append(sec).append(" second");
        return builder.toString();
    }

    private String getHpoolCoinInfo() {
        String out = getDataFromUrl("https://www.hpool.co/api/home/list?language=zh&type=eco", false);
        if (out == null)
            return "";

        JsonObject chiaInfo = null;
        JsonArray data = new JsonObject(out).getJsonArray("data");
        for (Object i : data.items()) {
            JsonObject coinInfo = (JsonObject) i;
            if (coinInfo.getString("coin").equals("chia")) {
                chiaInfo = coinInfo;
                break;
            }
        }
        if (chiaInfo == null)
            return "";

        String incomePerPB = chiaInfo.getString("income");
        long networkCapacity = chiaInfo.getLong("network_capacity");
        long poolCapacity = Long.parseLong(chiaInfo.getString("pool_capacity"));
        String height = chiaInfo.getString("height");
        String withdrawMin = chiaInfo.getString("withdraw_min");
        String unit = chiaInfo.getString("unit");

        StringBuilder builder = new StringBuilder();
        builder.append("Network capacity: ").append(String.format("%,.3f", (double) networkCapacity / 1024 / 1024 / 1024)).append("EB").append(newL);
        builder.append("Pool capacity: ").append(String.format("%,.3f", (double) poolCapacity / 1024 / 1024 / 1024)).append("EB").append(newL);
        builder.append("Block height: ").append(height).append(newL);
        builder.append("Minimum payment limit: ").append(withdrawMin).append(unit).append(newL);
        builder.append("Daily reward per PB: ").append(incomePerPB).append(unit).append(newL);

        return builder.toString();
//        return chiaInfo.toStringBeauty();
    }

    private String getOnlinePlotInfo() {
        StringBuilder outPut = new StringBuilder();
        outPut.append("#################################################################################").append(newL);
        outPut.append("Pool Public Key         Capacity      Status    File Count    Updated at").append(newL);
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        long totalCapacity = 0;
        int totalFile = 0;

        int pageCount = 1;
        int total;
        int numberInPage = 100;
        System.out.print("\rget page 0/*");
        do {
            String result = getDataFromUrl("https://www.hpool.co/api/pool/GetPlots?language=zh&status=ALL&pool=chia&count=100&page=" + pageCount++, true);
            if (result == null)
                break;

            //process result
            JsonObject jsonObject = new JsonObject(result);
            if (jsonObject.getInteger("code") > 200) {
                System.out.println(newL + jsonObject.getString("message"));
                System.out.println("please check your token");
                return null;
            }
            JsonObject data = jsonObject.get("data");
            total = data.getInteger("total");

            JsonArray info = data.getJsonArray("list");
            for (Object i : info.items()) {
                JsonObject plotInfo = (JsonObject) i;
                long capacity = plotInfo.getLong("capacity");
                String public_key = plotInfo.getString("public_key");
                int filesCount = plotInfo.getInteger("size");
                String status = plotInfo.getString("status");
                long updated_at = plotInfo.getLong("updated_at") * 1000;
                String uuid = plotInfo.getString("uuid");

                totalCapacity += capacity;
                totalFile += filesCount;

                String capacityTB = String.format("%.2fTB", (double) capacity / 1024 / 1024 / 1024 / 1024);
                outPut.append(public_key).append("\t")
                        .append(capacityTB).append(getTabCount(14, capacityTB.length()))
                        .append(status).append(getTabCount(10, status.length()))
                        .append(filesCount).append(getTabCount(18, status.length()))
                        .append(format.format(updated_at)).append(newL);
            }

            System.out.print("\rget page " + (pageCount - 1) + "/" + (total / numberInPage + 1));
        } while (((pageCount - 1) * numberInPage) < total);
        System.out.println();

        outPut.append("#################################################################################").append(newL);
        outPut.append(String.format("Total Capacity: %.2fTB", (double) totalCapacity / 1024 / 1024 / 1024 / 1024)).append(newL);
        outPut.append("Total File Count:").append(totalFile).append(newL);

        return outPut.toString();
    }

    private String getTabCount(int spaceCount, int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < spaceCount - length; i++) {
            builder.append(" ");
        }

        return builder.toString();
    }

    private String getDataFromUrl(String urlString, boolean isCookie) {
        try {
            //connection api
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            if (isCookie)
                connection.setRequestProperty("Cookie", cookie);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            //read result
            StringBuilder result = new StringBuilder();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String readLine;
            while ((readLine = in.readLine()) != null) {
                result.append(readLine);
            }
            //close connection
            in.close();
            connection.disconnect();
            return result.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private int getDate(long time) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
        calendar.setTimeInMillis(time * 1000);

        return calendar.get(Calendar.DATE);
    }

    private String cookie;

    private void buildCookie() {
        //build cookie
        Map<String, String> cookies = new HashMap<>();
        cookies.put("auth_token", token);
        StringBuilder cookieBuilder = new StringBuilder();
        for (Map.Entry<String, String> i : cookies.entrySet()) {
            cookieBuilder.append(i.getKey()).append("=").append(i.getValue()).append(";");
        }
        cookie = cookieBuilder.toString();
    }


    public static void main(String[] args) throws IOException {
        new HPool(args);
    }
}
