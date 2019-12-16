#include <iostream>
#include <math.h>
#include <stdlib.h>
#include <vector>
#include <fstream>

double l = 50;
double h = 50;
double Ts = 300;
double a = 2.3*1e-5;


double U (double R, int I, int t) {
    double uk = 0.24 * R * I * I * t;
    return uk;
}

double Ox (double T0, double a) {
    double T = Ts + (T0 - Ts) * (pow(exp(1), (-a * 1200)));
    return T;
}

double Uk_next (double uk, double ui_next, double ui_pr) {
    double uk_next = (1 - ((2 * a * l)/(h*h))) * uk + (((a * l)/(h*h)) * (ui_next + ui_pr));
    return uk_next;
}



using namespace std;
int main() {
    // тут вводим константы типа R, I, k, u1...u6
    double R = 0.1;
    double I = 300;
    double u = 300;
    double eps = 0.1;
    int x = 50;
    int y = 50;
    int n = 2;
    int m = 3;
    double x_cor[2] = {50, 100};
    double y_cor[3] = {50, 100, 150};
    // тут цикл от 1 до 6 в нем будет рассчитываться нагрев узла через функцию U и еще один цикл {
    // u1 = U( argumentbl ) и так далее с каждым шагом цикла
    // новый цикл по времени,{ в нём u_ago = u1...6, u_next = Uk_next(); uk_pr = ui_next = ui_pr = uk_next (это равенство всегда сохраняется)
    // дальше в цикле идет рассчет u1...u6 =Ox(args)  (по шагу основного цикла)
    // каждую секунду заполняется массив ( каждую секунду новые значения, записывается uk_next, u1...6)
    // цикл выполняется пока abs(u1..6 - u_ago) < eps, eps = 0.1, как только условие не выполняется, основной цикл делает шаг}
    //результат программы - 6 массивов( две строки, неизвестно сколько столбцов)
    vector<double>** u_values_vector = new vector<double>*[n];
    for(int i = 0; i < n; i++)
    {
	u_values_vector[i] = new vector<double>[m];
    }

    for (int i = 0; i < n; ++i) {
	for (int j = 0; j < m; ++j)
	{
            u = U(R, I, 3);
            double u_next = 300;
            double u_ago = 300;
            double t = 0;
            double *tmp;
            std::vector<double *> arr;
            do {

                u_ago = u;
                u_next = Uk_next(u, u_next, u_next);
                u = Ox(u, a);
                tmp = new double[2];
                tmp[0] = u;
		u_values_vector[i][j].push_back(u);
                tmp[1] = u_next;
                arr.push_back(tmp);
            } while ((u_ago - u > eps) || (u_ago - u < -eps));

           // std::cstd::cout << "start printing " << std::endl;
            for (int j = 0; j < arr.size(); j++) {
             //   std::cstd::cout << arr.at(j)[0] << " " << arr.at(j)[1] << std::endl;
    //            std::cout << arr.at(j)[0] << " " << arr.at(j)[1] << endl;
            }
//            std::cout<< endl;
	}
    }

    for (int i = 0; i < n; ++i) 
    {
	for (int j = 0; j < m; ++j)
	{
	    for (int k = 0; k < u_values_vector[i][j].size(); k++) 
	    {
//		int a = i;
//		int b = j;
		for (int a = 0; a < n; ++a)
		{
		    for (int b = 0; b < m; ++b)
		    {	
			if( (a != i) || (b != j) )
			{
		            std::cout << a << " " << b << " " << 0 << " ";
			} else {
	    	 	    std::cout << i << " " << j << " ";
	                    std::cout << u_values_vector[i][j].at(k) << " ";
			}
		    }		    
		}
		std::cout << '\n';
            }
	}
    }
    return 0;
}
