
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
 * Library kamera, Hosted on Github: https://github.com/Muktiadi/openCVJavaVMD/
 * 
 * @author Muktiadi A. Januar
 * 
 */
public class threadKamera  extends Thread {
    // inisiasi library openCV
    static{System.loadLibrary(Core.NATIVE_LIBRARY_NAME);}
    
    // membuat JFrame untuk layar tampilan kamera
    public JFrame monLiveStr = new JFrame("Siaran Langsung");  
    public JFrame monFrameBg = new JFrame("Layar background");  
    public JFrame monHasil = new JFrame("Hasil");  

    // membuat komponen JLabel sebagai tempat munculnya gambar
    public JLabel scrLiveStr = new JLabel("",SwingConstants.CENTER);
    public JLabel scrFrameBg = new JLabel("",SwingConstants.CENTER);
    public JLabel scrHasil = new JLabel("",SwingConstants.CENTER);
    
    // membuat JFrame & JLabel untuk Debugging
    public JFrame monDebug = new JFrame("Delta");  
    public JLabel scrDebug = new JLabel("",SwingConstants.CENTER);

    // menyetel resolusi layar output
    Size resMonitor = new Size(320,240);
    
    // Inisiasi variabel
    private int idKamera    = 0;
    private int frameStep   = 0;
    private int modeDeteksi = 1;

    private int resLebarKamera  = 352;
    private int resTinggiKamera = 240;

    
    private boolean berhenti    = false;
    private boolean kerjakan    = false;
    private boolean GUITerbuka  = false;
    
    // Inisiasi Matriks-matriks OpenCV
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
    
    // Inisiasi VideoCapture
    VideoCapture kamera = new VideoCapture(); 
       
    threadKamera() {                            // Constructor thread
        // tidak melakukan sesuatu saat thread dibuat
    }
    
    // fungsi yang dijalankan saat thread berjalan
    public void run() {
        // memanggil fungsi inisiasi (fungsi yang dijalankan sebelum loop
        // apa seharusnya di constructor saja ya?
        inisiasi();
        
        // Loop utama
        while (true) {
            // Mengecek apakah thread diinterupsi?
            if (!berhenti){
                // Mengecek apakah thread beroperasi atau standby?
                if (kerjakan)
                    // menjalankan fungsi utama thread
                    kerjakanTugasBerulang();
                else // jika thread tidak beroperasi, maka diam selama satu detik
                    tidur(1000);
            } else { // jika thread diinterupsi
                // Hentikan segala kegiatan
                bersihBersihThread();
                // Hentikan thread
                interrupt(); 
            }
        }
    }
    
    // fungsi untuk mengganti sumber input kamera
    public void setIdKamera(int _idKamera) {
        idKamera = _idKamera;
    }
    
    // fungsi untuk mengganti mode deteksi
    public void setModeDeteksi(int _modeDeteksi) {
        modeDeteksi = _modeDeteksi;
    }
    
    //  fungsi untuk mengubah resolusi kamera
    // BIGNOTE : hasil testing, fungsi ubah
    // resolusi kamera tidak berfungsi
    // something went wrong ..
    public boolean setResolusiKamera(int _resLebar, int _resTinggi) {
        resLebarKamera  = _resLebar;
        resTinggiKamera = _resTinggi;
        
        // cek apakah kamera tidak sedang tersambung
        if (!kamera.isOpened()) { // mengganti resolusi kamera
            kamera.set(Videoio.CAP_PROP_FRAME_WIDTH, resLebarKamera);
            kamera.set(Videoio.CAP_PROP_FRAME_HEIGHT, resTinggiKamera);
            return true; // resolusi sudah terganti
        } else // kamera sedang tersambung, permintaan tidak diproses
            return false;
        
        /* note: ketika kamera sedang terbuka apakah lebih
        * baik jika secara otomatis menutup sambungan kamera
        * lalu mengubah resolusi, dan ketika selesai maka
        * kamera secara otomatis menyambungkan kembali?
        */
    }
    
    // set Resolusi kamera saat thread diinisiasi
    public void setResolusiKamera() { // diisi nilai default
        kamera.set(Videoio.CAP_PROP_FRAME_WIDTH, resLebarKamera);
        kamera.set(Videoio.CAP_PROP_FRAME_HEIGHT, resTinggiKamera);
    }
    
    // fungsi untuk membuat tampilan GUI
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
    
    // fungsi untuk menampilkan atau menutup tampilan layar GUI
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
    
