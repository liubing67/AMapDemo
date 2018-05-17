package com.abing.amap.utils;

import com.abing.amap.utils.updatemanager.Update;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.Iterator;

public class UpdateXmlResolve {
    //    public static void main(String[] args) {
//        UpdateXmlResolve td=new UpdateXmlResolve();
//        File file=new File("test.xml");
//        List<Book> list=td.findAll(file);
//        for (int i = 0; i <list.size(); i++) {
//            Book book=list.get(i);
//            System.out.println(book.toString());
//            }
//    }
    public Update findAll(String filePath) {
        File file = new File(filePath);
        SAXReader reader = new SAXReader();
        Document doc = null;
        try {
            doc = reader.read(file);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        Element root = doc.getRootElement();//取出根节点
        //System.out.println(root);
        //迭代出所有子节点
        Iterator its = root.elementIterator();
        Update update = null;
        while (its.hasNext()) {
            Element et = (Element) its.next();//取出所有update节点
            if ("android".equals(et.getName())) {
                update = new Update();
                //迭代Book地下元素
                for (Iterator it = et.elementIterator(); it.hasNext(); ) {
                    Element el = (Element) it.next();
                    switch (el.getName()) {
                        case "versionCode":
                            update.setVersionCode(el.getText());
                            break;
                        case "versionName":
                            update.setVersionName(el.getText());
                            break;
                        case "updateLog":
                            update.setUpdateLog(el.getText());
                            break;
                        case "forceUpdate":
                            update.setForceUpdate(el.getText());
                            break;
                        default:
                            break;
                    }

                }
            }
        }
        return update;
    }
}