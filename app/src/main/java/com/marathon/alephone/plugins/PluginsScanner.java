package com.marathon.alephone.plugins;

import android.app.Activity;
import android.net.Uri;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class PluginsScanner {
    private final Activity activity;

    public PluginsScanner(Activity activity) {
        this.activity = activity;
    }

    public List<Plugin> scan(Uri source) {
        List<Plugin> plugins = new ArrayList<>();

        try {
            InputStream is = this.activity.getContentResolver().openInputStream(source);
            ZipInputStream zis = new ZipInputStream(is);
            ZipEntry ze = null;

            while ((ze = zis.getNextEntry()) != null) {
                String name = FilenameUtils.getName(ze.getName());

                if (name != null && name.equals("Plugin.xml")) {
                    Plugin p = readPlugin(ze.getName(), zis);

                    if (p != null) {
                        plugins.add(p);
                    }
                }

                zis.closeEntry();
            }

            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        return plugins;
    }

    protected Plugin readPlugin(String path, InputStream is) throws IOException {
        byte[] b = new byte[1024];
        int size = 0;
        int count = 0;

        String pluginDesc = "";

        while((count = is.read(b)) != -1) {
            size += count;
            pluginDesc += new String(b, 0, count, "UTF8");
        }

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

        Plugin plugin = null;

        try {
            DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new InputSource(new StringReader(pluginDesc)));
            XPath xp = XPathFactory.newInstance().newXPath();
            Node node = (Node)xp.compile("/plugin").evaluate(doc, XPathConstants.NODE);

            if (node == null) {
                return null;
            }

            NamedNodeMap attrs = node.getAttributes();

            if (
                attrs.getNamedItem("name") == null
                || attrs.getNamedItem("description") == null
                || attrs.getNamedItem("version") == null
            ) {
                return null;
            }

            plugin = new Plugin(
                FilenameUtils.getPath(path),
                attrs.getNamedItem("name").getNodeValue(),
                attrs.getNamedItem("description").getNodeValue(),
                attrs.getNamedItem("version").getNodeValue()
            );

            if (attrs.getNamedItem("minimum_version") != null) {
                plugin.minVersion = attrs.getNamedItem("minimum_version").getNodeValue();
            }

            if (attrs.getNamedItem("hud_lua") != null) {
                plugin.hudLua = attrs.getNamedItem("hud_lua").getNodeValue();
            }

            if (attrs.getNamedItem("solo_lua") != null) {
                plugin.soloLua = attrs.getNamedItem("solo_lua").getNodeValue();
            }

            if (attrs.getNamedItem("theme_dir") != null) {
                plugin.themeDir = attrs.getNamedItem("theme_dir").getNodeValue();
            }
        } catch (IOException | SAXException | ParserConfigurationException | XPathExpressionException e) {
            return null;
        }

        return plugin;
    }
}