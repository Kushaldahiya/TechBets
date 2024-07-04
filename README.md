# TechBets
Barcode Decoder

First I preprocess the image using opencv in which i apply the gaussian blurs, Laplacian edge detection and adjusted there values to read all barcodes and qrcodes.
second the processed image is decode by using zxing.

App.java file contains the ciode in multithreaded environment to reduce the execution time .
