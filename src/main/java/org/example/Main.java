package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String fileName = "data.csv";
        String fileNameXml = "data.xml";
        String fileNameCsvToJson = "data.json";
        String fileNameXmlToJson = "data2.json";

        // csv
        List<Employee> list = parseCSV(columnMapping, fileName);
        writeString(listToJson(list), fileNameCsvToJson);

        // xml
        List<Employee> listXml = parseXML(fileNameXml);
        writeString(listToJson(listXml), fileNameXmlToJson);

        // json
        String json = readString(fileNameCsvToJson);
        List<Employee> listJson = jsonToList(json);
        System.out.println(listJson);
    }

    private static List<Employee> jsonToList(String json) {
        List<Employee> list = new ArrayList<>();
        JSONArray arr;
        try {
            arr = (JSONArray) new JSONParser().parse(json);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        Gson gson = new GsonBuilder().create();
        for (Object obj : arr) {
            Employee employee = gson.fromJson(obj.toString(), Employee.class);
            list.add(employee);
        }
        return list;
    }

    private static String readString(String fileName) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String s;
            while ((s = br.readLine()) != null) {
                sb.append(s);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

    private static List<Employee> parseXML(String fileName) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document doc;
        try {
            builder = factory.newDocumentBuilder();
            doc = builder.parse(new File(fileName));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }

        List<Employee> list = new ArrayList<>();

        Node root = doc.getDocumentElement();
        NodeList nodeList = root.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i); // <employee>
            if (Node.ELEMENT_NODE == node.getNodeType()) {
                long id = 0; String firstName = ""; String lastName =""; String country = ""; int age = 0;
                NodeList nodeList1 = node.getChildNodes();
                for (int i1 = 0; i1 < nodeList1.getLength(); i1++) {
                    Node node1 = nodeList1.item(i1); // element
                    if (Node.ELEMENT_NODE == node1.getNodeType()) {
                        String value = node1.getTextContent();
                        switch (node1.getNodeName()) {
                            case "id" -> id = Long.parseLong(value);
                            case "firstName" -> firstName = value;
                            case "lastName" -> lastName = value;
                            case "country" -> country = value;
                            case "age" -> age = Integer.parseInt(value);
                        }
                    }
                }
                list.add(new Employee(id, firstName, lastName, country, age));
            }
        }
        return list;
    }

    private static void writeString(String data, String fileName) {
        try (FileWriter writer = new FileWriter(fileName, false)) {
            writer.write(data);
            writer.flush();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static String listToJson(List<Employee> list) {
        Gson gson = new GsonBuilder().create();
        Type listType = new TypeToken<List<Employee>>() {}.getType();
        return gson.toJson(list, listType);
    }

    private static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        try (CSVReader csvReader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> cmap = new ColumnPositionMappingStrategy<>();
            cmap.setType(Employee.class);
            cmap.setColumnMapping(columnMapping);
            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(csvReader)
                    .withMappingStrategy(cmap)
                    .build();
            return csv.parse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}