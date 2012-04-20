#include <iostream>
#include "cv.h"
#include "highgui.h"
#include <zbar.h>

using namespace std;
using namespace cv;
using namespace zbar;

int main(int, char**)
{


  ImageScanner scanner;
  scanner.set_config(ZBAR_NONE, ZBAR_CFG_ENABLE, 1);


    VideoCapture cap(0); // open the default camera
    if(!cap.isOpened())  // check if we succeeded
        return -1;

    Mat edges;
    namedWindow("edges",1);
    int doit = 16;
    for(;;)
    {
        Mat frame;
        cap >> frame; // get a new frame from camera
        cvtColor(frame, edges, CV_BGR2GRAY);
//        GaussianBlur(edges, edges, Size(7,7), 1.5, 1.5);
//        Canny(edges, edges, 0, 30, 3);

if (!--doit) { 
    Image image(edges.cols, edges.rows, "Y800", edges.data, edges.cols*edges.rows);

    int n = scanner.scan(image);
    cout << "Codes:" << n << endl;
    // extract results
    for(Image::SymbolIterator symbol = image.symbol_begin();
        symbol != image.symbol_end();
        ++symbol) {
        // do something useful with results
        cout << "decoded " << symbol->get_type_name()
             << " symbol \"" << symbol->get_data() << '"' << endl;
        imwrite("found.jpg", edges);
    }
    doit = 16;
}
        imshow("edges", frame);
    }
    // the camera will be deinitialized automatically in VideoCapture destructor
    return 0;
}
