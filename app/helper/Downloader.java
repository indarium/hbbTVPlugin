package helper;

import play.Logger;

import javax.net.ssl.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * Created by dermicha on 05/09/14.
 */
public class Downloader {
    private static int BUFFER_SIZE = 16 * 1024;

    public static void downloadFile(URL source, FileOutputStream os) {
        try {
               /*
     *  fix for
     *    Exception in thread "main" javax.net.ssl.SSLHandshakeException:
     *       sun.security.validator.ValidatorException:
     *           PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException:
     *               unable to find valid certification path to requested target
     */
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }

                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    /*
     * end of the fix
     */

            InputStream is = source.openStream();
            try {
                copy(is, os);
            } finally {
                if (is != null) is.close();
                if (os != null) os.close();
            }
        } catch (NoSuchAlgorithmException | KeyManagementException | IOException e) {
            Logger.error("error while downloading file", e);
        }
    }

    private static void copy(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int count = is.read(buffer, 0, BUFFER_SIZE);
        int counter = 0;
        //&& counter < 500
        while (count != -1) {
            os.write(buffer, 0, count);
            count = is.read(buffer, 0, BUFFER_SIZE);
            counter++;
            //if (counter % 200 == 0)
            //    System.out.print("*");
        }
        os.flush();
    }
}
