//
// Created by cobalt on 1/4/16.
//

#include <opencv2/core.hpp>
#include "RecolorRC.h"

using namespace mhealth;

void RecolorRCFilter::apply(cv::Mat &src, cv::Mat &dst)
{
    cv::split(src, mChannels);

    cv::Mat g = mChannels[1];
    cv::Mat b = mChannels[2];

    // dst.g = 0.5 * src.g + 0.5 * src.b
    cv::addWeighted(g, 0.5, b,  0.5, 0.0, g);

    // dst.b = dst.g
    g.copyTo(b);

    cv::merge(mChannels, 4, dst);
}