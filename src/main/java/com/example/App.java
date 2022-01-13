package com.example;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.alibaba.fastjson.JSONArray;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;

import lombok.Cleanup;
import lombok.val;
import lombok.var;

/**
 * Hello world!
 */
public final class App {

    /**
     * 爬取省市县信息.
     *
     * @param args The arguments of the program.
     * @throws IOException
     * @throws SQLException
     */
    public static void main(String[] args) throws IOException, SQLException {
        val regionsInfoList = new ArrayList<ChinaRegionsInfo>();
        getInfo(regionsInfoList);

        // 打印结果
        // System.out.println(JSONArray.toJSONString(regionsInfoList));

        // create table
        createTable();

        // saveAll
        saveAll(regionsInfoList);

        // // SQL statement for creating a new table
        // val sql = "CREATE TABLE IF NOT EXISTS employees (\n" + " id integer PRIMARY
        // KEY,\n"
        // + " name text NOT NULL,\n" + " capacity real\n" + ");";

        // ConnectSQLite.execute(sql);

        // // SQL statement for insert a new record
        // val sql2 = "INSERT INTO employees(name, capacity) VALUES(?,?)";
        // ConnectSQLite.executeUpdate(sql2);

        // val sql3 = "SELECT * FROM employees";
        // ConnectSQLite.executeQuery(sql3);
    }

    public static void getInfo(List<ChinaRegionsInfo> regionsInfoList) throws IOException {

        // 需要抓取的网页地址
        val URL = "http://www.mca.gov.cn/article/sj/xzqh/2020/20201201.html";

        // List<ChinaRegionsInfo> regionsInfoList = new ArrayList<>();

        // 抓取网页信息
        val document = Jsoup.connect(URL).get();
        // 获取真实的数据体
        val element = document.getElementsByTag("tbody").get(0);

        var provinceCode = StringUtils.EMPTY;// 省级编码
        var cityCode = StringUtils.EMPTY;// 市级编码

        if (Objects.nonNull(element)) {
            val trs = element.getElementsByTag("tr");
            for (int i = 3; i < trs.size(); i++) {
                val tds = trs.get(i).getElementsByTag("td");
                if (tds.size() < 3) {
                    continue;
                }
                val td1 = tds.get(1);// 行政区域编码
                val td2 = tds.get(2);// 行政区域名称

                if (StringUtils.isNotEmpty(td1.text())) {
                    if (td1.classNames().contains("xl7228320")) {
                        if (td2.toString().contains("span")) {

                            val span = td2.getElementsByTag("span").get(0);
                            val spanWholeText = span.wholeText();
                            if (spanWholeText.length() == 1) {

                                // 市级
                                val chinaRegions = new ChinaRegionsInfo();
                                chinaRegions.setCode(td1.text());
                                chinaRegions.setName(td2.text());
                                chinaRegions.setType(2);
                                chinaRegions.setParentCode(provinceCode);
                                regionsInfoList.add(chinaRegions);
                                cityCode = td1.text();
                            }

                        } else {

                            // 省级
                            val chinaRegions = new ChinaRegionsInfo();
                            chinaRegions.setCode(td1.text());
                            chinaRegions.setName(td2.text());
                            chinaRegions.setType(1);
                            chinaRegions.setParentCode("");
                            regionsInfoList.add(chinaRegions);
                            provinceCode = td1.text();
                        }
                        continue;
                    }
                    if (td1.classNames().contains("xl7328320")) {
                        val span = td2.getElementsByTag("span").get(0);
                        val spanWholeText = span.wholeText();
                        if (spanWholeText.length() == 3) {

                            // 区或者县级
                            val chinaRegions = new ChinaRegionsInfo();
                            chinaRegions.setCode(td1.text());
                            chinaRegions.setName(td2.text());
                            chinaRegions.setType(3);
                            chinaRegions.setParentCode(StringUtils.isNotEmpty(cityCode) ? cityCode : provinceCode);
                            regionsInfoList.add(chinaRegions);
                        }
                    }
                }
            }
        }
        // // 打印结果
        // System.out.println(JSONArray.toJSONString(regionsInfoList));
    }

    static void createTable() throws SQLException {
        @Cleanup
        val conn = ConnectSQLite.connect();
        @Cleanup
        val stat = conn.createStatement();
        stat.executeUpdate("drop table if exists chinaRegion;");
        stat.executeUpdate(
                "create table chinaRegion (code text PRIMARY KEY, name text, type integer, parentCode text);");
    }

    private static void saveAll(List<ChinaRegionsInfo> chinaRegionsInfos)
            throws SQLException {
        @Cleanup
        val conn = ConnectSQLite.connect();
        @Cleanup
        val prep = conn.prepareStatement("insert into chinaRegion values (?, ?, ?, ?);");
        for (ChinaRegionsInfo chinaRegionsInfo : chinaRegionsInfos) {
            prep.setString(1, chinaRegionsInfo.getCode());
            prep.setString(2, chinaRegionsInfo.getName());
            prep.setInt(3, chinaRegionsInfo.getType());
            prep.setString(4, chinaRegionsInfo.getParentCode());
            prep.addBatch();
        }
        conn.setAutoCommit(false);
        prep.executeBatch();
        conn.setAutoCommit(true);
    }
}
