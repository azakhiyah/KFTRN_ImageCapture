/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kftrn_imagecapture;



import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import kftrn_imagecapture.controller.HSQLDBConnection;
import kftrn_imagecapture.utilities.Config;


/**
 *
 * @author zakhiyah arsal
 */
public class KFTRN_ImageCapture {
    
    static Logger mLog = Logger.getLogger(KFTRN_ImageCapture.class.getName());;
    private static final String USER_NAME = "";
    private static final String PASSWORD = "";
    private static final String protocol = "http";
    private static final String file = "/ISAPI/Streaming/channels/101/picture";
    private static final String server_ip = "";
    private static final int port = 0;//
    private static final String upload = "/upload";
    private static final File targetDir = new File("trnimage");
    private static final String FileName="";
 
    /**
     * @param args the command line arguments
     */
    
    public static void main(String[] args) throws Exception {
          Handler handler = new FileHandler("KFTRN_ImageCapture"+getCurrentDatetime()+".log", true);
          mLog.addHandler(handler);
          SimpleFormatter formatter = new SimpleFormatter();  
          handler.setFormatter(formatter);
          
          ArrayList <String> Trn_Identifer=getTRN_IDENTIFER();
          
          String Site_Reg=getSITE_REG();
          
          if (Trn_Identifer.size()!=0) {
              
          int Id=Integer.parseInt(Trn_Identifer.get(0));
          int TRNPanel_Id = Integer.parseInt(Trn_Identifer.get(1));
          int SlipNo=Integer.parseInt(Trn_Identifer.get(2));
          String CardNo = Trn_Identifer.get(3);
          long unixTimestamp = Instant.now().getEpochSecond();
          String FileName=new String(Site_Reg+"_"+TRNPanel_Id+"_"+SlipNo+"_"+CardNo+"_"+unixTimestamp);
          File targetFile=new File(targetDir, FileName+".jpg");
          
              getImage(targetFile);
          
              postImage(targetFile);
                        
          } else {
              //System.out.println("No Data Found"); 
              mLog.info("No Data Found"); 
         }   
    }
    
    
    public static String getCAMERA_IP() {
        String Camera_IP="";
        Connection connDB = null;
        
        try {
           
            connDB = HSQLDBConnection.getConnection();
            
            StringBuilder sqlgetCameraIP = new StringBuilder();
            sqlgetCameraIP.append("SELECT CAMERA_IP FROM CAMERAMAP ");
            sqlgetCameraIP.append(" WHERE TRNPANEL_ID = (SELECT top 1 TRNPANEL_ID FROM TRANSACTIONIMAGE WHERE CAPTURE_STATUS='P' order by TRN_DATE asc)");
            sqlgetCameraIP.append(" AND NOZZLE_NO = (SELECT top 1 NOZZLE_NO FROM TRANSACTIONIMAGE WHERE CAPTURE_STATUS='P' order by TRN_DATE asc)");
            PreparedStatement psgetCameraIP = connDB.prepareStatement(sqlgetCameraIP.toString());
            ResultSet rsgetCameraIP = psgetCameraIP.executeQuery();
            
            while (rsgetCameraIP.next()) {
                Camera_IP = rsgetCameraIP.getString("CAMERA_IP");
            }
            
            connDB.close();
            
        } catch (Exception e) {
                e.printStackTrace();
        }
        
        return Camera_IP;
    }
    
    public static String getSITE_REG() {
        String SITE_REG="";
        Connection connDB = null;
        
        try {
           
            connDB = HSQLDBConnection.getConnection();
            
            StringBuilder sqlgetSiteReg = new StringBuilder();
            sqlgetSiteReg.append("SELECT SITE_REGISTRATION FROM SITEDETAIL ");
            PreparedStatement psgetSiteReg = connDB.prepareStatement(sqlgetSiteReg.toString());
            ResultSet rsgetSiteReg = psgetSiteReg.executeQuery();
            
            while (rsgetSiteReg.next()) {
                SITE_REG = rsgetSiteReg.getString("SITE_REGISTRATION");
            }
            
            connDB.close();
            
        } catch (SQLException e) {
        }
        
        return SITE_REG;
    }
    
