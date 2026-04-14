package com.fr.data;

import com.fr.general.data.TableDataException;
import java.io.*;
import java.util.*;
import java.net.*;
import java.util.regex.*;

/**
 * AGV 小车面板数据
 */
public class AgvDataSource extends AbstractTableData {

    private static final String[] COLUMN_NAMES = {
        "车牌号", "指标名称", "指标值", "采集时间"
    };
    private Object[][] DATA;
    private static final String CONFIG_FILE = "agv-config.properties";
    private static final Pattern AGV_ID_PATTERN = Pattern.compile("AGV标识:\\s*【([^】]+)】");
    private static final Pattern STATUS_PATTERN = Pattern.compile("当前状态:\\s*【([^】]+)】");
    private static final Pattern BATTERY_PATTERN = Pattern.compile("剩余电量:\\s*【([^】]+)】");
    private static final Pattern TASK_PATTERN = Pattern.compile("正在执行.*?【(.*?)】", Pattern.DOTALL);

    public AgvDataSource() {
        super();
        loadConfig();
    }

    private void loadConfig() {
        List<Object[]> rows = new ArrayList<>();
        Properties prop = new Properties();
        
        String[] paths = {
            "D:/FineReport_11.0/webapps/webroot/WEB-INF/classes/" + CONFIG_FILE,
            "C:/FineReport_11.0/webapps/webroot/WEB-INF/classes/" + CONFIG_FILE,
            CONFIG_FILE
        };
        
        for (String path : paths) {
            try (InputStream is = new FileInputStream(path)) {
                prop.load(is);
                break;
            } catch (Exception e) {
                // 
            }
        }
        
        if (prop.isEmpty()) {
            addDefaultData(rows);
        } else {
            for (String key : prop.stringPropertyNames()) {
                if (key.endsWith(".url")) {
                    String carName = key.substring(0, key.length() - 4);
                    String url = prop.getProperty(key);
                    fetchCarData(rows, carName, url);
                }
            }
        }
        
        DATA = rows.toArray(new Object[0][0]);
    }

    private void addDefaultData(List<Object[]> rows) {
        String now = getCurrentTime();
        rows.add(new Object[]{"XQE-122-1", "当前状态", "manual", now});
        rows.add(new Object[]{"XQE-122-1", "剩余电量", "64", now});
        rows.add(new Object[]{"XQE-122-1", "正在执行任务", "货架上架 / I|590", now});
        rows.add(new Object[]{"XQE-122-1", "任务状态", "执行中", now});
    }

    private void fetchCarData(List<Object[]> rows, String carName, String url) {
        String now = getCurrentTime();
        
        try {
            URL httpUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                addCarErrorData(rows, carName, "HTTP错误:" + responseCode, now);
                return;
            }
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder html = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                html.append(line);
            }
            reader.close();
            
            String htmlContent = html.toString();
            String textContent = decodeHtmlEntities(htmlContent);
            
            String agvId = extractFirst(AGV_ID_PATTERN, textContent, carName);
            String status = extractFirst(STATUS_PATTERN, textContent, "unknown");
            String battery = extractFirst(BATTERY_PATTERN, textContent, "0");
            String taskInfo = extractTaskInfo(textContent);
            
            // 提取jobstatus（可能为空，由JS动态加载）
            String jobStatus = "";
            int jobIdx = htmlContent.indexOf("id=\"jobstatus\"");
            if (jobIdx >= 0) {
                int gtIdx = htmlContent.indexOf(">", jobIdx);
                int closeSpanIdx = htmlContent.indexOf("</span>", gtIdx);
                if (gtIdx >= 0 && closeSpanIdx >= 0) {
                    jobStatus = htmlContent.substring(gtIdx + 1, closeSpanIdx).trim();
                }
            }
            
            // 检查是否有执行失败信息
            String execStatus = "";
            if (htmlContent.contains("（执行失败）") || htmlContent.contains("(执行失败)")) {
                execStatus = "执行失败";
            } else if (jobStatus != null && jobStatus.length() > 0) {
                execStatus = jobStatus;
            } else if (taskInfo != null && !taskInfo.isEmpty()) {
                execStatus = "执行中";
            }
            
            rows.add(new Object[]{agvId, "当前状态", status, now});
            rows.add(new Object[]{agvId, "剩余电量", battery, now});
            
            if (taskInfo != null && !taskInfo.isEmpty()) {
                rows.add(new Object[]{agvId, "正在执行任务", taskInfo, now});
            }
            
            // 任务状态：根据HTML中的实际内容显示
            if (execStatus.length() > 0) {
                rows.add(new Object[]{agvId, "任务状态", execStatus, now});
            }
            
        } catch (Exception e) {
            addCarErrorData(rows, carName, e.getMessage(), now);
        }
    }

    private String decodeHtmlEntities(String text) {
        Pattern hexPattern = Pattern.compile("&#x([0-9a-fA-F]+);");
        Matcher hexMatcher = hexPattern.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (hexMatcher.find()) {
            try {
                int codePoint = Integer.parseInt(hexMatcher.group(1), 16);
                hexMatcher.appendReplacement(sb, new String(Character.toChars(codePoint)));
            } catch (Exception e) {
                hexMatcher.appendReplacement(sb, hexMatcher.group(0));
            }
        }
        hexMatcher.appendTail(sb);
        
        Pattern decPattern = Pattern.compile("&#(\\d+);");
        Matcher decMatcher = decPattern.matcher(sb.toString());
        StringBuffer sb2 = new StringBuffer();
        while (decMatcher.find()) {
            try {
                int codePoint = Integer.parseInt(decMatcher.group(1), 10);
                decMatcher.appendReplacement(sb2, new String(Character.toChars(codePoint)));
            } catch (Exception e) {
                decMatcher.appendReplacement(sb2, decMatcher.group(0));
            }
        }
        decMatcher.appendTail(sb2);
        
        return sb2.toString();
    }

    private String extractFirst(Pattern pattern, String text, String defaultValue) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return defaultValue;
    }

    private String extractTaskInfo(String text) {
        Matcher m = TASK_PATTERN.matcher(text);
        if (m.find()) {
            String taskContent = m.group(1).trim();
            int slashIndex = taskContent.lastIndexOf('/');
            if (slashIndex > 0) {
                String taskType = taskContent.substring(0, slashIndex).trim();
                String taskId = taskContent.substring(slashIndex + 1).trim();
                return taskType + " / " + taskId;
            }
            return taskContent;
        }
        return null;
    }

    private void addCarErrorData(List<Object[]> rows, String carName, String error, String now) {
        rows.add(new Object[]{carName, "当前状态", "error", now});
        rows.add(new Object[]{carName, "错误信息", error, now});
    }

    private String getCurrentTime() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    @Override
    public int getColumnCount() throws TableDataException {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int columnIndex) throws TableDataException {
        return COLUMN_NAMES[columnIndex];
    }

    @Override
    public int getRowCount() throws TableDataException {
        return DATA.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return DATA[rowIndex][columnIndex];
    }
}
