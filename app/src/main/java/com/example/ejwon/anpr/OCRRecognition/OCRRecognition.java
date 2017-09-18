package com.example.ejwon.anpr.OCRRecognition;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.example.ejwon.anpr.common.ReadWriteImageFile;
import com.example.ejwon.anpr.common.Result;
import com.example.ejwon.anpr.common.Time;
import com.example.ejwon.anpr.common.Utils;
import com.example.ejwon.anpr.interfaces.OnTaskCompleted;
import com.example.ejwon.anpr.models.BitmapWithCentroid;
import com.example.ejwon.anpr.neural.SampleData;
import com.hazuu.uitanpr.neural.KohonenNetwork;

import junit.framework.Assert;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class OCRRecognition extends AsyncTask<Void, Bitmap, String> {

    private List<Rect> currentPlatesOnAsy;
    private Mat originImageOnAsy;
    private OnTaskCompleted listener;

    static final int DOWNSAMPLE_WIDTH = 20;
    static final int DOWNSAMPLE_HEIGHT = 50;
    private static final String TAG = "OCRRecognition.java";

    protected int pixelMap[];
    protected Bitmap newBitmap;
    protected double ratioX;
    protected double ratioY;
    protected int downSampleLeft;
    protected int downSampleRight;
    protected int downSampleTop;
    protected int downSampleBottom;
    KohonenNetwork net;
    Utils utils;
    public boolean isFail = false;
    public boolean isRunningTask = false;
    public static int i = 0;
    public static int ct = 0;
//    public static Result resultRec = new Result();

    public OCRRecognition(List<Rect> currentPlates, Mat originImage, boolean isRunningTask,
                          OnTaskCompleted listener, KohonenNetwork net, Utils utils) {
        this.currentPlatesOnAsy = new ArrayList<Rect>(currentPlates);
        this.originImageOnAsy = originImage;
        this.listener = listener;
        this.isRunningTask = isRunningTask;
        this.net = net;
        this.utils = utils;
    }

    @Override
    protected void onPreExecute() {
        isRunningTask = true;
    }

    @Override
    protected String doInBackground(Void... params) {
        Iterator<Rect> iterator = currentPlatesOnAsy.iterator();
        BitmapWithCentroid tempBitmap;
        long start, start2, start3;
        long timeRequired = 0, timeRequired2 = 0, timeRequired3 = 0, timeAllRequired = 0;
        String result = "";
        String cutResult = "";
        String resultTown = "";
        String recognizedText = "";
        int countOfCharInFirstSequence = 0;
        Result recognitionResult = new Result();
        Mat plateImageResized = null;

        while (iterator.hasNext()) {
            start = System.currentTimeMillis();
            Rect plateRect = iterator.next();
            Mat plateImage;
            List<BitmapWithCentroid> charList = new ArrayList<BitmapWithCentroid>();

            int x = plateRect.x - 500, y = plateRect.y, w = plateRect.width + 500, h = plateRect.height;

            if (x - 500 < 0)
                x = 0;
            if (w > 1280)
                w = 1280;

//            Log.e(TAG, "Coordinate x,y of numberPlate: " + x + ", " + y);
//            Log.e(TAG, "Width: " + w);
//            Log.e(TAG, "Height: " + h);


            Rect roi = new Rect((int) (x), (int) (y), (int) (w),
                    (int) (h));

            plateImage = new Mat(roi.size(), originImageOnAsy.type());

            plateImage = originImageOnAsy.submat(roi); //docinanie tablic z całego obrazu

            Bitmap imageBitmap13 = Bitmap.createBitmap(
                    plateImage.width(),
                    plateImage.height(),
                    Bitmap.Config.ARGB_8888);

            org.opencv.android.Utils.matToBitmap(
                    plateImage, imageBitmap13);

//            File folder13 = new File(Environment.getExternalStorageDirectory() + File.separator + "ANPR_doc/");
//            folder13.mkdir();
//            ReadWriteImageFile.saveBitmapAsJpegFile(imageBitmap13, folder13);

            plateImageResized = new Mat(); //zimenione, wczesniej bylo tu definiowane - 17_09

            //jednostkowy wymiar
            Imgproc.resize(plateImage, plateImageResized, new Size(680,
                    492)); //dlaczego do rozmiaru 680, 492? - zwiekszone zdjecie i zwężone

//            Mat plateImageGrey = new Mat();

//            Imgproc.cvtColor(plateImageResized, plateImageGrey,
//                    Imgproc.COLOR_BGR2GRAY, 1);

//            Imgproc.equalizeHist(plateImageResized, plateImageResized); // wtym miejscu powoduje zanieczyczenia, wykrywa zmięte krawędzie kartki, odpada
//            Utils.saveImageToFile(plateImageResized, "HistogramEqual/");
            Imgproc.medianBlur(plateImageResized, plateImageResized, 1); //wygładzenie obrazu, "wyczyszczenie" z zanieczyszczen obrazu
            //zalecane przed metoda wykrywania krawedzi find contours ponieważ metoda findcontoure jest podatna na szumy
            // celu wyodrebnienia głownych kolorów

//            ReadWriteImageFile.saveImageToFile(plateImageResized, "MedianFilter/");

            Imgproc.adaptiveThreshold(plateImageResized, plateImageResized,
                    255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
                    Imgproc.THRESH_BINARY, 85, 5); //binaryzacja obrazu w skali szarości - pięknie zbinaryzowane - OK
            //dopisane -- zbinaryzowana tablica na 0 i 1
//            ReadWriteImageFile.saveImageToFile(plateImageResized, "ANPR_plate2/");


            List<MatOfPoint> contours = new ArrayList<MatOfPoint>(); //rozpoznane kontury, vector of points

            Mat hierarchy = new Mat(plateImageResized.rows(),
                    plateImageResized.cols(), CvType.CV_8UC1,
                    new Scalar(0));

            //Wydzielanei znaków tablivy metoda konturową
            //Algorytm wykrywania kontur uzyty jako segmentacja liter na podstawie wykrytych kontur
            // zwracana jest lista znalezionych konturów ( jeden obraz z wykrytymi konturami)
            Imgproc.findContours(plateImageResized, contours, hierarchy,
                    Imgproc.CHAIN_APPROX_SIMPLE, Imgproc.RETR_LIST); //kolejnosc zamieniona? Aprox i retr list?
//            ReadWriteImageFile.saveImageToFile(plateImageResized, "ANPR_contours2");


            Imgproc.equalizeHist(plateImageResized, plateImageResized);
//            ReadWriteImageFile.saveImageToFile(plateImageResized, "HistogramEqual/");

            // segmentacja
//            String recognizedText = "";
            timeRequired = System.currentTimeMillis() - start;
//            Log.e(TAG, "Time for find countour: " + timeRequired);
//            Log.e(TAG, "Start loop!!!" + contours.size());
            start2 = System.currentTimeMillis();

//            List<Coordinate> coordinates = new ArrayList<Coordinate>();
//            int corOrder = 0;

            //http://grokbase.com/t/gg/android-opencv/123tqz6494/regarding-largest-contour-centroid-of-contour
            for (int i = 0; i < contours.size(); i++) {
                List<Point> goodpoints = new ArrayList<Point>();
                Mat contour = contours.get(i);
                int num = (int) contour.total(); //return the number of array elements
                int buff[] = new int[num * 2]; // [x1, y1, x2, y2, ...]
                contour.get(0, 0, buff);
                for (int q = 0; q < num * 2; q = q + 2) {
                    goodpoints.add(new Point(buff[q], buff[q + 1])); //P(buff[0], buff[1], P(buff[2], buff[3]),  P(buff[4], buff[5])- ArrayLista punktów
                }

                //nastepnie aby ulatwic analize konturu wyznacza się jego prostokąt otaczający
                MatOfPoint points = new MatOfPoint();
                points.fromList(goodpoints);
                Rect boundingRect = Imgproc.boundingRect(points); // wylicza i zwraca najmniejszy prostokąt dla zbioru punktów

//                ReadWriteImageFile.saveImageToFile(plateImageResized, "ANPR_drawCountours/"); //powoduje zwolnienie obliczeń

//                if(((boundingRect.height/boundingRect.width) >= 0.45) && ((boundingRect.height / boundingRect.width) <= 0.62) && ((boundingRect.height * boundingRect.width) >= 5000)){
//                }

                //NIE ZMIENIAC!!!!!!!!!!! POWODUJE BłEDY W ROZPOZNAWANIU
                //wyznaczenie progów W/H W*H czyli maksymalnych wartosci konturu otaczajacego potencjalna litere
                if (((boundingRect.height / boundingRect.width) >= 1.5) //stosunek dlugosci boków prosotkąta W/H nie moze byc mniejsza niz 1,5
                        && ((boundingRect.height / boundingRect.width) <= 3.0)    //stosunek dlugosci boków W/H nie moze byc większa niz 3
                        && ((boundingRect.height * boundingRect.width) >= 5000)) { //iloczyn dlugosci boków nie moze byc większy niz 5000, średnio wysokośc litery wynosi
                    //Hobrazu/3 czyli 492/3 = 164, oraz Wobrazu/15 = 45,3 (lub 55) czyli iloczyn h i w wynosi srednio 7380

                    int cx = boundingRect.x + (boundingRect.width / 2); // x i y to współrzedne górnegolewego boku protsotkąta
                    int cy = boundingRect.y + (boundingRect.height / 2);

                    Point centroid = new Point(cx, cy); // to jest środek (wspolrzedne x i y) prostokąta - centroid


                    if (centroid.y >= 120 && centroid.y <= 400          // tablica znajduje sie mniej wiecej na srodku, wartosc wspolrzednych centroidu nei oze byc mniejsza niz te
                            && centroid.x >= 100 && centroid.x <= 590) { // wartości ktore liczone sa od poczatkow krawedzi h i w (mniej wiecej na oko liczone)

                        int calWidth = (boundingRect.width + 5)  // b.width w przyblizeniu 45 + 5 - (45 + 5) mod 4 = 50 - 2 = 48
                                - (boundingRect.width + 5) % 4;

//                        corOrder = corOrder + 1;
//                        Coordinate cord = new Coordinate(boundingRect.tl(),boundingRect.br(),corOrder);
//                        coordinates.add(cord);

                        Rect cr = new Rect(boundingRect.x,
                                boundingRect.y, calWidth,
                                boundingRect.height); // nowy obwód (prostokąt)dla litery

                        Mat charImage = new Mat(
                                cr.size(),
                                plateImageResized.type());

                        charImage = plateImageResized.submat(cr); // "wycina" macierz o danym wymiarze prostokątam w której znajduje się litera
//                        Log.e(TAG, "Channels of CharImage: " + charImage.channels());

                        //dopisane -- Litery wyciete, więcej niz 2 kolory ale blizej binaryzacji -- Prawie OK
//                        ReadWriteImageFile.saveImageToFile(charImage, "ANPR_znaki2/");

//                        MatOfPoint cnt =  contours.get(i);
//                        MatOfPoint2f mat2f = new MatOfPoint2f(points.toArray());
//                        Mat rotatedChar = AspectOfPlate.computeSkew(mat2f ,charImage);
//                        Mat rotatedChar = AspectOfPlate.computeSkew(charImage);
//
//                        Imgproc.equalizeHist(rotatedChar, rotatedChar);
//                        ReadWriteImageFile.saveImageToFile(rotatedChar, "HistogramEqual/");

                        //odkomentowanie spowoduje blad liczby kanałow (channels), mamy 1 kanałowy
//                        Imgproc.cvtColor(charImage, charImageGrey,
//                                Imgproc.COLOR_BGR2GRAY, 1);
                        Imgproc.adaptiveThreshold(charImage,
                                charImage, 255,
                                Imgproc.ADAPTIVE_THRESH_MEAN_C,
                                Imgproc.THRESH_BINARY, 85, 5);

                        Bitmap charImageBitmap = Bitmap.createBitmap(
                                charImage.width(),
                                charImage.height(),
                                Bitmap.Config.ARGB_8888);

                        //possible characters
                        org.opencv.android.Utils.matToBitmap(
                                charImage, charImageBitmap);
//                        Log.e(TAG, "Before save");

//                        File folder5 = new File(Environment.getExternalStorageDirectory() + File.separator + "ANPR_threshold_second2/"); //sprawdzenie wyciętych liter
//                        folder5.mkdirs();
//                        ReadWriteImageFile.saveBitmapAsJpegFile(charImageBitmap, folder5);


                        tempBitmap = new BitmapWithCentroid(
                                charImageBitmap, centroid, boundingRect.tl(), boundingRect.br());
                        charList.add(tempBitmap);

                    }
                }
                // }
            }

            timeRequired2 = System.currentTimeMillis() - start2;
//            Log.e(TAG, "Passed the loop");
//            Log.e(TAG, "Time for OCR: " + timeRequired2);

            start3 = System.currentTimeMillis();
            Collections.sort(charList);

            //DOWNSAMPLE_WIDTH oraz DOWNSAMPLE_HEIGHT wymiar gridu w do którego zmniejszamy znak - downsampling image
            SampleData data = new SampleData('?', DOWNSAMPLE_WIDTH,
                    DOWNSAMPLE_HEIGHT);

//            int countOfCharInFirstSequence = 0;
            int sizeOfCharList = charList.size();
//            MaxGap maxGap = new MaxGap();
//            double max = 0.0;
//            Log.e(TAG, "sizeOfCharList: " + sizeOfCharList);
            for (int index = 0; index < sizeOfCharList; index++) {
                newBitmap = charList.get(index).getBitmap();
                int nextIndex = index + 1;
                if (nextIndex < sizeOfCharList) {
                    double pNextTL = charList.get(nextIndex).getpTopLeft().x;
                    double pNextBR = charList.get(nextIndex).getpBottomRight().x;
                    double pActualTL = charList.get(index).getpTopLeft().x;
                    double pActualBR = charList.get(index).getpBottomRight().x;
                    double diff = Math.abs(pActualBR - pNextTL);
//                    Log.d("OCRRecognition", "pActualTL:" + pActualTL + "," + "pActualBR:" + pActualBR + ",pNextTL:" + pNextTL + ",pNextBR:" + pNextBR + " - diff: " + diff);
//                    Log.d("OCRRecognition", "pActualBR:" + pActualBR + ",pNextTL:" + pNextTL +  ", Diff " + diff);
//                    if (diff > max){
//                        max = diff;
//                        maxGap.setIndex(index);
//                        maxGap.setMax(max);
//                        Log.d("OCRRecognition", "MAX  and diff " + max + " ," + diff);
//                    }

                    if (diff >= 25 && diff <= 45) {
                        countOfCharInFirstSequence = index;
//                        Log.d("OCRRecognition", "Diff " + diff + ", countOfCharInFirstSequence: " + countOfCharInFirstSequence);
                        break;
                    }
                }


                final int wi = newBitmap.getWidth();
                final int he = newBitmap.getHeight();

                pixelMap = new int[newBitmap.getHeight()
                        * newBitmap.getWidth()];
                newBitmap.getPixels(pixelMap, 0, newBitmap.getWidth(),
                        0, 0, newBitmap.getWidth(),
                        newBitmap.getHeight());

                findBounds(wi, he);

                ratioX = (double) (downSampleRight - downSampleLeft)
                        / (double) data.getWidth();
                ratioY = (double) (downSampleBottom - downSampleTop)
                        / (double) data.getHeight();

                for (int yy = 0; yy < data.getHeight(); yy++) {
                    for (int xx = 0; xx < data.getWidth(); xx++) {
                        if (downSampleRegion(xx, yy)) {
                            data.setData(xx, yy, true);
                        } else {
                            data.setData(xx, yy, false);
                        }
                    }
                }
                //downsampling //https://www.youtube.com/watch?v=Sq_PrLNLLMU
                // if pixel is solid 0.5 if not -0.5 (0.5 black pixel, -0.5 white pixel)
                final double input[] = new double[20 * 50];
                int idx = 0;
                for (int yy = 0; yy < data.getHeight(); yy++) {
                    for (int xx = 0; xx < data.getWidth(); xx++) {
                        input[idx++] = data.getData(xx, yy) ? 0.5
                                : -0.5;
                    }
                }

                double normfac[] = new double[1];
                double synth[] = new double[1];

                Assert.assertNotNull(input);
                if (input == null) {
                    Log.e(TAG, "Input array is Null");

                }


                int best = net.winner(input, normfac, synth); //rozpoznawanie litery z jakis danych
                //input neuron,method indentify which of the 1000 neurons won, store this information in the best integer

                recognizedText += net.getMap()[best];
                Log.e(TAG, "Plate number:" + recognizedText);


            }
//            Log.d("OCRRecognition", "After max: " + max );

//            recognizedText = utils.formatPlateNumber(recognizedText);


            if (TextUtils.isEmpty(result))
                result = recognizedText;
            else
                result += "\n" + recognizedText;

            timeRequired3 = System.currentTimeMillis() - start3;
//            Log.e(TAG, "Time: " + timeRequired3);
            timeAllRequired = System.currentTimeMillis() - start;
//            Log.d("Summary time: ", "Time: " + timeAllRequired);
//            Log.e(TAG, "Plate number:" + recognizedText);

        }

        int sizeResult = result.length();
        Log.e(TAG, "Length: " + sizeResult + "Result: " + result);
        if (!result.isEmpty() && sizeResult >= 2) {

            result = utils.formatPlateNumber(result);

//            Log.e(TAG, "Length2: " + result.length() + "Result: " + result);

            if (countOfCharInFirstSequence > 0) {
                if(sizeResult ==  countOfCharInFirstSequence + 1) {
                    result = result.substring(0, countOfCharInFirstSequence + 1);
                }

            } else if(countOfCharInFirstSequence == 0 && sizeResult >= 3) {
                result = result.substring(0, 3);
            }

            recognitionResult.setRecognizedNumber(result);
//            recognitionResult.setRecognizedTown(utils.lookingForPlate(recognitionResult));
            recognitionResult = utils.lookingForPlate(recognitionResult);
            resultTown = recognitionResult.getRecognizedTown();
            recognitionResult.setAllTimes(new Time(timeRequired, timeRequired2, timeRequired3, timeAllRequired));
            ReadWriteImageFile.saveResultToFile(recognitionResult);
//            ReadWriteImageFile.saveImageToFile(plateImageResized, "ANPR_drawCountours/");

        }


//        recognitionResult = ReadWriteImageFile.saveRecognizedTownToFile(resultRec, result, i, ct);
//        i = recognitionResult.getNumberIteration();
//        ct = recognitionResult.getNumberTown();


//        if( !result.isEmpty()) {
//            String districtNumber = result.substring(0, 2);
//            Log.e(TAG, "Plate: " + districtNumber);
//            String recognizedTown = ReadJsonFile.ReadFile(districtNumber);
//            Log.e(TAG, "Town: " + recognizedTown);
//            ReadWriteImageFile.saveRecognizedTextToFile(recognizedTown, i, ct);
//        }


        return resultTown;
    }

    @Override
    protected void onProgressUpdate(Bitmap... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String aResult) {
        isRunningTask = false;
        if (!TextUtils.isEmpty(aResult)) {
            Log.e(TAG, "onPostExecute: isFail=" + isFail);
            isFail = false;
        } else {
            isFail = true;
            Log.e(TAG, "onPostExecute: isFail=" + isFail);

        }
        listener.updateResult(aResult);
    }

    /**
     * This method is called to automatically crop the image so that whitespace
     * is removed.
     *
     * @param w The width of the image.
     * @param h The height of the image
     */
    protected void findBounds(final int w, final int h) {
        // top line
        for (int y = 0; y < h; y++) {
            if (!hLineClear(y)) {
                this.downSampleTop = y;
                break;
            }

        }
        // bottom line
        for (int y = h - 1; y >= 0; y--) {
            if (!hLineClear(y)) {
                this.downSampleBottom = y;
                break;
            }
        }
        // left line
        for (int x = 0; x < w; x++) {
            if (!vLineClear(x)) {
                this.downSampleLeft = x;
                break;
            }
        }

        // right line
        for (int x = w - 1; x >= 0; x--) {
            if (!vLineClear(x)) {
                this.downSampleRight = x;
                break;
            }
        }
    }

    protected boolean downSampleRegion(final int x, final int y) {
        final int w = this.newBitmap.getWidth();
        final int startX = (int) (this.downSampleLeft + (x * this.ratioX));
        final int startY = (int) (this.downSampleTop + (y * this.ratioY));
        final int endX = (int) (startX + this.ratioX);
        final int endY = (int) (startY + this.ratioY);

        for (int yy = startY; yy <= endY; yy++) {
            for (int xx = startX; xx <= endX; xx++) {
                final int loc = xx + (yy * w);
                if (this.pixelMap[loc] != -1) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * This method is called internally to see if there are any pixels in the
     * given scan line. This method is used to perform autocropping.
     *
     * @param y The horizontal line to scan.
     * @return True if there were any pixels in this horizontal line.
     */
    protected boolean hLineClear(final int y) {
        final int w = this.newBitmap.getWidth();
        for (int i = 0; i < w; i++) {
            if (this.pixelMap[(y * w) + i] != -1) { //isBlackpixel
                return false;
            }
        }
        return true;
    }

    /**
     * This method is called to determine ....
     *
     * @param x The vertical line to scan.
     * @return True if there are any pixels in the specified vertical line.
     */
    protected boolean vLineClear(final int x) {
        final int w = this.newBitmap.getWidth();
        final int h = this.newBitmap.getHeight();
        for (int i = 0; i < h; i++) {
            if (this.pixelMap[(i * w) + x] != -1) {
                return false;
            }
        }
        return true;
    }
}