    public static void uploadSTATUSSEND(int Id, int SlipNo){
        Connection connDB = null;
        try {
           
            connDB = HSQLDBConnection.getConnection();
            
            StringBuilder sqlupdateStatus = new StringBuilder();
            sqlupdateStatus.append("UPDATE TRANSACTIONIMAGE SET UPLOAD_STATUS ='S'");
            sqlupdateStatus.append(" WHERE ID = ").append(Id).append("");
            sqlupdateStatus.append(" AND SLIP_NO = ").append(SlipNo).append("");
            PreparedStatement psupdateStatus = connDB.prepareStatement(sqlupdateStatus.toString());
            psupdateStatus.execute();
            
            connDB.close();
            
        } catch (Exception e) {
             e.printStackTrace();
        }
    }
    
    
    public static void updateSTATUSCAPTURE(int Id, int SlipNo){
        Connection connDB = null;
        try {
           
            connDB = HSQLDBConnection.getConnection();
            
            StringBuilder sqlupdateStatus = new StringBuilder();
            sqlupdateStatus.append("UPDATE TRANSACTIONIMAGE SET CAPTURE_STATUS ='C'");
            sqlupdateStatus.append(" WHERE ID = ").append(Id).append("");
            sqlupdateStatus.append(" AND SLIP_NO = ").append(SlipNo).append("");
            PreparedStatement psupdateStatus = connDB.prepareStatement(sqlupdateStatus.toString());
            psupdateStatus.execute();
            
            connDB.close();
            
        } catch (Exception e) {
             e.printStackTrace();
        }
    }

    public static void updateSTATUSFAIL(int Id, int SlipNo){
        Connection connDB = null;
        try {
           
            connDB = HSQLDBConnection.getConnection();
            
            StringBuilder sqlupdateStatus = new StringBuilder();
            sqlupdateStatus.append("UPDATE TRANSACTIONIMAGE SET CAPTURE_STATUS ='F'");
            sqlupdateStatus.append(" WHERE ID = ").append(Id).append("");
            sqlupdateStatus.append(" AND SLIP_NO = ").append(SlipNo).append("");
            PreparedStatement psupdateStatus = connDB.prepareStatement(sqlupdateStatus.toString());
            psupdateStatus.execute();
            
            connDB.close();
            
        } catch (Exception e) {
             e.printStackTrace();
        }
    }
   
