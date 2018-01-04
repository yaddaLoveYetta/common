package com.yadda.encode;

import org.junit.Test;

import java.util.Iterator;
import java.util.Properties;

/**
 * @author yadda<silenceisok@163.com>
 * @since 2017/12/19
 */
public class Encode {

    @Test
    public void getSystemEncoding() {
        //System.out.println(System.getProperty("file.encoding"));
        Properties properties = System.getProperties();

        Iterator it = properties.keySet().iterator();

        while (it.hasNext()) {
            String key = (String) it.next();
            String value = properties.getProperty(key);

            System.out.println(key + "=" + value);
        }

    }
}
