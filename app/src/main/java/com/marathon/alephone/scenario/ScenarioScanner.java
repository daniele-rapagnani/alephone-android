package com.marathon.alephone.scenario;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class ScenarioScanner {
    private final File path;

    public ScenarioScanner(File path) {
        this.path = path;
    }

    public ScenarioEntry scan() {
        File root = findRoot(null);

        if (root == null) {
            return null;
        }

        String scenarioName = findScenarioName(root);

        if (scenarioName == null) {
            return null;
        }

        ScenarioEntry de = new ScenarioEntry();
        de.rootPath = root.getAbsolutePath();
        de.path = this.path.getAbsolutePath();
        de.scenarioName = scenarioName;

        return de;
    }

    public File findRoot() {
        return findRoot(null);
    }

    public File findRoot(File start) {
        if (start == null) {
            start = this.path;
        }

        if (!start.isDirectory()) {
            return null;
        }

        File mmlDir = new File(start, "MML");
        File scriptsDir = new File(start, "Scripts");

        if (mmlDir.exists() || scriptsDir.exists()) {
            return start;
        }

        File[] files = start.listFiles();

        for (File f : files) {
            if (f.isDirectory()) {
                File root = findRoot(f);

                if (root != null) {
                    return root;
                }
            }
        }

        return null;
    }

    public Node findXPath(String xpath) {
        return findXPath(null, xpath);
    }

    public Node findXPath(File root, String xpath) {
        if (root == null) {
            root = this.path;
        }

        XPath xp = XPathFactory.newInstance().newXPath();
        XPathExpression xpe = null;

        try {
            xpe = xp.compile(xpath);
        } catch (XPathExpressionException e) {
            return null;
        }

        File mmlDir = new File(root, "MML");
        File scriptsDir = new File(root, "Scripts");
        Node value = null;

        if (mmlDir.exists()) {
            value = findXPathAt(mmlDir, xpe);
        }

        if (value == null && scriptsDir.exists()) {
            value = findXPathAt(scriptsDir, xpe);
        }

        return value;
    }

    private Node findXPathAt(File dir, XPathExpression xpath) {
        File[] files = dir.listFiles();

        for (File f : files) {
            if (f.getName().toLowerCase().endsWith(".lua")) {
                continue;
            }

            if (f.isDirectory()) {
                continue;
            }

            Document d = parseMML(f);

            if (d == null) {
                continue;
            }

            try {
                Node n = (Node)xpath.evaluate(d, XPathConstants.NODE);

                if (n != null) {
                    return n;
                }
            } catch (XPathExpressionException e) {
                e.printStackTrace();
                continue;
            }
        }

        return null;
    }

    public String findScenarioName(File dir) {
        Node scenarioName = findXPath(dir, "/marathon/scenario");

        if (scenarioName == null) {
            return null;
        }

        Node scenarioNameAtt = scenarioName.getAttributes().getNamedItem("name");

        if (scenarioNameAtt == null) {
            return null;
        }

        return scenarioNameAtt.getNodeValue();
    }

    public Document parseMML(File mmlFile) {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();
            return docBuilder.parse(mmlFile);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
