
import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

/**
 * Library kamera, Hosted on Github 
 * 
 * @author Muktiadi A. Januar
 * 
 */
public class threadKamera  extends Thread {
    static{System.loadLibrary(Core.NATIVE_LIBRARY_NAME);}
    
    public JFrame monLiveStr = new JFrame("Siaran Langsung");  
    public JFrame monFrameBg = new JFrame("Layar background");  
    public JFrame monHasil = new JFrame("Hasil");  

    public JLabel scrLiveStr = new JLabel("",SwingConstants.CENTER);
    public JLabel scrFrameBg = new JLabel("",SwingConstants.CENTER);
    public JLabel scrHasil = new JLabel("",SwingConstants.CENTER);
    
    public JFrame monDebug = new JFrame("Delta");  
    public JLabel scrDebug = new JLabel("",SwingConstants.CENTER);

    Size resMonitor = new Size(320,240);
    
    private int idKamera    = 0;
    private int frameStep   = 0;
    private int modeDeteksi = 1;

    private int resLebarKamera  = 352;
    private int resTinggiKamera = 240;

    
    private boolean berhenti    = false;
    private boolean kerjakan    = false;
    private boolean GUITerbuka  = false;
    
    public Mat matLiveStr  = new Mat();
    public Mat matFrameBg  = new Mat();
    public Mat matDiff     = new Mat();
    public Mat matAbu      = new Mat();
    public Mat matHasil    = new Mat();
    public Mat matDebug    = new Mat();

    public Mat matLiveStrSc  = new Mat();
    public Mat matFrameBgSc  = new Mat();
    public Mat matDiffSc     = new Mat();
    public Mat matAbuSc      = new Mat();
    public Mat matHasilSc    = new Mat();
    public Mat matDebugSc    = new Mat();
    

    VideoCapture kamera = new VideoCapture(); 
       
    threadKamera() {                            // Constructor thread
        
    }
    
    public void run() {
        inisiasi();                             // Inisiasi awal
        while (true) {
            if (!berhenti){                     // cek thread diinterupsi?
                if (kerjakan)                   // cek status layanan
                    kerjakanTugasBerulang();    // fungsi utama thread
                else 
                    tidur(1000);                // tunggu
            } else {
                bersihBersihThread();           // menghentikan aktifitas thread
                interrupt();                    // hentikan thread ini
            }
        }
    }
    
    public void setIdKamera(int _idKamera) {
        idKamera = _idKamera;
    }
    
    public void setModeDeteksi(int _modeDeteksi) {
        modeDeteksi = _modeDeteksi;
    }
    
    public boolean setResolusiKamera(int _resLebar, int _resTinggi) {
        resLebarKamera  = _resLebar;
        resTinggiKamera = _resTinggi;
                
        if (!kamera.isOpened()) {
            kamera.set(Videoio.CAP_PROP_FRAME_WIDTH, resLebarKamera);
            kamera.set(Videoio.CAP_PROP_FRAME_HEIGHT, resTinggiKamera);
            return true;
        } else 
            return false;
    }
    
    public void setResolusiKamera() {
        kamera.set(Videoio.CAP_PROP_FRAME_WIDTH, resLebarKamera);
        kamera.set(Videoio.CAP_PROP_FRAME_HEIGHT, resTinggiKamera);
    }
    
    public void buatGUI() {
        monLiveStr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        monLiveStr.getContentPane().add(scrLiveStr, BorderLayout.CENTER); 
        monLiveStr.setLocation(50,50);
        monLiveStr.pack(); 

        
        monFrameBg.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        monFrameBg.getContentPane().add(scrFrameBg, BorderLayout.CENTER); 
        monFrameBg.setLocation(50,400);
        monFrameBg.pack(); 

        monHasil.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        monHasil.getContentPane().add(scrHasil, BorderLayout.CENTER); 
        monHasil.setLocation(500,50);
        monHasil.pack(); 

        
        monDebug.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        monDebug.getContentPane().add(scrDebug, BorderLayout.CENTER); 
        monDebug.setLocation(500,400);
        monDebug.pack(); 

    }
    
