#pragma once
#include "cluster.pb.h"

struct RawPoint
{
	double x, y, z;
	operator cluster::Point() const
    {
        cluster::Point rpcPoint;
        rpcPoint.set_x(x);
        rpcPoint.set_y(y);
        rpcPoint.set_z(z);
        return rpcPoint;
    }
};