    // fungsi Thread.sleep(), agar lebih rapi dibuat fungsi
    public void tidur(int _miliDetik) {
        try {
            Thread.sleep(_miliDetik);
        } catch (InterruptedException ex) {
            Logger.getLogger(threadKamera.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // fungsi untuk mencoba koneksi kamera saat mencoba menyalakan layanan
    public boolean cobaKamera() {
        boolean hasil = false;
        
        // mengkoneksikan kamera
        kamera.open(idKamera);
        
        // mengecek apakah kamera terkoneksi
        if (kamera.isOpened()) {
            hasil = true;
                
            // mencoba untuk menangkap frame pada kamera
            kamera.read(matLiveStr);
            kamera.read(matFrameBg);
            kamera.read(matDebug);  
            kamera.read(matHasil); 
            
            // menampilkan layar monitor kamera
            tampilanGUI(true);
           
        } else // memutuskan koneksi kamera 
            kamera.release();
        
        return hasil;
    }
    
    // memberikan instruksi pada thread untuk menjalankan layanan
    public void kerjakan() {
        kerjakan = true;
    }
    
    // memberikan instruksi pada thread untuk menghentikan layanan
    public boolean hentikanPekerjakan() {
        // menghentikan pekerjaan tugas
        kerjakan = false;
        
        // menunggu satu detik agar tugas thread telah berhenti sepenuhnya
        tidur(1000);
        
        // membersikan thread dari kegiatan
        bersihBersihThread();
        return true;
    }
    
    // mendapatkan identitas kamera yang sedang aktif 
    public String getIdKamera() {
        return kamera.toString();
    }
    
    // mengupdate tampilan background
    public void setMatFrameBG() {
        kamera.read(matFrameBg);
        isiMonitor(1, matFrameBg); 
    }
    
    // menghentikan aktivitas thread
    public void hentikanThread(){ 
        berhenti = true;
    }
    
    // prosedur untuk menghentikan aktifitas pada thread
    public void bersihBersihThread() {
        if(kamera.isOpened())
            kamera.release();
        
        if(GUITerbuka)
            tampilanGUI(false);
    }    
    
    // prosedur untuk mengisi monitor dengan gambar dari Matriks
    public void isiMonitor(int _mon, Mat _frame) {
        // pemilihan layar monitor yang diupdate
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
    
    // menginisiasi thread
    public void inisiasi() {
        buatGUI();
        setResolusiKamera();
    }
    
    // Fungsi Loop utama ketika thread beroperasi
    public void kerjakanTugasBerulang() {
        // delay untuk mengatur FPS
        tidur(100);
        
        // mengupdate background
        feedFrameBg();
        
        // mengupdate layar monitor
        feedLiveStr();
        
        // fungsi untuk mencari perbedaan
        cariPerbedaan();
        
    }
    
    // prosedur untuk mengupdate layar stream kamera
    public void feedLiveStr() {
        // menangkap frame ke matriks
        kamera.read(matLiveStr);
        
        // mengupdate monitor ke 0 dengan matriks liveStream
        isiMonitor(0, matLiveStr);
    }
    
    // prosedur untuk mengupdate layar background
    public void feedFrameBg() {
        // mengecek mode deteksi
        // 1 : mode kontinyu
        // 2 : mode background tetap
        if (modeDeteksi == 1) {
            // update frame background setiap 2 frame
            frameStep++;
            if (frameStep >= 1) {
                frameStep = 0;
                // memindahkan matriks LiveStream ke matriks Background
                matLiveStr.copyTo(matFrameBg);
                // update layar 1 dengan matriks frame background
                isiMonitor(1, matFrameBg); 
            }
        }
    }
   
    // fungsi untuk mencari perbedaan antara 
    // matriks liveStream dan matriks Background
    public void cariPerbedaan() {
        matLiveStr.copyTo(matHasil);
        
        // menyamakan ukuran matriks 
        Imgproc.resize(matFrameBg, matFrameBgSc, matLiveStr.size());
        Imgproc.resize(matLiveStr, matLiveStrSc, matLiveStr.size());
        
        // menggunakan fungsi subtraksi untuk menghasilkan
        // matriks yang memuat delta-pixel
        Core.absdiff(matFrameBgSc, matLiveStrSc, matDiff);
        
        // Mengkonversi matriks subtraksi menjadi matriks grayscale
        Imgproc.cvtColor(matDiff, matAbu, Imgproc.COLOR_BGR2GRAY);
        
        // mengkonversi matriks grayscale menjadi matriks biner
        Imgproc.adaptiveThreshold(matAbu, matAbu, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 7, 5);
        
        // membuat arraylist of MatOfPoint dengan nama variabel kontur
        ArrayList<MatOfPoint> kontur = new ArrayList<MatOfPoint>();  
        
        // melakukan pencarian kontur dari matriks biner
        Imgproc.findContours(matAbu, kontur, new Mat(), Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);

        //  melakukan penggambaran kotak kotak
        // pada setiap kontur (perbedaan gambar)
        for(int i=0; i< kontur.size();i++) {
            if (Imgproc.contourArea(kontur.get(i)) > 100 ) {
                Rect rect = Imgproc.boundingRect(kontur.get(i));
                if ((rect.height > 50 && rect.height > 50)) {
                    Imgproc.rectangle(matHasil, new Point(rect.x,rect.y), new Point(rect.x+rect.width,rect.y+rect.height),new Scalar(0,0,255), 2);
                }
            }
        }
        
        // mengupdate layar 2 dengan matriks Hasil
        isiMonitor(2, matHasil); 
        
        // mengupdate layar 3 dengan matriks grayscale
        isiMonitor(3, matAbu);
        
    }
}