    public void tampilanGUI(boolean _set){
        if (_set) {
            monLiveStr.setVisible(true); 
            monFrameBg.setVisible(true); 
            monHasil.setVisible(true); 

            monDebug.setVisible(true);

            GUITerbuka = true;
        } else {
            monLiveStr.setVisible(false); 
            monFrameBg.setVisible(false); 
            monHasil.setVisible(false); 

            monDebug.setVisible(false); 

            GUITerbuka = false;   
        }        
    }
    
    
    public void tidur(int _miliDetik) {
        try {
            Thread.sleep(_miliDetik);
        } catch (InterruptedException ex) {
            Logger.getLogger(threadKamera.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean cobaKamera() {
        boolean hasil = false;
        kamera.open(idKamera);
        
        if (kamera.isOpened()) {
            hasil = true;
            tampilanGUI(true);
            kamera.read(matLiveStr);
            kamera.read(matFrameBg);
            kamera.read(matDebug);  
            kamera.read(matHasil);            
        } else 
            kamera.release();
        
        return hasil;
    }
    
    public void kerjakan() {
        kerjakan = true;
    }
    
    public boolean hentikanPekerjakan() {
        kerjakan = false;
        tidur(1000);
        bersihBersihThread();
        return true;
    }
    
    public String getIdKamera() {
        return kamera.toString();
    }
    
    public void setMatFrameBG() {
        kamera.read(matFrameBg);
        isiMonitor(1, matFrameBg); 
    }
    
    public void hentikanThread(){ 
        berhenti = true;
    }
    
    public void bersihBersihThread() {
        if(kamera.isOpened())
            kamera.release();
        
        if(GUITerbuka)
            tampilanGUI(false);
    }    
    
    public void isiMonitor(int _mon, Mat _frame) {
        switch (_mon) {
            case 0:
                Imgproc.resize(_frame,_frame,resMonitor);
                scrLiveStr.setIcon(new ImageIcon(createAwtImage(_frame)));
                monLiveStr.pack();
                break;
            case 1:
                Imgproc.resize(_frame,_frame,resMonitor);
                scrFrameBg.setIcon(new ImageIcon(createAwtImage(_frame)));
                monFrameBg.pack();
                break;
            case 2:
                Imgproc.resize(_frame,_frame, new Size(500,400));
                scrHasil.setIcon(new ImageIcon(createAwtImage(_frame)));
                monHasil.pack();
                break;
            case 3:
                Imgproc.resize(_frame,_frame,resMonitor);
                scrDebug.setIcon(new ImageIcon(createAwtImage(_frame)));
                monDebug.pack();
                break;
            default:
        }
        
    }
    
    
    /* Fungsi konversi openCV.Mat ke icon
    *   
    * Dikutip dari :
    * OpenCV output Using Mat object in Jpanel
    * Author: lukk, on Mar 10 '14 at 23:07
    * https://stackoverflow.com/questions/22284823/opencv-output-using-mat-object-in-jpanel
    */
    public BufferedImage createAwtImage(Mat mat) {
	int type = 0;
        switch (mat.channels()) {
            case 1:
                type = BufferedImage.TYPE_BYTE_GRAY;
                break;
            case 3:
                type = BufferedImage.TYPE_3BYTE_BGR;
                break;
            default:
                return null;
        }

	BufferedImage image = new BufferedImage(mat.width(), mat.height(), type);
	WritableRaster raster = image.getRaster();
	DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
	byte[] data = dataBuffer.getData();
	mat.get(0, 0, data);

	return image;
    }
    // End of Fungsi konversi openCV.Mat ke awt icon
    
    
    public void inisiasi() {
        buatGUI();
        setResolusiKamera();
    }
    
    // Fungsi Loop utama ketika thread beroperasi
    public void kerjakanTugasBerulang() {
        tidur(100);
        feedFrameBg();
        feedLiveStr();
        
        cariPerbedaan();
        
    }
    
    
    public void feedLiveStr() {
        kamera.read(matLiveStr);
        isiMonitor(0, matLiveStr);

    }
    
    public void feedFrameBg() {
        
        if (modeDeteksi == 1) {
            frameStep++;
            
            if (frameStep >= 1) {
                frameStep = 0;
                
                matLiveStr.copyTo(matFrameBg);
                
                isiMonitor(1, matFrameBg); 
            }
        }
    }
   
    
    public void cariPerbedaan() {
        matLiveStr.copyTo(matHasil);
        
        Imgproc.resize(matFrameBg, matFrameBgSc, matLiveStr.size());
        Imgproc.resize(matLiveStr, matLiveStrSc, matLiveStr.size());
        
        Core.absdiff(matFrameBgSc, matLiveStrSc, matDiff);
        
        Imgproc.cvtColor(matDiff, matAbu, Imgproc.COLOR_BGR2GRAY);
        
        Imgproc.adaptiveThreshold(matAbu, matAbu, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 7, 5);
        
        ArrayList<MatOfPoint> kontur = new ArrayList<MatOfPoint>();  
        Imgproc.findContours(matAbu, kontur, new Mat(), Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);

        for(int i=0; i< kontur.size();i++) {
            if (Imgproc.contourArea(kontur.get(i)) > 100 ) {
                Rect rect = Imgproc.boundingRect(kontur.get(i));
                if ((rect.height > 50 && rect.height > 50))
                {
                    Imgproc.rectangle(matHasil, new Point(rect.x,rect.y), new Point(rect.x+rect.width,rect.y+rect.height),new Scalar(0,0,255), 2);
                }
            }
        }
        
        isiMonitor(3, matAbu);
        isiMonitor(2, matHasil);        
    }
}
