
void main(int n) {
  assume (n <= 20 && n >= 0);
  int ni;
  ni = n;

  int f0;
  int f1;
  int f2;
  int f3;
  int f4;
  int f5;
  int f6;
  int f7;
  int f8;
  int f9;
  int f10;
  int f11;

  f0 = 0;
  f1 = 1;
  f2 = 1;
  f3 = 2;
  f4 = 3;
  f5 = 5;
  f6 = 8;
  f7 = 13;
  f8 = 21;
  f9 = 34;
  f10 = 55;
  f11 = 89;

  int x;
  int y;
  int temp;

  x = 0;
  y = 1;

  while (n > 1) {
    temp = x + y;
    x = y;
    y = temp;  

    n = n - 1;
  }

  if (ni == 0)
    assert(y == f0);
  if (ni == 1)
    assert(y == f1);
  if (ni == 2)
    assert(y == f2);
  if (ni == 3)
    assert(y == f3);
  if (ni == 4)
    assert(y == f4);
  if (ni == 5)
    assert(y == f5);
  if (ni == 6)
    assert(y == f6);
  if (ni == 7)
    assert(y == f7);
  if (ni == 8)
    assert(y == f8);
  if (ni == 9)
    assert(y == f9);
  if (ni == 10)
    assert(y == f10);
  if (ni == 11)
    assert(y == f11);
}
