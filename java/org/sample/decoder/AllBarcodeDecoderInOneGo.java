package org.sample.decoder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.GenericMultipleBarcodeReader;
import com.google.zxing.multi.MultipleBarcodeReader;


public class AllBarcodeDecoderInOneGo {
    
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis(); 
        
        String folderPath = "C:\\Users\\kusha\\OneDrive\\Desktop\\workspace\\decoder\\New Folder\\New folder";
        String outputFolderPath = "C:\\Users\\kusha\\OneDrive\\Desktop\\workspace\\decoder\\ProcessedImages";

        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("Invalid folder path: " + folderPath);
            return;
        }

        File outputFolder = new File(outputFolderPath);
        if (!outputFolder.exists()) {
            if (outputFolder.mkdirs()) {
                System.out.println("Output directory created: " + outputFolderPath);
            } else {
                System.err.println("Failed to create output directory: " + outputFolderPath);
                return;
            }
        }

        Set<String> processedFiles = new HashSet<>();

        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png"));

        if (files != null) {
            for (File file : files) {
                if (!processedFiles.contains(file.getName())) {
                    processImage(file, outputFolderPath);
                    processedFiles.add(file.getName());
                }
            }
        } 
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        System.out.println("Total execution time: " + executionTime + " milliseconds");
    }

    private static void processImage(File file, String outputFolderPath) {
        try {
            String imagePath = file.getAbsolutePath();
            Mat image = Imgcodecs.imread(imagePath);

      
            int[][] barcodeCoordinates = {
                //{197, 188, 185, 217},  
                {1281,101,286,134},
               // {78, 1360, 288, 222},   
                
            };

            for (int[] coords : barcodeCoordinates) {
                int x = coords[0], y = coords[1], width = coords[2], height = coords[3];
                Rect roi = new Rect(x, y, width, height);
                Mat boxRegion = new Mat(image, roi);

               //Grayscale
                Mat grayImage = new Mat();
                Imgproc.cvtColor(boxRegion, grayImage, Imgproc.COLOR_BGR2GRAY);

                
                Mat blurredImage = new Mat();
                Imgproc.GaussianBlur(grayImage, blurredImage, new Size(5, 5), 0);

              
                Mat edges = new Mat();
                Imgproc.Laplacian(blurredImage, edges, CvType.CV_8U);

      
                Mat sharpenedImage = new Mat();
                Core.addWeighted(grayImage, 0.5, edges, -3.0, 0, sharpenedImage);

            
                Mat contrastImage = new Mat();
                double alpha = 1.8; 
                int beta = 50; 
                sharpenedImage.convertTo(contrastImage, -1, alpha, beta);

         
                decodeBarcode(contrastImage);
         
                String outputImagePath = outputFolderPath + "\\" + file.getName();
                Imgcodecs.imwrite(outputImagePath, contrastImage);
            }

     
            

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void decodeBarcode(Mat image) {
        try {
            File tempFile = File.createTempFile("barcode", ".png");
            Imgcodecs.imwrite(tempFile.getAbsolutePath(), image);
            
            BufferedImage bufferedImage = ImageIO.read(tempFile);
            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            MultiFormatReader multiFormatReader = new MultiFormatReader();
            MultipleBarcodeReader reader = new GenericMultipleBarcodeReader(multiFormatReader);

            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            hints.put(DecodeHintType.POSSIBLE_FORMATS, Arrays.asList(BarcodeFormat.values()));
            hints.put(DecodeHintType.PURE_BARCODE, Boolean.FALSE);

            
            try {
                Result[] results = reader.decodeMultiple(bitmap, hints);
     
                List<String> barcodes = new ArrayList<>();
                for (Result result : results) {
                    barcodes.add(result.getText());
                    System.out.println("Barcode Data: " + result.getText());
                }
            } catch (NotFoundException e) {
                System.err.println("No barcode found in the image: " + e.getMessage());
            } finally {
                tempFile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


