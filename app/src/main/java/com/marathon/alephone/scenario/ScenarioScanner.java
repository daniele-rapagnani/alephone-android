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

        File mmlDir = new File(root, "MML");
        File scriptsDir = new File(root, "Scripts");
        String scenarioName = null;

        if (mmlDir.exists()) {
            scenarioName = findScenarioName(mmlDir);
        }

        if (scenarioName == null && scriptsDir.exists()) {
            scenarioName = findScenarioName(scriptsDir);
        }

        if (scenarioName == null) {
            return null;
        }

        ScenarioEntry de = new ScenarioEntry();
        de.rootPath = root.getAbsolutePath();
        de.path = this.path.getAbsolutePath();
        de.scenarioName = scenarioName;

        return de;
    }

    private File findRoot(File start) {
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

    private String findScenarioName(File dir) {
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

            XPath xp = XPathFactory.newInstance().newXPath();
            try {
                Node n = (Node)xp.compile("/marathon/scenario").evaluate(d, XPathConstants.NODE);

                if (n != null) {
                    Node name = n.getAttributes().getNamedItem("name");

                    if (name != null) {
                        return name.getNodeValue();
                    }
                }
            } catch (XPathExpressionException e) {
                e.printStackTrace();
                continue;
            }
        }

        return null;
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
