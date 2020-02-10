#include <iostream>
#include <iomanip>      // std::setw
#include <vector>
using std::vector;
using std::cout;
using std::endl;
using std::setw;


int main()
{
	setlocale(LC_ALL, "Russian");
	double r = 0.3;  //  Упругость.
	int h_len = 11;  // Длины вертикальных нитей.
	vector<int> v_len{ 10, 15, 20 };  // Длины горизонтальных нитей.
	int mid = h_len / 2;
	vector<int> h_intersect{ 3, 6 }; //  Координаты вертикальных нитей.
	vector<int> v_intersect{ 0, mid, h_len - 1 };  //  Координаты горизонтальных нитей.

	vector< vector<double> > v_new(3, vector<double>(v_len[2], 0));  // Новые координаты вертикальных нитей([номер нити][координата отрезка]).
	vector< vector<vector<double> > > v_last(3, vector< vector<double> >(2, vector<double>(v_len[2] + 2, 0)));  // Старые координаты вертикальных нитей[ ybntq[номер стрержня][номер времени][номер отрезка].

	vector< vector<double> > h_new(2, vector<double>(h_len, 0));  // Новые координаты горизонтальных нитей([номер нити][координата отрезка]).
	vector< vector<vector<double> > > h_last(2, vector< vector<double> >(2, vector<double>(h_len, 0)));  // Старые координаты горизонтальных нитей[ ybntq[номер стрержня][номер времени][номер отрезка].

	// Задаем изначальное положение.
	for (int i = 1; i < v_len[1] + 1; i++)
	{
		if (i < v_len[1] / 2 + 1)
			v_last[1][0][i] = 0.1 * i;
		else
			v_last[1][0][i] = (v_len[1] - i + 1) * 0.1;
	}

	// Рассчитываем новые значения k раз.
	for (int k = 0; k < 10; k++)
	{
		//  Рассчитываем новые значения горизонтальных нитей.
		for (int i = 0; i < 3; i++)
		{
			for (int j = 1; j < v_len[i] + 1; j++)
			{
				if (j - 1 == h_intersect[0] || j - 1 == h_intersect[1])
				{
					int h;
					if (j - 1 == h_intersect[0])
						h = 0;
					else if (j - 1 == h_intersect[1])
						h = 1;

					if (i == 0)
						v_new[i][j - 1] = r * (v_last[i][0][j - 1] - 3 * v_last[i][0][j] + v_last[i][0][j + 1] + h_last[h][0][v_intersect[i] + 1]) + 2 * v_last[i][0][j] - v_last[i][1][j];
					else if (i == 2)
						v_new[i][j - 1] = r * (v_last[i][0][j - 1] - 3 * v_last[i][0][j] + v_last[i][0][j + 1] + h_last[h][0][v_intersect[i] - 1]) + 2 * v_last[i][0][j] - v_last[i][1][j];
					else
					{
						v_new[i][j - 1] = r * (v_last[i][0][j - 1] - 4 * v_last[i][0][j] + v_last[i][0][j + 1] + h_last[h][0][v_intersect[i] - 1] + h_last[h][0][v_intersect[i] + 1]) + 2 * v_last[i][0][j] - v_last[i][1][j];
					}
				}
				else
					v_new[i][j - 1] = r * (v_last[i][0][j - 1] - 2 * v_last[i][0][j] + v_last[i][0][j + 1]) + 2 * v_last[i][0][j] - v_last[i][1][j];
			}

			for (int j = 1; j < v_len[i] + 1; j++)
			{
				v_last[i][1][j] = v_last[i][0][j];
				v_last[i][0][j] = v_new[i][j - 1];
			}
		}


		//  Присваиваем новые координаты горизонтальных нитей вертикальным.
		h_last[0][0][0] = v_new[0][4];
		h_last[1][0][0] = v_new[0][7];

		h_last[0][0][mid] = v_new[1][4];
		h_last[1][0][mid] = v_new[1][7];

		h_last[0][0][h_len - 1] = v_new[2][4];
		h_last[1][0][h_len - 1] = v_new[2][7];


		//  Рассчитываем новые значения вертикальных нитей.
		for (int i = 0; i < 2; i++)
		{
			for (int j = 0; j < h_len; j++)
			{
				if (j != v_intersect[0] && j != v_intersect[1] && j != v_intersect[2])
				{
					h_new[i][j] = r * (h_last[i][0][j - 1] - 2 * h_last[i][0][j] + h_last[i][0][j + 1]) + 2 * h_last[i][0][j] - h_last[i][1][j];
					h_last[i][1][j] = h_last[i][0][j];
					h_last[i][0][j] = h_new[i][j];
				}
			}
		}
		//cout << endl;
		//cout << endl;
		//  Выводим характеристики первой горизонтальной нити.
		for (int i = 0; i < v_len[0]; i++)
		{
			//cout << setw(10) << v_new[0][i]; // ВЕРХНЯЯ СТРУНА
		}
		//cout << endl;

		//  Между первой и второй нитью.
		/*for (int j = 1; j < mid; j++)
		{
			for (int i = 0; i < v_len[0]; i++)
			{
				if (i == 3)
					cout << setw(10) << h_new[0][j];
				else if (i == 6)
					cout << setw(10) << h_new[1][j];
				else
					cout << setw(10) << " ";
			}
			cout << endl;
		}*/

		//  Характеристики второй горизонтальной нити.
		for (int i = 0; i < v_len[1]; i++)
		{
			//cout << setw(10) << v_new[1][i];
		}
		cout << endl;

		//  Между второй и третьей нитью.
		//for (int j = mid + 1; j < h_len - 1; j++)
		//{
		//	for (int i = 0; i < v_len[0]; i++)
		//	{
		//		if (i == 3)
		//			cout << setw(10) << h_new[0][j];
		//		else if (i == 6)
		//			cout << setw(10) << h_new[1][j];
		//		else
		//			cout << setw(10) << " ";
		//	}
		//	cout << endl;
		//}

		//  Характеристики третьей горизонтальной нити.
		for (int i = 0; i < v_len[2]; i++)
		{
			//cout << setw(10) << v_new[2][i];
		}

		cout << 3 << " ";
		cout << 0 << " ";
		cout << v_new[0][3] << " ";

		cout << 6 << " ";
		cout << 0 << " ";
		cout << v_new[0][6] << " ";

		cout << 3 << " ";
		cout << mid << " ";
		cout << v_new[1][3] << " ";

		cout << 6 << " ";
		cout << mid << " ";
		cout << v_new[1][6] << " ";

		cout << 3 << " ";
		cout << h_len - 1 << " ";
		cout << v_new[2][3] << " ";

		cout << 6 << " ";
		cout << h_len - 1 << " ";
		cout << v_new[2][6] << " ";
	}



	cout << endl << endl << endl;
	return 0;
}