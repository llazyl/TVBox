package com.github.tvbox.osc.js;

import android.text.TextUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlParser {
    private static Document pdfa_doc = null;
    private static String pdfa_html = "";
    private static Document pdfh_doc = null;
    private static String pdfh_html = "";
    private static String SPACE = " ";
    private static String TAG_STYLE = "style";

    public static class Painfo {
        public List<String> excludes;
        public int nparse_index;
        public String nparse_rule;
    }

    public static String joinUrl(String parent, String child) {
        if (TextUtils.isEmpty(parent)) {
            return child;
        }
        try {
            return new URL(new URL(parent), child).toExternalForm();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return parent;
        }
    }

    private static Painfo getParseInfo(String nparse) {
        Painfo painfo = new Painfo();
        painfo.nparse_rule = nparse;
        if (nparse.contains(":eq")) {
            painfo.nparse_rule = nparse.split(":")[0];
            String str = nparse.split(":")[1];
            if (painfo.nparse_rule.contains("--")) {
                String[] split = painfo.nparse_rule.split("--");
                painfo.excludes = new ArrayList(Arrays.asList(split));
                painfo.excludes.remove(0);
                painfo.nparse_rule = split[0];
            } else if (str.contains("--")) {
                String[] split2 = str.split("--");
                painfo.excludes = new ArrayList(Arrays.asList(split2));
                painfo.excludes.remove(0);
                str = split2[0];
            }
            try {
                painfo.nparse_index = Integer.parseInt(str.replace("eq(", "").replace(")", ""));
            } catch (Exception unused) {
                painfo.nparse_index = 0;
            }
        } else if (nparse.contains("--")) {
            String[] split3 = painfo.nparse_rule.split("--");
            painfo.excludes = new ArrayList(Arrays.asList(split3));
            painfo.excludes.remove(0);
            painfo.nparse_rule = split3[0];
        }
        return painfo;
    }

    private static String parseHikerToJq(String parse, boolean first) {
        if (parse.contains("&&")) {
            String[] split = parse.split("&&");
            ArrayList arrayList = new ArrayList();
            for (int i = 0; i < split.length; i++) {
                String[] split2 = split[i].split(SPACE);
                if (Pattern.compile(":eq|:lt|:gt|:first|:last|^body$|^#").matcher(split2[split2.length - 1]).find()) {
                    arrayList.add(split[i]);
                } else if (!first && i >= split.length - 1) {
                    arrayList.add(split[i]);
                } else {
                    arrayList.add(split[i] + ":eq(0)");
                }
            }
            return TextUtils.join(SPACE, arrayList);
        }
        String[] split3 = parse.split(SPACE);
        if (Pattern.compile(":eq|:lt|:gt|:first|:last|^body$|^#").matcher(split3[split3.length - 1]).find() || !first) {
            return parse;
        }
        return parse + ":eq(0)";
    }

    public static synchronized String parseDomForUrl(String html, String rule, String add_url) {
        String outerHtml;
        synchronized (HtmlParser.class) {
            if (!pdfh_html.equals(html)) {
                pdfh_html = html;
                pdfh_doc = Jsoup.parse(html);
            }
            Document document = pdfh_doc;
            if (!rule.equals("body&&Text") && !rule.equals("Text")) {
                if (!rule.equals("body&&Html") && !rule.equals("Html")) {
                    String str = "";
                    if (rule.contains("&&")) {
                        String[] split = rule.split("&&");
                        str = split[split.length - 1];
                        ArrayList arrayList = new ArrayList(Arrays.asList(split));
                        arrayList.remove(split.length - 1);
                        rule = TextUtils.join("&&", arrayList);
                    }
                    String[] split2 = parseHikerToJq(rule, true).split(SPACE);
                    Elements elements = new Elements();
                    for (String str2 : split2) {
                        elements = parseOneRule(document, str2, elements);
                    }
                    if (!TextUtils.isEmpty(str)) {
                        if (str.equals("Text")) {
                            outerHtml = elements.text();
                        } else if (str.equals("Html")) {
                            outerHtml = elements.html();
                        } else {
                            outerHtml = elements.attr(str);
                            if (str.toLowerCase().contains(TAG_STYLE) && outerHtml.contains("url(")) {
                                Matcher matcher = Pattern.compile("url\\((.*?)\\)", 40).matcher(outerHtml);
                                if (matcher.find()) {
                                    outerHtml = matcher.group();
                                }
                            }
                            if (!TextUtils.isEmpty(outerHtml) && !TextUtils.isEmpty(add_url) && Pattern.compile("(url|src|href|-original|-src|-play|-url)$", 10).matcher(str).find()) {
                                if (outerHtml.contains("http")) {
                                    outerHtml = outerHtml.substring(outerHtml.indexOf("http"));
                                } else {
                                    outerHtml = joinUrl(add_url, outerHtml);
                                }
                            }
                        }
                    } else {
                        outerHtml = elements.outerHtml();
                    }
                    return outerHtml;
                }
                return document.html();
            }
            return document.text();
        }
    }

    public static synchronized List<String> parseDomForList(String html, String rule) {
        synchronized (HtmlParser.class) {
            if (!pdfa_html.equals(html)) {
                pdfa_html = html;
                pdfa_doc = Jsoup.parse(html);
            }
            Document document = pdfa_doc;
            String[] split = parseHikerToJq(rule, false).split(SPACE);
            Elements elements = new Elements();
            for (String str : split) {
                elements = parseOneRule(document, str, elements);
                if (elements.isEmpty()) {
                    return new ArrayList();
                }
            }
            ArrayList arrayList = new ArrayList();
            Iterator<Element> it = elements.iterator();
            while (it.hasNext()) {
                arrayList.add(it.next().outerHtml());
            }
            return arrayList;
        }
    }

    private static Elements parseOneRule(Document doc, String nparse, Elements ret) {
        Elements select;
        Painfo parseInfo = getParseInfo(nparse);
        if (nparse.contains(":eq")) {
            if (ret.isEmpty()) {
                select = doc.select(parseInfo.nparse_rule).eq(parseInfo.nparse_index);
            } else {
                select = ret.select(parseInfo.nparse_rule).eq(parseInfo.nparse_index);
            }
        } else if (ret.isEmpty()) {
            select = doc.select(nparse);
        } else {
            select = ret.select(nparse);
        }
        if (parseInfo.excludes != null && !select.isEmpty()) {
            select = select.clone();
            for (String str : parseInfo.excludes) {
                select.select(str).remove();
            }
        }
        return select;
    }
}
