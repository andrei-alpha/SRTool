
void main(int n) {
  assume(n >= 0 && n <= 45000);

  int sn;
  int i;

  sn = 0; 
  i = 0;
  while (i < n) {
    sn = sn + i;
    i = i + 1;
  }

  int v1;
  int v2;
  int v3;
  int v4;

  if (v1 == 1 && v2 == 7 && v3 == 2 && v4 == 9) {
    int Ramanujan;
    Ramanujan = v1 * 1000 + v2 * 100 + v3 * 10 + v4;
      
    assert(Ramanujan == 1 * 1 * 1 + 12 * 12 * 12);
    assert(Ramanujan == 9 * 9 * 9 + 10 * 10 * 10);
  }

  int x;
  assume(x > -9999 && x < 9999);
  // This has solutions x = 6 or -10  
  assume(x * x + 4 * x - 60 == 0);

  int cnt;
  cnt = sn / 2 + sn % 2 + (20 - sn);
  assert(x == 6 || x == -10 || cnt > 10);
}
