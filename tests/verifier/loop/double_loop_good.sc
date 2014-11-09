
void main(int xi, int yi) {
  assume(xi < 100 && xi >= 0);
  assume(yi < 100 && yi >= 0);

  int x;
  int y;
  int m;
  int n;
  
  x = xi;
  y = yi;
  n = x * y;
  m = 0;

  while (x > 0 || m < n)
  inv(x >= 0)
  inv(m <= (xi - x) * yi)
  {
    y = yi;

    while (y > 0)
    inv(y >= 0)
    inv(m <= yi + (xi - x) * yi)
    {
      m = m + 1;
      y = y - 1;
    }

    x = x - 1;
  }

  assert(m == n);
}
