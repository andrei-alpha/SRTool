
void main(int n, int v) {

  int ni;
  int x;
  int y;
  ni = n;
  y = 0;
  x = 0;

  while (n > 0)
    inv(n >= 0)
    inv(x + y == ni - n) 
  {
    if (v < 50) {
      x = x + 1;
    } else {
      y = y + 2;
    }

    n = n - 1;
  }

  assert(x + y == ni);
  havoc(x);
  assume(x);
}
