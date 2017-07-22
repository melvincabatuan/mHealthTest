//
// Created by cobalt on 1/5/16.
//

#ifndef MHEALTH_COMMON_H
#define MHEALTH_COMMON_H

#include <android/log.h>
#include <opencv2/imgproc.hpp>

#define LOG_TAG "mhealth_vision"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))

// Log check : The circle is printed
/*
void mLog(cv::Mat src, char message[], cv::Point start){
   cv::putText(src, message, start, cv::FONT_HERSHEY_SIMPLEX, 1, cv::Scalar(0,200,200), 3);
}
*/

#endif //MHEALTH_COMMON_H