    public static void getImage( File targetFile) throws MalformedURLException, IOException, Exception{
        String host = getCAMERA_IP();
        
        ArrayList <String> Trn_Identifer=getTRN_IDENTIFER();
        int Id=Integer.parseInt(Trn_Identifer.get(0));
        int SlipNo=Integer.parseInt(Trn_Identifer.get(2));
        String CardNo = Trn_Identifer.get(3);
        
        Config properties = new Config();
        String USER_NAME=new String(properties.getProperty("USER_NAME"));
        String PASSWORD=new String(properties.getProperty("PASSWORD"));
        
        URL url = new URL(protocol, host, file); 
        final String authString = USER_NAME + ":" + PASSWORD;
        byte[] buffer = new byte[4096];
        int n = 0;
               
            HttpURLConnection HTTPconn = (HttpURLConnection) url.openConnection();
            HTTPconn.setConnectTimeout(3000);
            HTTPconn.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString(authString.getBytes()));
            try { 
            HTTPconn.connect();
              
                InputStream is = HTTPconn.getInputStream(); 
                FileOutputStream fos = new FileOutputStream(targetFile);
                
                    while (-1 != (n = is.read(buffer))) {
                        fos.write(buffer, 0, n);  
                    }
                is.close();
                fos.close();
                mLog.info("Capture Image for Transaction with SlipNo: "+SlipNo+" and CardNo: "+CardNo+" Done :)"); 
                //postImage(targetFile);
                updateSTATUSCAPTURE(Id, SlipNo);
                
             } catch (SocketTimeoutException e) {
                updateSTATUSFAIL(Id, SlipNo); 
                mLog.info("ERROR !! Please Check Camera :" + host);
                
             }
            HTTPconn.disconnect();    
    }    
    
    public static ArrayList getTRN_IDENTIFER() {
        ArrayList <String> Trn_Identifer = new ArrayList <String>();
        Connection connDB = null;
        
        try {
           
            connDB = HSQLDBConnection.getConnection();
            
            StringBuilder sqlgetTrn = new StringBuilder();
            sqlgetTrn.append("SELECT ID, TRNPANEL_ID,SLIP_NO,CARD_NO FROM TRANSACTIONIMAGE ");
            sqlgetTrn.append(" WHERE CAPTURE_STATUS='P'");
            PreparedStatement psgetTrn = connDB.prepareStatement(sqlgetTrn.toString());
            ResultSet rsgetTrn = psgetTrn.executeQuery();
            
            while (rsgetTrn.next()) {
                    Trn_Identifer.add(rsgetTrn.getString("ID"));
                    Trn_Identifer.add(rsgetTrn.getString("TRNPANEL_ID"));
                    Trn_Identifer.add(rsgetTrn.getString("SLIP_NO"));
                    Trn_Identifer.add(rsgetTrn.getString("CARD_NO"));
            }
            
            connDB.close();
            
        } catch (SQLException e) {
        }
        
        return Trn_Identifer;        
    }
    
    public static ArrayList getIMAGE_TOSEND() {
        ArrayList <String> image_tosend = new ArrayList <String>();
        Connection connDB = null;
        
        try {
           
            connDB = HSQLDBConnection.getConnection();
            
            StringBuilder sqlgetImg2send = new StringBuilder();
            sqlgetImg2send.append("SELECT ID, TRNPANEL_ID,SLIP_NO,CARD_NO FROM TRANSACTIONIMAGE ");
            sqlgetImg2send.append(" WHERE CAPTURE_STATUS='C'");
            sqlgetImg2send.append(" AND UPLOAD_STATUS='P' order by TRN_DATE desc");
            PreparedStatement psgetImg2send = connDB.prepareStatement(sqlgetImg2send.toString());
            ResultSet rsgetImg2send = psgetImg2send.executeQuery();
            
            while (rsgetImg2send.next()) {
                    image_tosend.add(rsgetImg2send.getString("ID"));
                    image_tosend.add(rsgetImg2send.getString("TRNPANEL_ID"));
                    image_tosend.add(rsgetImg2send.getString("SLIP_NO"));
                    image_tosend.add(rsgetImg2send.getString("CARD_NO"));
            }
            
            connDB.close();
            
        } catch (SQLException e) {
        }
        
        return image_tosend;        
    }
    

    
    public static void postImage(File targetFile) throws MalformedURLException, IOException {
        Config properties = new Config();
        String server_ip=new String(properties.getProperty("server_ip"));
        int port =Integer.parseInt(properties.getProperty("port"));
        
        URL urlUpload = new URL(protocol, server_ip, port, upload);
    
        ArrayList <String> Image_tosend=getIMAGE_TOSEND();
        int Id=Integer.parseInt(Image_tosend.get(0));
        int SlipNo=Integer.parseInt(Image_tosend.get(2));
        String CardNo = Image_tosend.get(3);
        
        String boundary = Long.toHexString(System.currentTimeMillis());
        String charset = "UTF-8";
        String CRLF = "\r\n"; // Line separator required by multipart/form-data.
        
            HttpURLConnection Uploadconn = (HttpURLConnection) urlUpload.openConnection();
            Uploadconn.setConnectTimeout(3000);
            Uploadconn.setDoOutput(true);
            Uploadconn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
          try  {
                OutputStream output = Uploadconn.getOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
               
           
                writer.append("--" + boundary).append(CRLF);
                writer.append("Content-Disposition: form-data; name=\"NAME\"; filename=\"" + targetFile.getName() + "\"").append(CRLF);
                writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF); // Text file itself must be saved in this charset!
                writer.append(CRLF).flush();
                try {
                    Files.copy(targetFile.toPath(), output);
                    output.flush(); // Important before continuing with writer!
                    writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.

                    writer.append("--" + boundary + "--").append(CRLF).flush();
               } catch (IOException e){
                    mLog.info("FIle Not Found");
                }
            
         int responseCode = ((HttpURLConnection) Uploadconn).getResponseCode();
         if (responseCode==200){
             //System.out.println("Upload file Success" );
             uploadSTATUSSEND(Id, SlipNo);
             mLog.info("Upload file with SlipNo: "+SlipNo+" and CardNo: "+CardNo+" Success" );
         } else {
              //System.out.println("FAIL!! Response Code  : " + responseCode);
              mLog.info("FAIL!! Response Code  : " + responseCode);
         }  
         } catch (ConnectException e) {
                 mLog.info("Koneksi HTTP / Service API Fail");    
            }
    }
   
 
    public static java.sql.Date getCurrentDatetime() {
        java.util.Date today = new java.util.Date();
        return new java.sql.Date(today.getTime());
    }

    static {
      System.setProperty("java.util.logging.SimpleFormatter.format",
              "[%1$tF %1$tT] [%4$-7s] %5$s %n");
      mLog = Logger.getLogger(KFTRN_ImageCapture.class.getName());
    }
      
}


