#pragma once
#include "cluster.pb.h"
#include "Point.h"

inline cluster::Point ConvertToRpcPoint(const Point& point)
{
	cluster::Point rpcPoint;
	rpcPoint.set_x(point.x);
	rpcPoint.set_y(point.y);
	rpcPoint.set_color(point.color);
	return rpcPoint;
}
