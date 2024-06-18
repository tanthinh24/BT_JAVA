/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.multithreadedstudentprocessor;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

public class MultiThreadedStudentProcessor {
    public static void main(String[] args) {
        List<Student> students = new ArrayList<>();
        List<Result> results = new ArrayList<>();

        // Thread 1: Read student.xml
        Thread thread1 = new Thread(() -> {
            try {
                File inputFile = new File("student.xml");
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(inputFile);
                doc.getDocumentElement().normalize();

                NodeList nList = doc.getElementsByTagName("Student");

                for (int temp = 0; temp < nList.getLength(); temp++) {
                    Node nNode = nList.item(temp);

                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        String id = eElement.getElementsByTagName("id").item(0).getTextContent();
                        String name = eElement.getElementsByTagName("name").item(0).getTextContent();
                        String address = eElement.getElementsByTagName("address").item(0).getTextContent();
                        String dateOfBirth = eElement.getElementsByTagName("dateOfBirth").item(0).getTextContent();

                        Student student = new Student(id, name, address, dateOfBirth);
                        synchronized (students) {
                            students.add(student);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Thread 2: Calculate age
        Thread thread2 = new Thread(() -> {
            synchronized (students) {
                for (Student student : students) {
                    String dateOfBirth = student.getDateOfBirth();
                    LocalDate birthDate = LocalDate.parse(dateOfBirth);
                    LocalDate currentDate = LocalDate.now();
                    Period period = Period.between(birthDate, currentDate);
                    String encodedAge = period.getYears() + "-" + period.getMonths() + "-" + period.getDays();

                    student.setAge(encodedAge);
                }
            }
        });

        // Thread 3: Check if sum of digits in dateOfBirth is prime
        Thread thread3 = new Thread(() -> {
            synchronized (students) {
                for (Student student : students) {
                    String dateOfBirth = student.getDateOfBirth();
                    int sum = dateOfBirth.chars().filter(Character::isDigit).map(Character::getNumericValue).sum();
                    boolean isPrime = isPrime(sum);

                    Result result = new Result(student.getId(), student.getName(), student.getAge(), sum, isPrime);
                    synchronized (results) {
                        results.add(result);
                    }
                }
            }
        });

        thread1.start();
        try {
            thread1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        thread2.start();
        try {
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        thread3.start();
        try {
            thread3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Write results to kq.xml
        writeResultsToXml(results);
    }

    private static boolean isPrime(int num) {
        if (num <= 1) {
            return false;
        }
        for (int i = 2; i <= Math.sqrt(num); i++) {
            if (num % i == 0) {
                return false;
            }
        }
        return true;
    }

    private static void writeResultsToXml(List<Result> results) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();
            
            Element rootElement = doc.createElement("Students");
            doc.appendChild(rootElement);

            for (Result result : results) {
                Element studentElement = doc.createElement("Student");
                rootElement.appendChild(studentElement);

                Element id = doc.createElement("id");
                id.appendChild(doc.createTextNode(result.getId()));
                studentElement.appendChild(id);

                Element name = doc.createElement("name");
                name.appendChild(doc.createTextNode(result.getName()));
                studentElement.appendChild(name);

                Element age = doc.createElement("age");
                age.appendChild(doc.createTextNode(result.getAge()));
                studentElement.appendChild(age);

                Element sum = doc.createElement("sum");
                sum.appendChild(doc.createTextNode(String.valueOf(result.getSum())));
                studentElement.appendChild(sum);

                Element isDigit = doc.createElement("isDigit");
                isDigit.appendChild(doc.createTextNode(String.valueOf(result.isPrime())));
                studentElement.appendChild(isDigit);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("kq.xml"));
            transformer.transform(source, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class Student {
    private String id;
    private String name;
    private String address;
    private String dateOfBirth;
    private String age;

    public Student(String id, String name, String address, String dateOfBirth) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }
}

class Result {
    private String id;
    private String name;
    private String age;
    private int sum;
    private boolean isPrime;

    public Result(String id, String name, String age, int sum, boolean isPrime) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.sum = sum;
        this.isPrime = isPrime;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAge() {
        return age;
    }

    public int getSum() {
        return sum;
    }

    public boolean isPrime() {
        return isPrime;
    }
}
